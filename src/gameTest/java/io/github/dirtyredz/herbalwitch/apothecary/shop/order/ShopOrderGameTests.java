package io.github.dirtyredz.herbalwitch.apothecary.shop.order;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopManagement;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Nonbinding requests through the real services: association, partial and complete purchase, cancel, decline, expiry, replay. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopOrderGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);

    static String teaId() {
        return String.valueOf(ForgeRegistries.ITEMS.getKey(tea(1).getItem()));
    }

    static ShopOrder only(GameTestHelper h, Shop shop, UUID customer) {
        return record(h, shop).orders.stream().filter(o -> o.requester().equals(customer)).reduce((a, b) -> b).orElseThrow();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_orders", timeoutTicks = 60)
    public static void anAssociatedRequestIsFulfilledByOrdinaryPurchasesExactlyOnce(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "order_owner"), COUNTER);
        UUID offer = selling(h, shop, 5, 2);
        ServerPlayer customer = player(h, "requester");
        emeralds(customer, 20);
        var before = snapshot(customer);
        check(ShopOrderService.create(customer, store(h), shop.id(), teaId(), false, 2, 2).success(), "request");
        check(same(before, customer) && record(h, shop).proceeds == 0, "a request moved value");
        ShopOrder order = only(h, shop, customer.getUUID());
        check(ShopOrderService.associate(customer, store(h), shop.id(), order.id(), offer).code().equals("not_owner"), "customer associated");
        check(ShopOrderService.associate(shop.owner(), store(h), shop.id(), order.id(), offer).success(), "association");
        order = only(h, shop, customer.getUUID());
        check(order.state() == ShopOrder.State.OFFERED, "offered");
        ServerPlayer other = player(h, "other");
        emeralds(other, 5);
        check(ShopTrades.purchase(other, store(h), shop.id(), offer, order.offerRevision(), order.id()).code().equals("order_stale"), "another customer used the request");
        check(ShopTrades.purchase(customer, store(h), shop.id(), offer, order.offerRevision(), order.id()).success(), "first lot");
        check(only(h, shop, customer.getUUID()).purchased() == 1 && only(h, shop, customer.getUUID()).state() == ShopOrder.State.OFFERED, "partial");
        check(ShopTrades.purchase(customer, store(h), shop.id(), offer, order.offerRevision(), order.id()).success(), "second lot");
        check(only(h, shop, customer.getUUID()).state() == ShopOrder.State.FULFILLED, "fulfilled");
        check(ShopTrades.purchase(customer, store(h), shop.id(), offer, order.offerRevision(), order.id()).code().equals("order_stale"), "a fulfilled request bought again");
        check(count(customer, tea(1).getItem()) == 2 && count(customer, Items.EMERALD) == 16 && record(h, shop).proceeds == 4, "value moved twice");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_orders", timeoutTicks = 60)
    public static void cancelDeclineExpiryAndOfferChangesMoveNothing(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "order_owner"), COUNTER);
        UUID offer = selling(h, shop, 5, 1);
        ServerPlayer a = player(h, "cancel"), b = player(h, "decline"), c = player(h, "expire");
        for (ServerPlayer p : java.util.List.of(a, b, c)) {
            emeralds(p, 5);
            check(ShopOrderService.create(p, store(h), shop.id(), teaId(), true, 3, 1).success(), "request");
        }
        check(ShopOrderService.create(a, store(h), shop.id(), teaId(), true, 3, 1).code().equals("order_cooldown"), "cooldown");
        check(ShopOrderService.cancel(b, store(h), shop.id(), only(h, shop, a.getUUID()).id()).code().equals("order_owner"), "cancel someone else's request");
        check(ShopOrderService.cancel(a, store(h), shop.id(), only(h, shop, a.getUUID()).id()).success(), "cancel");
        check(ShopOrderService.decline(shop.owner(), store(h), shop.id(), only(h, shop, b.getUUID()).id()).success(), "decline");
        check(ShopOrderService.associate(shop.owner(), store(h), shop.id(), only(h, shop, c.getUUID()).id(), offer).success(), "associate");
        check(ShopManagement.saveOffer(shop.owner(), store(h), shop.id(), offer, 0, 1, 1, false).success(), "offer change");
        check(only(h, shop, c.getUUID()).state() == ShopOrder.State.OPEN, "a changed offer did not return the request to open");
        NpcDemandService.get(h.getLevel().getServer()).tick(ShopServerConfig.current().orderLifetime(), ShopServerConfig.current().economyWindow());
        check(ShopManagement.rename(shop.owner(), store(h), shop.id(), "Expired Shop").success(), "refresh");
        check(only(h, shop, c.getUUID()).state() == ShopOrder.State.EXPIRED, "request did not expire");
        check(only(h, shop, a.getUUID()).state() == ShopOrder.State.CANCELLED && only(h, shop, b.getUUID()).state() == ShopOrder.State.DECLINED, "states");
        for (ServerPlayer p : java.util.List.of(a, b, c)) check(count(p, Items.EMERALD) == 5 && count(p, tea(1).getItem()) == 0, "a request transition moved value");
        check(record(h, shop).proceeds == 0, "proceeds moved");
        h.succeed();
    }
}
