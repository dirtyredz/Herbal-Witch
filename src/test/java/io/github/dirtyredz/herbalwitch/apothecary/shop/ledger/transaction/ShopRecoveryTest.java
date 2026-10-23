package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopBinding;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrder;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrderRules;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Conservative recovery: only proven endpoints finalize or roll back, and each transition applies once. */
class ShopRecoveryTest {
    static final ShopStackKey TEA = ShopStackKey.of("herbalwitch:preparations/drinks/recovery_tea");
    final UUID buyer = UUID.randomUUID();

    static CompoundTag item(String id, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putByte("Count", (byte) count);
        return tag;
    }

    ShopRecord shop() {
        ShopRecord r = new ShopRecord(UUID.randomUUID(), UUID.randomUUID(), "Owner", "Shop", 0);
        r.generation = 1;
        r.binding = new ShopBinding("minecraft:overworld", 1L, UUID.randomUUID(), 1);
        r.stock.insert(TEA, 5, 16);
        r.offers.add(new ShopOffer(UUID.randomUUID(), 1, TEA, 1, 2, true));
        return r;
    }

    ShopTransaction purchase(long sequence, UUID order, int orderQuantity) {
        return new ShopTransaction(UUID.randomUUID(), ShopTransaction.Kind.PURCHASE, ShopTransaction.Phase.PREPARED, buyer, "Buyer", true, sequence,
                List.of(new ShopTransaction.SlotChange(0, item("minecraft:emerald", 2), new CompoundTag()),
                        new ShopTransaction.SlotChange(1, new CompoundTag(), item(TEA.item(), 1))),
                List.of(new ShopTransaction.Goods(TEA, 1, 16)), List.of(), 0, 2, order, orderQuantity, null, 0, "");
    }

    static ShopPlayerCheckpoint.Endpoint endpoint(long sequence, UUID transaction, Map<Integer, CompoundTag> slots) {
        return new ShopPlayerCheckpoint.Endpoint(true, new ShopPlayerReceipt.Marker(sequence, transaction, null, transaction != null), slots);
    }

    @Test
    void prepareReservesOutgoingValueAndNeverCreditsIncomingValue() {
        ShopRecord shop = shop();
        ShopTransaction t = purchase(4, null, 0);
        ShopRecord prepared = ShopTransfer.prepare(shop, t);
        assertEquals(4, prepared.stock.count(TEA), "the lot is reserved and cannot be sold twice");
        assertEquals(0, prepared.proceeds, "payment is not spendable before the commit");
        assertTrue(prepared.blocked());
        ShopRecord stale = shop();
        stale.stock.remove(TEA, 5);
        assertThrows(IllegalStateException.class, () -> ShopTransfer.prepare(stale, t));
    }

    @Test
    void finalizeAppliesOnceAndConservesTheLot() {
        ShopRecord shop = shop();
        ShopTransaction t = purchase(4, null, 0);
        ShopRecord prepared = ShopTransfer.prepare(shop, t);
        ShopRecord done = ShopRecovery.finalizeTransfer(prepared, t.id(), 9, ShopHistoryEntry.Type.SALE);
        assertEquals(4, done.stock.count(TEA));
        assertEquals(2, done.proceeds);
        assertFalse(done.blocked());
        assertTrue(done.settled(t.id()));
        assertThrows(IllegalStateException.class, () -> ShopRecovery.finalizeTransfer(done, t.id(), 9, ShopHistoryEntry.Type.SALE),
                "a settled transaction cannot be finalized again");
        assertThrows(IllegalStateException.class, () -> ShopRecovery.rollback(done, t.id(), 9, true));
    }

    @Test
    void rollbackReturnsOnlyTheReservation() {
        ShopRecord prepared = ShopTransfer.prepare(shop(), purchase(4, null, 0));
        ShopRecord back = ShopRecovery.rollback(prepared, prepared.transactions.get(0).id(), 9, true);
        assertEquals(5, back.stock.count(TEA));
        assertEquals(0, back.proceeds);
        assertFalse(back.blocked());
    }

