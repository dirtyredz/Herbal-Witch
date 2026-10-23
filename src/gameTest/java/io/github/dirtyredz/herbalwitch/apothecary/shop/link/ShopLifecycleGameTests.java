package io.github.dirtyredz.herbalwitch.apothecary.shop.link;

import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopLinkService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopItems;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopManagement;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Register removal, support loss, explosions, rebinding, copied entities, fixture links and projections. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopLifecycleGameTests {
    static ShopFixtureBlockEntity fixture(GameTestHelper h, BlockPos relative) {
        return (ShopFixtureBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(relative));
    }

    static void detached(GameTestHelper h, Shop shop, int teas, String how) {
        ShopRecord r = record(h, shop);
        check(r.lifecycle == ShopRecord.Lifecycle.DETACHED && r.binding == null, how + " did not detach the shop");
        check(r.stock.count(ShopStackKey.of(tea(1))) == teas, how + " lost protected stock");
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_lifecycle", timeoutTicks = 80)
    public static void registerRemovalSupportLossAndExplosionsDetachWithoutExposingValue(GameTestHelper h) {
        floor(h);
        ServerPlayer owner = witch(h, "owner");
        Shop broken = shop(h, owner, new BlockPos(1, 1, 4));
        Shop unsupported = shop(h, owner, new BlockPos(4, 1, 4));
        Shop exploded = shop(h, owner, new BlockPos(4, 1, 7));
        stock(h, broken, tea(3));
        stock(h, unsupported, tea(4));
        stock(h, exploded, tea(5));
        owner.gameMode.destroyBlock(h.absolutePos(broken.register()));
        h.setBlock(unsupported.counter(), Blocks.AIR);
        h.getLevel().explode(null, h.absolutePos(exploded.register()).getX() + 0.5, h.absolutePos(exploded.register()).getY() + 0.5,
                h.absolutePos(exploded.register()).getZ() + 0.5, 1.0F, Level.ExplosionInteraction.BLOCK);
        h.runAfterDelay(3, () -> {
            check(h.getBlockState(unsupported.register()).isAir(), "register survived losing its counter");
            detached(h, broken, 3, "breaking");
            detached(h, unsupported, 4, "support loss");
            detached(h, exploded, 5, "explosion");
            List<ItemEntity> drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(BlockPos.ZERO)).inflate(12),
                    e -> e.getItem().is(ShopItems.item(ShopPieceCatalog.REGISTER)));
            check(!drops.isEmpty() && drops.stream().noneMatch(e -> e.getItem().hasTag()), "a register drop carried data");
            check(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(BlockPos.ZERO)).inflate(12), e -> e.getItem().is(tea(1).getItem())).isEmpty(),
                    "stock leaked into the world");
            clean(h);
            h.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_lifecycle", timeoutTicks = 60)
    public static void reconnectAdvancesTheGenerationAndCopiedRegistersNeverBind(GameTestHelper h) {
        floor(h);
        ServerPlayer owner = witch(h, "rebind_owner");
        Shop shop = shop(h, owner, new BlockPos(1, 1, 4));
        var oldCopy = fixture(h, shop.register()).saveWithFullMetadata();
        owner.gameMode.destroyBlock(h.absolutePos(shop.register()));
        detached(h, shop, 0, "breaking");
        BlockPos counter = new BlockPos(4, 1, 4), register = counter.above();
        h.setBlock(counter, DecorBlocks.block(io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog.APOTHECARY_COUNTER).defaultBlockState());
        h.setBlock(register, ShopBlocks.REGISTER.get().defaultBlockState().setValue(ShopFixtureBlock.FACING, Direction.NORTH));
        check(ShopBindingService.reconnect(owner, h.getLevel(), h.absolutePos(register), fixture(h, register), shop.id()).success(), "reconnect");
        ShopRecord r = record(h, shop);
        check(r.generation == 2 && r.binding.generation() == 2 && r.lifecycle == ShopRecord.Lifecycle.CLOSED, "reconnect generation");
        // A copied register entity at a third position claims the shop under its old generation: it must never bind.
        BlockPos copy = new BlockPos(7, 2, 4);
        h.setBlock(copy.below(), DecorBlocks.block(io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog.APOTHECARY_COUNTER).defaultBlockState());
        h.setBlock(copy, ShopBlocks.REGISTER.get().defaultBlockState().setValue(ShopFixtureBlock.FACING, Direction.NORTH));
        fixture(h, copy).load(oldCopy);
        check(ShopBindingService.resolveRegister(h.getLevel(), h.absolutePos(copy), fixture(h, copy)).isEmpty(), "a copied register bound the shop");
        check(fixture(h, copy).shop() == null, "the stale claim was kept");
        check(ShopBindingService.resolveRegister(h.getLevel(), h.absolutePos(register), fixture(h, register)).map(x -> x.id).orElse(null) != null, "real register lost");
        clean(h);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_lifecycle", timeoutTicks = 60)
    public static void fixturesBelongToOneShopAndProjectWithoutInventory(GameTestHelper h) {
        floor(h);
        ServerPlayer owner = witch(h, "link_owner");
        Shop a = shop(h, owner, new BlockPos(1, 1, 2));
        Shop b = shop(h, witch(h, "rival"), new BlockPos(7, 1, 2));
        UUID offer = selling(h, a, 3, 2);
        BlockPos shelf = new BlockPos(4, 2, 6), plainShelf = new BlockPos(6, 2, 6);
        h.setBlock(shelf.south(), Blocks.STONE);
        h.setBlock(shelf, ShopBlocks.STOCK_SHELF.get().defaultBlockState().setValue(ShopFixtureBlock.FACING, Direction.NORTH));
        h.setBlock(plainShelf.south(), Blocks.STONE);
        h.setBlock(plainShelf, DecorBlocks.block(StorageCatalog.ALL.stream().filter(s -> s.id().equals("small_wall_shelf")).findFirst().orElseThrow())
                .defaultBlockState());
        check(ShopLinkService.arm(owner, store(h), a.id(), ShopLinkService.Mode.DISPLAY).success(), "arm");
        check(ShopLinkService.click(owner, h.getLevel(), h.absolutePos(plainShelf)).code().equals("link_ordinary_shelf"), "ordinary shelf linked");
        check(ShopLinkService.arm(owner, store(h), a.id(), ShopLinkService.Mode.DISPLAY).success(), "re-arm");
        check(ShopLinkService.click(owner, h.getLevel(), h.absolutePos(shelf)).success(), "link shelf");
        check(ShopLinkService.arm(b.owner(), store(h), b.id(), ShopLinkService.Mode.DISPLAY).success(), "rival arm");
        check(ShopLinkService.click(b.owner(), h.getLevel(), h.absolutePos(shelf)).code().equals("link_taken"), "a fixture joined two shops");
        int index = record(h, a).links.size() - 1;
        check(record(h, a).links.get(index).kind() == ShopLink.Kind.SHELF, "link kind");
        check(ShopManagement.assign(owner, store(h), a.id(), index, 0, offer).success(), "assign");
        io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopProjection.publish(h.getLevel().getServer(), a.id());
        ShopDisplayBlockEntity display = (ShopDisplayBlockEntity) fixture(h, shelf);
        check(display.samples().size() == 1 && display.samples().get(0).available() && display.samples().get(0).stack().getCount() == 1, "projection");
        check(record(h, a).stock.count(ShopStackKey.of(tea(1))) == 3, "projection moved stock");
        owner.gameMode.destroyBlock(h.absolutePos(a.register()));
        check(!((ShopDisplayBlockEntity) fixture(h, shelf)).samples().get(0).available(), "display still sells without its register");
        owner.gameMode.destroyBlock(h.absolutePos(shelf));
        h.runAfterDelay(2, () -> {
            check(record(h, a).links.stream().noneMatch(l -> l.kind() == ShopLink.Kind.SHELF), "removed shelf still linked");
            check(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(BlockPos.ZERO)).inflate(12),
                    e -> e.getItem().is(tea(1).getItem())).isEmpty(), "display removal dropped stock");
            clean(h);
            h.succeed();
        });
    }
}
