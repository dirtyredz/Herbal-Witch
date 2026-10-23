package io.github.dirtyredz.herbalwitch.apothecary.shop.order;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Nonbinding request lifecycle, caps, expiry, price ceiling, partial fulfilment and offer changes. */
class ShopOrderRulesTest {
    static final ShopStackKey TEA = ShopStackKey.of("herbalwitch:preparations/drinks/recovery_tea");
    final UUID customer = UUID.randomUUID();

    ShopOrder order(int quantity, int maxPrice, long now) {
        return ShopOrderRules.create(UUID.randomUUID(), customer, "Customer", TEA.item(), ShopOrder.Requirement.ANY, quantity, maxPrice, now, 1000);
    }

    @Test
    void capsCooldownAndBounds() {
        List<ShopOrder> orders = new ArrayList<>();
        assertEquals(ShopOrderRules.Refusal.QUANTITY, ShopOrderRules.canCreate(orders, customer, 65, 1, 0));
        assertEquals(ShopOrderRules.Refusal.PRICE, ShopOrderRules.canCreate(orders, customer, 1, 65, 0));
        for (int i = 0; i < 3; i++) {
            assertEquals(ShopOrderRules.Refusal.NONE, ShopOrderRules.canCreate(orders, customer, 1, 1, i * 200L));
            orders.add(order(1, 1, i * 200L));
        }
        assertEquals(ShopOrderRules.Refusal.CUSTOMER_LIMIT, ShopOrderRules.canCreate(orders, customer, 1, 1, 10_000));
        assertEquals(ShopOrderRules.Refusal.COOLDOWN, ShopOrderRules.canCreate(List.of(order(1, 1, 0)), customer, 1, 1, 199));
        List<ShopOrder> full = new ArrayList<>();
        for (int i = 0; i < 16; i++) full.add(ShopOrderRules.create(UUID.randomUUID(), UUID.randomUUID(), "x", TEA.item(), ShopOrder.Requirement.ANY, 1, 1, 0, 1000));
        assertEquals(ShopOrderRules.Refusal.SHOP_LIMIT, ShopOrderRules.canCreate(full, customer, 1, 1, 10_000));
    }

    @Test
    void associationNeedsCompatibleOfferAndPurchasesAdvanceUntilFulfilled() {
        ShopOrder order = order(3, 2, 0);
        ShopOffer pricey = new ShopOffer(UUID.randomUUID(), 1, TEA, 1, 3, true);
        assertEquals(ShopOrderRules.Refusal.PRICE, ShopOrderRules.compatible(order, pricey));
        ShopOffer big = new ShopOffer(UUID.randomUUID(), 1, TEA, 4, 1, true);
        assertEquals(ShopOrderRules.Refusal.QUANTITY, ShopOrderRules.compatible(order, big));
        CompoundTag tag = new CompoundTag();
        tag.putInt("Purity", 1);
        ShopOffer tagged = new ShopOffer(UUID.randomUUID(), 1, new ShopStackKey(TEA.item(), tag), 1, 1, true);
        assertEquals(ShopOrderRules.Refusal.NONE, ShopOrderRules.compatible(order, tagged));
        ShopOrder plainOnly = ShopOrderRules.create(UUID.randomUUID(), customer, "Customer", TEA.item(), ShopOrder.Requirement.DEFAULT, 3, 2, 0, 1000);
        assertEquals(ShopOrderRules.Refusal.INCOMPATIBLE, ShopOrderRules.compatible(plainOnly, tagged));
        ShopOffer good = new ShopOffer(UUID.randomUUID(), 1, TEA, 2, 2, true);
        ShopOrder offered = ShopOrderRules.associate(order, good);
        assertEquals(ShopOrder.State.OFFERED, offered.state());
        assertEquals(ShopOrderRules.Refusal.OWNER, ShopOrderRules.canFulfil(offered, good, UUID.randomUUID()), "another customer cannot buy against it");
        assertEquals(ShopOrderRules.Refusal.NONE, ShopOrderRules.canFulfil(offered, good, customer));
        ShopOrder partial = ShopOrderRules.purchased(offered, 2);
        assertEquals(2, partial.purchased());
        assertEquals(ShopOrderRules.Refusal.QUANTITY, ShopOrderRules.canFulfil(partial, good, customer), "the lot now exceeds the remainder");
        List<ShopOrder> refreshed = ShopOrderRules.refresh(List.of(partial), List.of(good), 1);
        assertEquals(ShopOrder.State.OPEN, refreshed.get(0).state(), "an offer that no longer fits returns the request to open");
        assertEquals(ShopOrder.State.FULFILLED, ShopOrderRules.purchased(ShopOrderRules.associate(order, new ShopOffer(UUID.randomUUID(), 1, TEA, 3, 2, true)), 3).state());
    }

    @Test
    void changedOrRemovedOffersReopenAndExpiryAndCancellationMoveNothing() {
        ShopOffer offer = new ShopOffer(UUID.randomUUID(), 1, TEA, 1, 1, true);
        ShopOrder offered = ShopOrderRules.associate(order(2, 2, 0), offer);
        ShopOffer edited = offer.edit(TEA, 1, 2, true);
        assertEquals(2, edited.revision());
        assertEquals(ShopOrder.State.OPEN, ShopOrderRules.refresh(List.of(offered), List.of(edited), 1).get(0).state());
        assertEquals(ShopOrder.State.OPEN, ShopOrderRules.refresh(List.of(offered), List.of(), 1).get(0).state());
        assertEquals(ShopOrder.State.EXPIRED, ShopOrderRules.refresh(List.of(offered), List.of(offer), 1000).get(0).state());
        List<ShopOrder> unchanged = List.of(offered);
        assertSame(unchanged, ShopOrderRules.refresh(unchanged, List.of(offer), 5));
        assertEquals(ShopOrderRules.Refusal.OWNER, ShopOrderRules.canCancel(offered, UUID.randomUUID()));
        ShopOrder cancelled = ShopOrderRules.cancel(offered);
        assertEquals(ShopOrder.State.CANCELLED, cancelled.state());
        assertEquals(ShopOrderRules.Refusal.STATE, ShopOrderRules.canCancel(cancelled, customer));
        assertTrue(offered.revision() < cancelled.revision());
    }

    @Test
    void terminalHistoryIsBounded() {
        List<ShopOrder> orders = new ArrayList<>();
        for (int i = 0; i < 60; i++) orders.add(ShopOrderRules.cancel(order(1, 1, i)));
        assertEquals(ShopOrderRules.RECENT_TERMINAL, ShopOrderRules.refresh(orders, List.of(), 100).size());
    }
}