    @Test
    void decisionsRequireProof() {
        ShopTransaction t = purchase(4, null, 0);
        assertEquals(ShopRecovery.Outcome.FINALIZE, ShopRecovery.decide(t, endpoint(4, t.id(), Map.of())));
        assertEquals(ShopRecovery.Outcome.ROLLBACK, ShopRecovery.decide(t, endpoint(3, UUID.randomUUID(), Map.of(0, item("minecraft:emerald", 2)))));
        assertEquals(ShopRecovery.Outcome.QUARANTINE, ShopRecovery.decide(t, endpoint(3, UUID.randomUUID(), Map.of(0, item("minecraft:emerald", 1)))),
                "a changed before-state is not proof of an untouched endpoint");
        assertEquals(ShopRecovery.Outcome.QUARANTINE, ShopRecovery.decide(t, endpoint(2, UUID.randomUUID(), Map.of(0, item("minecraft:emerald", 2)))),
                "an older player file proves nothing");
        assertEquals(ShopRecovery.Outcome.QUARANTINE, ShopRecovery.decide(t, endpoint(5, UUID.randomUUID(), Map.of())),
                "a newer unrelated receipt proves nothing");
        assertEquals(ShopRecovery.Outcome.QUARANTINE, ShopRecovery.decide(t, ShopPlayerCheckpoint.Endpoint.UNREADABLE));
        ShopTransaction npc = new ShopTransaction(UUID.randomUUID(), ShopTransaction.Kind.NPC_SALE, ShopTransaction.Phase.PREPARED, UUID.randomUUID(),
                "Villager", false, 0, List.of(), List.of(new ShopTransaction.Goods(TEA, 1, 16)), List.of(), 0, 1, null, 0, null, 0, "");
        assertEquals(ShopRecovery.Outcome.FINALIZE, ShopRecovery.decideNpc(npc, true));
        assertEquals(ShopRecovery.Outcome.ROLLBACK, ShopRecovery.decideNpc(npc, false), "no debit: no emerald was created, the stock returns");
        assertEquals(ShopRecovery.Outcome.QUARANTINE, ShopRecovery.decideNpc(t, true));
    }

    @Test
    void quarantineBlocksAndDiscardLosesTheReservationWithoutRefund() {
        ShopRecord prepared = ShopTransfer.prepare(shop(), purchase(4, null, 0));
        UUID id = prepared.transactions.get(0).id();
        ShopRecord held = ShopRecovery.quarantine(prepared, id, "player_checkpoint");
        assertEquals(ShopRecord.Lifecycle.QUARANTINED, held.lifecycle);
        assertTrue(held.blocked());
        ShopRecord lost = ShopRecovery.discard(held, id, UUID.randomUUID(), 3);
        assertEquals(4, lost.stock.count(TEA), "the reserved lot is not returned");
        assertEquals(0, lost.proceeds, "the payment is not credited");
        assertEquals(ShopRecord.Lifecycle.CLOSED, lost.lifecycle);
        assertTrue(lost.settled(id), "the identity is retired");
        assertEquals(ShopHistoryEntry.Type.RECOVERY_DISCARDED, lost.history.peekLast().type());
    }

    @Test
    void anAssociatedPurchaseAdvancesItsRequestInTheSameTransition() {
        ShopRecord shop = shop();
        ShopOffer offer = shop.offers.get(0);
        ShopOrder order = ShopOrderRules.associate(ShopOrderRules.create(UUID.randomUUID(), buyer, "Buyer", TEA.item(), ShopOrder.Requirement.ANY, 2, 2, 0, 100), offer);
        shop.orders.add(order);
        ShopTransaction t = purchase(4, order.id(), 1);
        ShopRecord done = ShopRecovery.finalizeTransfer(ShopTransfer.prepare(shop, t), t.id(), 1, ShopHistoryEntry.Type.SALE);
        assertEquals(1, done.order(order.id()).orElseThrow().purchased());
        assertEquals(ShopOrder.State.OFFERED, done.order(order.id()).orElseThrow().state());
        ShopTransaction second = purchase(5, order.id(), 1);
        ShopRecord twice = ShopRecovery.finalizeTransfer(ShopTransfer.prepare(done, second), second.id(), 2, ShopHistoryEntry.Type.SALE);
        assertEquals(ShopOrder.State.FULFILLED, twice.order(order.id()).orElseThrow().state());
    }
}
