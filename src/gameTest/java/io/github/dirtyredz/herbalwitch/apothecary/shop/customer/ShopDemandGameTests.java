package io.github.dirtyredz.herbalwitch.apothecary.shop.customer;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransfer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Villager budgets through the production sale path: several shops, owners and villagers contend; time
 * changes, a replacement register and new villagers cannot reset or bypass a budget; refused debits
 * return the reserved lot and never create an emerald.
 */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopDemandGameTests {
    static ShopTransfer.Result sell(GameTestHelper h, Shop shop, UUID villager) {
        MinecraftServer server = h.getLevel().getServer();
        ShopRecord r = record(h, shop);
        ShopOffer offer = r.offers.get(0);
        var plan = new ShopTransfer.Plan(ShopTransaction.Kind.NPC_SALE, offer.id(), List.of(), List.of(new ShopTransaction.Goods(offer.key(), 1, 16)),
                List.of(), 0, 1, null, 0);
        return ShopTransfer.villager(store(h), shop.id(), villager, h.getLevel().getGameTime(), plan,
                t -> NpcDemandService.debit(server, t, r.owner, r.id, villager));
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_demand", timeoutTicks = 60)
    public static void budgetsHoldAcrossShopsOwnersVillagersTimeAndReplacementRegisters(GameTestHelper h) {
        floor(h);
        ShopRules old = ShopServerConfig.current();
        long day = h.getLevel().getDayTime();
        try {
            ShopServerConfig.install(new ShopRules(old.shopsPerOwner(), 4096, old.proceedsCapacity(), true, 24_000, 1024, 2, 1, 1,
                    old.visitInterval(), old.visitDuration(), old.concurrentVisits(), old.orderLifetimeWindows()));
            var owner = witch(h, "budget_owner");
            Shop first = shop(h, owner, new BlockPos(1, 1, 4));
            Shop second = shop(h, owner, new BlockPos(4, 1, 4));
            Shop third = shop(h, owner, new BlockPos(7, 1, 4));
            for (Shop s : List.of(first, second, third)) selling(h, s, 4, 1);
            UUID v1 = UUID.randomUUID(), v2 = UUID.randomUUID(), v3 = UUID.randomUUID(), v4 = UUID.randomUUID();
            check(sell(h, first, v1).success(), "first sale");
            ShopTransfer.Result again = sell(h, first, v2);
            check(!again.success() && again.reason().equals("budget"), "a new villager bypassed the shop budget");
            check(record(h, first).stock.count(ShopStackKey.of(tea(1))) == 3 && record(h, first).proceeds == 1, "a refused debit kept the lot or minted an emerald");
            check(!sell(h, second, v1).success(), "one villager bought twice in a window");
            check(sell(h, second, v3).success(), "second shop sale");
            h.getLevel().setDayTime(h.getLevel().getDayTime() + 48_000);
            check(!sell(h, third, v4).success(), "a time change reset the owner budget");
            // A replacement register keeps the shop identity: its budget does not reset.
            owner.gameMode.destroyBlock(h.absolutePos(first.register()));
            BlockPos register = new BlockPos(1, 2, 7);
            h.setBlock(register.below(), DecorBlocks.block(SurfacesCatalog.APOTHECARY_COUNTER).defaultBlockState());
            h.setBlock(register, ShopBlocks.REGISTER.get().defaultBlockState().setValue(ShopFixtureBlock.FACING, Direction.NORTH));
            check(ShopBindingService.reconnect(owner, h.getLevel(), h.absolutePos(register),
                    (ShopFixtureBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(register)), first.id()).success(), "reconnect");
            ShopRecord reopened = record(h, first).next();
            reopened.lifecycle = ShopRecord.Lifecycle.OPEN;
            check(store(h).update(reopened, true), "reopen");
            check(!sell(h, first, UUID.randomUUID()).success(), "a replacement register reset the shop budget");
            int teas = 0;
            long proceeds = 0;
            for (Shop s : List.of(first, second, third)) {
                teas += record(h, s).stock.count(ShopStackKey.of(tea(1)));
                proceeds += record(h, s).proceeds;
            }
            clean(h);
            check(teas == 10 && proceeds == 2, "only the two charged sales consumed stock and paid: " + teas + "/" + proceeds);
            h.succeed();
        } finally {
            ShopServerConfig.install(old);
            h.getLevel().setDayTime(day);
        }
    }
}
