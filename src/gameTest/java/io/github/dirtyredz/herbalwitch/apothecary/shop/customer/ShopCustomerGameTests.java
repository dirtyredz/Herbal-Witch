package io.github.dirtyredz.herbalwitch.apothecary.shop.customer;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopManagement;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Actual vanilla villagers walking to an open register and buying, and every case that must not sell. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopCustomerGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);
    private static ShopRules previous;

    @BeforeBatch(batch = "herbalwitch_shop_customers")
    public static void generous(ServerLevel level) {
        previous = ShopServerConfig.current();
        ShopRules r = previous;
        ShopServerConfig.install(new ShopRules(r.shopsPerOwner(), 4096, r.proceedsCapacity(), true, 24_000, 1024, 128, 64, 4, 200, 600, 32, r.orderLifetimeWindows()));
    }

    @AfterBatch(batch = "herbalwitch_shop_customers")
    public static void restore(ServerLevel level) {
        if (previous != null) ShopServerConfig.install(previous);
    }

    static Shop villagerShop(GameTestHelper h, int teas) {
        floor(h);
        Shop shop = shop(h, witch(h, "npc_owner"), COUNTER);
        selling(h, shop, teas, 1);
        check(ShopManagement.villagers(shop.owner(), store(h), shop.id(), true).success(), "enable villagers");
        return shop;
    }

    static boolean visiting(GameTestHelper h, Shop shop) {
        return ShopCustomerScheduler.visits(h.getLevel().getServer()).stream().anyMatch(v -> v.shop().equals(shop.id()));
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_customers", timeoutTicks = 1600)
    public static void aRealVillagerWalksToTheRegisterAndBuysOneLot(GameTestHelper h) {
        Shop shop = villagerShop(h, 3);
        Villager villager = h.spawn(EntityType.VILLAGER, new BlockPos(4, 1, 0));
        villager.setAge(0);
        h.succeedWhen(() -> {
            var r = record(h, shop);
            check(r.proceeds == 1, "no villager sale yet");
            check(r.stock.count(ShopStackKey.of(tea(1))) == 2, "villager sale moved the wrong stock");
            check(r.history.stream().anyMatch(e -> e.type() == ShopHistoryEntry.Type.NPC_SALE && e.actor().equals(villager.getUUID())), "history");
            check(villager.getInventory().isEmpty(), "the preparation entered the villager's inventory");
            check(villager.getVillagerXp() == 0 && villager.getOffers().stream().noneMatch(o -> o.getResult().is(tea(1).getItem())), "the sale became a vanilla trade");
        });
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_customers", timeoutTicks = 400)
    public static void babiesHurtTradingAndUnreachableVillagersAreNeverServed(GameTestHelper h) {
        Shop shop = villagerShop(h, 3);
        Villager baby = h.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 1));
        baby.setAge(-24000);
        Villager hurt = h.spawn(EntityType.VILLAGER, new BlockPos(7, 1, 1));
        hurt.setAge(0);
        Villager trading = h.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 7));
        trading.setAge(0);
        // A sealed glass room around the counter: nothing can reach the register.
        for (int x = 3; x <= 5; x++) for (int y = 1; y <= 3; y++) for (int z = 3; z <= 5; z++) {
            if ((x == 4 && z == 4 && y <= 2)) continue;
            h.setBlock(new BlockPos(x, y, z), Blocks.GLASS);
        }
        h.runAtTickTime(300, () -> {
            // Set just before the check: vanilla clears a stale trading partner and a hurt memory over time.
            hurt.getBrain().setMemory(MemoryModuleType.HURT_BY, h.getLevel().damageSources().generic());
            trading.setTradingPlayer(player(h, "trader"));
            check(record(h, shop).proceeds == 0 && record(h, shop).stock.count(ShopStackKey.of(tea(1))) == 3, "an ineligible villager bought");
            check(!ShopCustomerRules.eligible(baby) && !ShopCustomerRules.eligible(hurt) && !ShopCustomerRules.eligible(trading), "eligibility");
            h.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_customers", timeoutTicks = 1600)
    public static void closingTheShopOrChangingTheOfferCancelsAVisitWithoutSelling(GameTestHelper h) {
        Shop shop = villagerShop(h, 3);
        Villager villager = h.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 0));
        villager.setAge(0);
        UUID offer = record(h, shop).offers.get(0).id();
        boolean[] closed = {false};
        h.succeedWhen(() -> {
            if (!closed[0]) {
                check(visiting(h, shop), "waiting for a visit");
                check(ShopManagement.saveOffer(shop.owner(), store(h), shop.id(), offer, 0, 1, 2, true).success(), "offer change");
                check(ShopManagement.close(shop.owner(), store(h), shop.id()).success(), "close");
                closed[0] = true;
                check(false, "waiting for cancellation");
            }
            check(!visiting(h, shop), "visit survived a closed shop and a changed offer");
            check(record(h, shop).proceeds == 0 && record(h, shop).stock.count(ShopStackKey.of(tea(1))) == 3, "a cancelled visit sold");
        });
    }
}
