package io.github.dirtyredz.herbalwitch.apothecary.shop.access;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSession;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSessionService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopUpdate;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopManagement;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Owner-only management, buying modes, actor rules, bans, hand-breaking and private projections. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopPermissionGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_access", timeoutTicks = 60)
    public static void nonOwnersCannotManageStockProceedsOrOffers(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 4, 1);
        ServerPlayer other = witch(h, "intruder");
        other.getInventory().setItem(0, tea(3));
        var before = snapshot(other);
        long revision = record(h, shop).revision;
        check(ShopTrades.deposit(other, store(h), shop.id(), 0, 1).code().equals("not_owner"), "foreign deposit");
        check(ShopTrades.withdraw(other, store(h), shop.id(), 0, 1).code().equals("not_owner"), "foreign withdrawal");
        check(ShopTrades.proceeds(other, store(h), shop.id(), 1).code().equals("not_owner"), "foreign proceeds");
        check(ShopManagement.saveOffer(other, store(h), shop.id(), offer, 0, 1, 64, true).code().equals("not_owner"), "foreign price edit");
        check(ShopManagement.close(other, store(h), shop.id()).code().equals("not_owner"), "foreign close");
        check(same(before, other) && record(h, shop).revision == revision, "a refused management request changed something");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_access", timeoutTicks = 60)
    public static void adventureBuysButCreativeSpectatorFakeAndBannedPlayersCannot(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 6, 1);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        ServerPlayer adventure = player(h, "adventurer");
        adventure.setGameMode(GameType.ADVENTURE);
        emeralds(adventure, 2);
        check(ShopTrades.purchase(adventure, store(h), shop.id(), offer, revision, null).success(), "adventure buying is interaction, not building");
        ServerPlayer creative = player(h, "creative");
        creative.setGameMode(GameType.CREATIVE);
        emeralds(creative, 2);
        check(ShopTrades.purchase(creative, store(h), shop.id(), offer, revision, null).code().equals("actor"), "creative economic mutation");
        ServerPlayer spectator = player(h, "spectator");
        spectator.setGameMode(GameType.SPECTATOR);
        emeralds(spectator, 2);
        check(ShopTrades.purchase(spectator, store(h), shop.id(), offer, revision, null).code().equals("actor"), "spectator purchase");
        ServerPlayer fake = PlayerFixtures.player(h, unique("fake"), GameType.SURVIVAL, Direction.NORTH);
        emeralds(fake, 2);
        check(ShopTrades.purchase(fake, store(h), shop.id(), offer, revision, null).code().equals("actor"), "fake player purchase");
        ServerPlayer banned = player(h, "banned");
        emeralds(banned, 2);
        ShopManagementAccess.ban(h, shop, banned);
        check(ShopTrades.purchase(banned, store(h), shop.id(), offer, revision, null).code().equals("banned"), "banned purchase");
        check(ShopTrades.purchase(shop.owner(), store(h), shop.id(), offer, revision, null).code().equals("own_shop"), "owner self-purchase");
        check(record(h, shop).proceeds == 1 && count(creative, Items.EMERALD) == 2 && count(banned, Items.EMERALD) == 2, "only the adventure purchase moved value");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_access", timeoutTicks = 60)
    public static void nonOwnersCannotBreakABoundRegisterByHand(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        ServerPlayer other = player(h, "breaker");
        BlockPos register = h.absolutePos(shop.register());
        other.gameMode.destroyBlock(register);
        check(h.getLevel().getBlockState(register).is(io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks.REGISTER.get()), "a non-owner broke the register");
        check(h.getLevel().getBlockState(register).getPistonPushReaction() == net.minecraft.world.level.material.PushReaction.BLOCK, "pistons can move a register");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_access", timeoutTicks = 60)
    public static void customerProjectionsHideStockProceedsBansHistoryAndOtherRequests(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        selling(h, shop, 4, 1);
        ServerPlayer customer = player(h, "viewer");
        List<ShopUpdate> updates = new ArrayList<>();
        ShopSessionService.observe((player, update) -> updates.add(update));
        try {
            ShopSessionService.open(customer, ShopSession.View.BROWSE, shop.id(), ShopSession.Origin.REGISTER, h.getLevel(), h.absolutePos(shop.register()));
            CompoundTag view = updates.get(updates.size() - 1).snapshot();
            for (String hidden : List.of("stock", "proceeds", "bans", "history", "recovery", "links", "capacity")) check(!view.contains(hidden), "customer saw " + hidden);
            check(view.getList("offers", 10).size() == 1 && !view.getBoolean("owner"), "customer offers");
            customer.closeContainer();
            ShopSessionService.open(shop.owner(), ShopSession.View.MANAGE, shop.id(), ShopSession.Origin.REGISTER, h.getLevel(), h.absolutePos(shop.register()));
            CompoundTag owner = updates.get(updates.size() - 1).snapshot();
            check(owner.contains("stock") && owner.contains("proceeds") && owner.contains("history") && owner.getBoolean("owner"), "owner projection incomplete");
            shop.owner().closeContainer();
            h.succeed();
        } finally {
            ShopSessionService.observe(null);
        }
    }

    /** Test setup: records a ban directly; ShopManagement.ban resolves online names, which fixture players lack. */
    static final class ShopManagementAccess {
        static void ban(GameTestHelper h, Shop shop, ServerPlayer target) {
            var next = record(h, shop).next();
            next.bans.add(target.getUUID());
            check(store(h).update(next, true), "ban setup");
        }
    }
}
