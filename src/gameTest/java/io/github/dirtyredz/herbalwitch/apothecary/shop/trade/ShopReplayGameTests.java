package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopMenu;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSession;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSessionService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Real menu sessions: duplicate, reordered, foreign-nonce and closed-session requests never charge or grant twice. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopReplayGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);

    static ShopRequest buy(ShopMenu menu, int sequence, UUID offer, long revision) {
        return new ShopRequest(menu.containerId, menu.nonce(), sequence, ShopRequest.Action.BUY, offer, ShopRequest.NONE, revision, 0, 0, 0, false, "");
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_replay", timeoutTicks = 60)
    public static void duplicateReorderedAndStaleRequestsNeverRepeatACharge(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 5, 2);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        ServerPlayer buyer = player(h, "replay_buyer");
        emeralds(buyer, 10);
        List<ShopUpdate> updates = new ArrayList<>();
        ShopSessionService.observe((player, update) -> { if (player == buyer) updates.add(update); });
        try {
            ShopSessionService.open(buyer, ShopSession.View.BROWSE, shop.id(), ShopSession.Origin.REGISTER, h.getLevel(), h.absolutePos(shop.register()));
            check(buyer.containerMenu instanceof ShopMenu, "menu did not open");
            ShopMenu menu = (ShopMenu) buyer.containerMenu;
            check(!updates.isEmpty() && updates.get(0).result() == null && updates.get(0).snapshot().getString("name").equals("Test Shop"), "opening snapshot");
            check(!updates.get(0).snapshot().contains("proceeds") && !updates.get(0).snapshot().contains("stock"), "customer saw private data");
            ShopSessionService.request(buyer, buy(menu, 1, offer, revision));
            ShopUpdate first = updates.get(updates.size() - 1);
            check(first.result() != null && first.result().success() && first.result().transaction() != null, "first purchase: " + first.result());
            ShopSessionService.request(buyer, buy(menu, 1, offer, revision));
            ShopUpdate replay = updates.get(updates.size() - 1);
            check(replay.result().equals(first.result()), "retransmission did not replay the original result");
            check(count(buyer, Items.EMERALD) == 8 && count(buyer, tea(1).getItem()) == 1 && record(h, shop).proceeds == 2, "retransmission charged twice");
            ShopSessionService.request(buyer, buy(menu, 3, offer, revision));
            check(updates.get(updates.size() - 1).result().code().equals("stale_request"), "reordered request accepted");
            int seen = updates.size();
            ShopSessionService.request(buyer, new ShopRequest(menu.containerId, UUID.randomUUID(), 2, ShopRequest.Action.BUY, offer, ShopRequest.NONE, revision, 0, 0, 0, false, ""));
            ShopSessionService.request(buyer, new ShopRequest(menu.containerId + 1, menu.nonce(), 2, ShopRequest.Action.BUY, offer, ShopRequest.NONE, revision, 0, 0, 0, false, ""));
            check(updates.size() == seen, "a foreign nonce or menu id was answered");
            check(count(buyer, Items.EMERALD) == 8 && record(h, shop).proceeds == 2, "stale request moved value");
            ShopSessionService.request(buyer, buy(menu, 2, offer, revision));
            check(updates.get(updates.size() - 1).result().success() && count(buyer, tea(1).getItem()) == 2 && count(buyer, Items.EMERALD) == 6, "next sequence");
            buyer.closeContainer();
            seen = updates.size();
            ShopSessionService.request(buyer, buy(menu, 3, offer, revision));
            check(updates.size() == seen && count(buyer, tea(1).getItem()) == 2 && record(h, shop).proceeds == 4, "closed session still acts");
            h.succeed();
        } finally {
            ShopSessionService.observe(null);
        }
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_replay", timeoutTicks = 60)
    public static void sessionsEndWithReachAndRebindAndRecheckEveryRequest(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 5, 1);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        ServerPlayer buyer = player(h, "far_buyer");
        emeralds(buyer, 5);
        List<ShopUpdate> updates = new ArrayList<>();
        ShopSessionService.observe((player, update) -> { if (player == buyer) updates.add(update); });
        try {
            ShopSessionService.open(buyer, ShopSession.View.BROWSE, shop.id(), ShopSession.Origin.REGISTER, h.getLevel(), h.absolutePos(shop.register()));
            ShopMenu menu = (ShopMenu) buyer.containerMenu;
            buyer.moveTo(buyer.getX(), buyer.getY(), buyer.getZ() - 12);
            check(!menu.stillValid(buyer), "the menu stays valid out of reach");
            ShopSessionService.request(buyer, buy(menu, 1, offer, revision));
            check(updates.get(updates.size() - 1).result().code().equals("session"), "an out-of-reach request was handled");
            check(count(buyer, Items.EMERALD) == 5 && record(h, shop).proceeds == 0, "out-of-reach request moved value");
            h.succeed();
        } finally {
            ShopSessionService.observe(null);
        }
    }
}
