package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopManagement;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Real commerce setups for GameTests: unique real players (the retained ANU world keeps earlier shops,
 * so identities are fresh per run), an actual counter with a register bound through the production
 * service, deposits and offers through the production transfer path, and conservation helpers.
 */
public final class ShopFixtures {
    /** One bound shop: its id, owner and the relative counter/register positions. */
    public record Shop(UUID id, ServerPlayer owner, BlockPos counter, BlockPos register) {
    }

    private ShopFixtures() {
    }

    public static String unique(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** A real survival player with an empty inventory standing at relative (4, 1, 1). */
    public static ServerPlayer player(GameTestHelper h, String name) {
        ServerPlayer player = AnimalFixtures.realPlayer(h, unique(name));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        BlockPos stand = h.absolutePos(new BlockPos(4, 1, 1));
        player.moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0, 30);
        return player;
    }

    public static ServerPlayer witch(GameTestHelper h, String name) {
        ServerPlayer player = player(h, name);
        check(WitchStatusService.convert(player, UUID.randomUUID()), "witch setup");
        return player;
    }

    public static ShopStore store(GameTestHelper h) {
        ShopStore store = ShopStore.get(h.getLevel().getServer());
        while (!store.indexed()) store.indexBatch();
        check(store.ready(), "shop store unavailable");
        if (ShopServerConfig.current().shopsPerWorld() < 4096) {
            ShopRules r = ShopServerConfig.current();
            ShopServerConfig.install(new ShopRules(r.shopsPerOwner(), 4096, r.proceedsCapacity(), r.npcService(), r.economyWindow(), r.worldSpending(),
                    r.ownerSpending(), r.shopSpending(), r.villagerPurchases(), r.visitInterval(), r.visitDuration(), r.concurrentVisits(), r.orderLifetimeWindows()));
        }
        return store;
    }

    /** An apothecary counter at {@code counter} with a fresh register on top, bound to a new shop by {@code owner}. */
    public static Shop shop(GameTestHelper h, ServerPlayer owner, BlockPos counter) {
        ShopStore store = store(h);
        h.setBlock(counter, DecorBlocks.block(SurfacesCatalog.APOTHECARY_COUNTER).defaultBlockState());
        BlockPos register = counter.above();
        h.setBlock(register, ShopBlocks.REGISTER.get().defaultBlockState().setValue(ShopFixtureBlock.FACING, Direction.NORTH));
        ShopFixtureBlockEntity be = (ShopFixtureBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(register));
        ShopResult created = ShopBindingService.create(owner, h.getLevel(), h.absolutePos(register), be, "Test Shop");
        check(created.success(), "shop creation refused: " + created.code());
        UUID id = store.indexes().register(ShopBindingService.dimension(h.getLevel()), h.absolutePos(register).asLong()).orElseThrow();
        return new Shop(id, owner, counter, register);
    }

    public static ShopRecord record(GameTestHelper h, Shop shop) {
        return store(h).record(shop.id()).orElseThrow();
    }

    public static ItemStack tea(int count) {
        return new ItemStack(PreparationItems.product(EverydayProductCatalog.RECOVERY_TEA), count);
    }

    /** Deposits {@code stack} through the real transfer path from the owner's slot 0. */
    public static void stock(GameTestHelper h, Shop shop, ItemStack stack) {
        ServerPlayer owner = shop.owner();
        owner.getInventory().setItem(0, stack.copy());
        ShopResult result = ShopTrades.deposit(owner, store(h), shop.id(), 0, stack.getCount());
        check(result.success(), "deposit refused: " + result.code());
    }

    /** Adds an offer from the product in {@code stockSlot}; returns its id. */
    public static UUID offer(GameTestHelper h, Shop shop, int stockSlot, int lot, int price) {
        ShopResult result = ShopManagement.saveOffer(shop.owner(), store(h), shop.id(), null, stockSlot, lot, price, true);
        check(result.success(), "offer refused: " + result.code());
        List<?> offers = record(h, shop).offers;
        return record(h, shop).offers.get(offers.size() - 1).id();
    }

    public static void open(GameTestHelper h, Shop shop) {
        ShopResult result = ShopManagement.open(shop.owner(), store(h), shop.id());
        check(result.success(), "open refused: " + result.code());
    }

    /** A stocked, open shop selling one-tea lots for {@code price}; returns the offer id. */
    public static UUID selling(GameTestHelper h, Shop shop, int teas, int price) {
        stock(h, shop, tea(teas));
        UUID offer = offer(h, shop, 0, 1, price);
        open(h, shop);
        return offer;
    }

    public static int count(ServerPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) if (stack.is(item)) total += stack.getCount();
        return total;
    }

    public static void give(ServerPlayer player, int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    public static void emeralds(ServerPlayer player, int count) {
        player.getInventory().setItem(8, new ItemStack(Items.EMERALD, count));
    }

    /** A snapshot of all 36 main slots, for "nothing changed" assertions. */
    public static List<ItemStack> snapshot(ServerPlayer player) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) out.add(stack.copy());
        return out;
    }

    public static boolean same(List<ItemStack> before, ServerPlayer player) {
        for (int i = 0; i < before.size(); i++) if (!ItemStack.matches(before.get(i), player.getInventory().items.get(i))) return false;
        return true;
    }

    public static void floor(GameTestHelper h) {
        for (int x = 0; x < 9; x++) for (int z = 0; z < 9; z++) h.setBlock(new BlockPos(x, 0, z), net.minecraft.world.level.block.Blocks.STONE);
    }
}
