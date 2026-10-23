package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStock;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure transfer simulations: payment before output, exact keys, no drops, conservation. */
class ShopPurchasePlanTest {
    static final ShopStackKey TEA = ShopStackKey.of("herbalwitch:preparations/drinks/recovery_tea");
    static final ShopStackKey DIRT = ShopStackKey.of("minecraft:dirt");

    static ShopInventoryModel inventory(Object... slotKeyCount) {
        List<ShopInventoryModel.Held> held = new ArrayList<>();
        for (int i = 0; i < ShopInventoryModel.SLOTS; i++) held.add(ShopInventoryModel.Held.EMPTY);
        for (int i = 0; i < slotKeyCount.length; i += 3) {
            ShopStackKey key = (ShopStackKey) slotKeyCount[i + 1];
            held.set((Integer) slotKeyCount[i], new ShopInventoryModel.Held(key, (Integer) slotKeyCount[i + 2], key.equals(TEA) ? 16 : 64));
        }
        return new ShopInventoryModel(held);
    }

    static ShopInventoryModel full(ShopStackKey filler, Object... extra) {
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < ShopInventoryModel.SLOTS; i++) {
            args.add(i);
            args.add(filler);
            args.add(64);
        }
        ShopInventoryModel base = inventory(args.toArray());
        List<ShopInventoryModel.Held> held = new ArrayList<>();
        for (int i = 0; i < ShopInventoryModel.SLOTS; i++) held.add(base.slot(i));
        for (int i = 0; i < extra.length; i += 3) {
            ShopStackKey key = (ShopStackKey) extra[i + 1];
            held.set((Integer) extra[i], new ShopInventoryModel.Held(key, (Integer) extra[i + 2], key.equals(TEA) ? 16 : 64));
        }
        return new ShopInventoryModel(held);
    }

    static ShopStock stock(ShopStackKey key, int count) {
        ShopStock stock = new ShopStock();
        if (count > 0) assertTrue(stock.insert(key, count, 16));
        return stock;
    }

    static ShopOffer offer(int lot, int price) {
        return new ShopOffer(UUID.randomUUID(), 1, TEA, lot, price, true);
    }

    @Test
    void purchaseMovesExactlyThePriceAndTheLot() {
        var before = inventory(0, ShopInventoryModel.EMERALD, 10);
        var outcome = ShopPurchasePlan.purchase(before, offer(2, 3), stock(TEA, 5), 0, 1000, 16);
        assertTrue(outcome.allowed());
        assertEquals(7, outcome.after().count(ShopInventoryModel.EMERALD));
        assertEquals(2, outcome.after().count(TEA));
        assertEquals(10, before.count(ShopInventoryModel.EMERALD), "the input model is never changed");
    }

    @Test
    void paymentFreesASlotBeforeTheLotIsPlaced() {
        // Every slot full; the only emeralds are exactly the price, so paying frees one slot for the lot.
        var before = full(DIRT, 35, ShopInventoryModel.EMERALD, 2);
        var outcome = ShopPurchasePlan.purchase(before, offer(1, 2), stock(TEA, 1), 0, 1000, 16);
        assertTrue(outcome.allowed());
        assertEquals(TEA, outcome.after().slot(35).key());
        // Paying only part of a stack frees nothing: refused, never dropped.
        var crowded = full(DIRT, 35, ShopInventoryModel.EMERALD, 3);
        assertEquals(ShopPurchasePlan.Refusal.INVENTORY_FULL, ShopPurchasePlan.purchase(crowded, offer(1, 2), stock(TEA, 1), 0, 1000, 16).refusal());
    }

    @Test
    void refusalsMutateNothing() {
        var poor = inventory(0, ShopInventoryModel.EMERALD, 1);
        assertEquals(ShopPurchasePlan.Refusal.PAYMENT, ShopPurchasePlan.purchase(poor, offer(1, 2), stock(TEA, 4), 0, 1000, 16).refusal());
        var rich = inventory(0, ShopInventoryModel.EMERALD, 64);
        assertEquals(ShopPurchasePlan.Refusal.SOLD_OUT, ShopPurchasePlan.purchase(rich, offer(5, 1), stock(TEA, 4), 0, 1000, 16).refusal());
        assertEquals(ShopPurchasePlan.Refusal.PROCEEDS_FULL, ShopPurchasePlan.purchase(rich, offer(1, 2), stock(TEA, 4), 999, 1000, 16).refusal());
        assertEquals(ShopPurchasePlan.Refusal.DISABLED, ShopPurchasePlan.purchase(rich,
                new ShopOffer(UUID.randomUUID(), 1, TEA, 1, 1, false), stock(TEA, 4), 0, 1000, 16).refusal());
        assertNull(ShopPurchasePlan.purchase(poor, offer(1, 2), stock(TEA, 4), 0, 1000, 16).after());
        assertEquals(1, poor.count(ShopInventoryModel.EMERALD));
    }

    @Test
    void taggedEmeraldsAreNotMoneyAndTaggedProductsAreDifferentProducts() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Purity", 1);
        var odd = new ShopStackKey("minecraft:emerald", tag);
        var inventory = inventory(0, odd, 10);
        assertEquals(ShopPurchasePlan.Refusal.PAYMENT, ShopPurchasePlan.purchase(inventory, offer(1, 1), stock(TEA, 4), 0, 1000, 16).refusal());
        var dirtyTea = new ShopStackKey(TEA.item(), tag);
        assertFalse(dirtyTea.equals(TEA));
        assertEquals(ShopPurchasePlan.Refusal.SOLD_OUT, ShopPurchasePlan.purchase(inventory(0, ShopInventoryModel.EMERALD, 4),
                offer(1, 1), stock(dirtyTea, 4), 0, 1000, 16).refusal());
    }

    @Test
    void ownerTransfersConserveEveryItem() {
        var inv = inventory(3, TEA, 10);
        var stock = stock(TEA, 0);
        var deposit = ShopPurchasePlan.deposit(inv, 3, 4, stock);
        assertTrue(deposit.allowed());
        assertEquals(6, deposit.after().count(TEA));
        assertEquals(ShopPurchasePlan.Refusal.AMOUNT, ShopPurchasePlan.deposit(inv, 3, 11, stock).refusal());
        assertEquals(ShopPurchasePlan.Refusal.AMOUNT, ShopPurchasePlan.deposit(inv, 4, 1, stock).refusal());
        ShopStock filled = new ShopStock();
        for (int i = 0; i < ShopStock.SLOTS; i++) assertTrue(filled.insert(DIRT, 64, 64));
        assertEquals(ShopPurchasePlan.Refusal.NO_ROOM, ShopPurchasePlan.deposit(inv, 3, 1, filled).refusal());
        var withdraw = ShopPurchasePlan.withdraw(inventory(), stock(TEA, 9), 0, 9);
        assertTrue(withdraw.allowed());
        assertEquals(9, withdraw.after().count(TEA));
        assertEquals(ShopPurchasePlan.Refusal.INVENTORY_FULL, ShopPurchasePlan.withdraw(full(DIRT), stock(TEA, 9), 0, 1).refusal());
        var proceeds = ShopPurchasePlan.proceeds(inventory(), 200, 130);
        assertTrue(proceeds.allowed());
        assertEquals(130, proceeds.after().count(ShopInventoryModel.EMERALD));
        assertEquals(ShopPurchasePlan.Refusal.AMOUNT, ShopPurchasePlan.proceeds(inventory(), 10, 11).refusal());
    }

    @Test
    void stockStacksRespectLimitsAndOperationsAreAllOrNothing() {
        ShopStock stock = new ShopStock();
        assertTrue(stock.insert(TEA, 20, 16));
        assertEquals(16, stock.slot(0).count());
        assertEquals(4, stock.slot(1).count());
        assertFalse(stock.remove(TEA, 21));
        assertEquals(20, stock.count(TEA));
        assertTrue(stock.remove(TEA, 17));
        assertEquals(3, stock.count(TEA));
        assertTrue(stock.slot(0).empty());
        assertEquals(stock.count(TEA), ShopStock.load(stock.save()).count(TEA));
    }
}
