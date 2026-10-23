package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopCodec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrderRules;

import java.util.UUID;

/**
 * The pure shop-side transitions of a transfer and the conservative recovery decision. A commit and a
 * proven finalization are the same transition, applied once; a rollback needs proof that the player
 * endpoint never received the transfer and still holds exactly the before-state. Missing or
 * inconsistent evidence only ever quarantines: it never replays a grant, payment, refund or release.
 */
public final class ShopRecovery {
    public enum Outcome { FINALIZE, ROLLBACK, QUARANTINE }

    private ShopRecovery() {
    }

    /** The decision for a player transfer given what the player's saved data proves. */
    public static Outcome decide(ShopTransaction t, ShopPlayerCheckpoint.Endpoint endpoint) {
        if (!t.playerEndpoint() || !endpoint.readable() || endpoint.marker() == null) return Outcome.QUARANTINE;
        ShopPlayerReceipt.Marker marker = endpoint.marker();
        if (t.id().equals(marker.transaction()) && marker.sequence() == t.sequence()) return Outcome.FINALIZE;
        if (marker.sequence() == t.sequence() - 1 && !t.id().equals(marker.transaction())) {
            for (ShopTransaction.SlotChange change : t.slots()) {
                if (!change.before().equals(endpoint.slot(change.slot()))) return Outcome.QUARANTINE;
            }
            return Outcome.ROLLBACK;
        }
        return Outcome.QUARANTINE;
    }

    /** The decision for a villager sale: its budget debit is the only other endpoint. */
    public static Outcome decideNpc(ShopTransaction t, boolean debited) {
        if (t.playerEndpoint() || t.kind() != ShopTransaction.Kind.NPC_SALE) return Outcome.QUARANTINE;
        return debited ? Outcome.FINALIZE : Outcome.ROLLBACK;
    }

    /** Applies the transfer's incoming value and settles it: stock in, proceeds in, order progress, receipt, history. */
    public static ShopRecord finalizeTransfer(ShopRecord current, UUID transaction, long time, ShopHistoryEntry.Type historyType) {
        ShopRecord next = current.next();
        ShopTransaction t = take(next, transaction);
        for (ShopTransaction.Goods goods : t.stockIn()) {
            if (!next.stock.insert(goods.key(), goods.count(), goods.limit())) throw new IllegalStateException("Reserved stock room vanished");
        }
        next.proceeds = Math.addExact(next.proceeds, t.proceedsIn());
        if (next.proceeds > ShopCodec.MAX_PROCEEDS) throw new IllegalStateException("Proceeds exceed hard bound");
        if (t.order() != null && t.orderQuantity() > 0) {
            next.order(t.order()).ifPresent(order -> next.orders.set(next.orders.indexOf(order), ShopOrderRules.purchased(order, t.orderQuantity())));
        }
        next.receipt(new ShopRecord.Receipt(t.id(), t.actor(), t.sequence(), false));
        int quantity = t.stockOut().stream().mapToInt(ShopTransaction.Goods::count).sum() + t.stockIn().stream().mapToInt(ShopTransaction.Goods::count).sum();
        String product = !t.stockOut().isEmpty() ? t.stockOut().get(0).key().item() : t.stockIn().isEmpty() ? "" : t.stockIn().get(0).key().item();
        next.record(new ShopHistoryEntry(t.id(), time, t.actor(), t.actorName(), historyType, product, quantity, t.proceedsIn() + t.proceedsOut()));
        release(next);
        return next;
    }

    /** Returns the reserved outgoing value to the shop; the incoming value was never spendable. */
    public static ShopRecord rollback(ShopRecord current, UUID transaction, long time, boolean record) {
        ShopRecord next = current.next();
        ShopTransaction t = take(next, transaction);
        for (ShopTransaction.Goods goods : t.stockOut()) {
            if (!next.stock.insert(goods.key(), goods.count(), goods.limit())) throw new IllegalStateException("Reserved stock cannot return");
        }
        next.proceeds = Math.addExact(next.proceeds, t.proceedsOut());
        next.receipt(new ShopRecord.Receipt(t.id(), t.actor(), t.sequence(), true));
        if (record) next.record(new ShopHistoryEntry(t.id(), time, t.actor(), t.actorName(), ShopHistoryEntry.Type.RECOVERY_ROLLED_BACK, "", 0, 0));
        release(next);
        return next;
    }

    /** Keeps the evidence, blocks the shop and marks the lifecycle for the owner and operators. */
    public static ShopRecord quarantine(ShopRecord current, UUID transaction, String reason) {
        ShopRecord next = current.next();
        ShopTransaction t = next.transaction(transaction).orElseThrow();
        next.transactions.set(next.transactions.indexOf(t), t.quarantine(reason));
        next.lifecycle = ShopRecord.Lifecycle.QUARANTINED;
        return next;
    }

    /** Operator loss acceptance: drops the reservation without granting or refunding, retires the identity. */
    public static ShopRecord discard(ShopRecord current, UUID transaction, UUID operator, long time) {
        ShopRecord next = current.next();
        ShopTransaction t = take(next, transaction);
        next.receipt(new ShopRecord.Receipt(t.id(), t.actor(), t.sequence(), true));
        int lost = t.stockOut().stream().mapToInt(ShopTransaction.Goods::count).sum();
        next.record(new ShopHistoryEntry(t.id(), time, operator, "Operator", ShopHistoryEntry.Type.RECOVERY_DISCARDED,
                t.stockOut().isEmpty() ? "" : t.stockOut().get(0).key().item(), lost, t.proceedsOut()));
        release(next);
        return next;
    }

    private static ShopTransaction take(ShopRecord next, UUID transaction) {
        ShopTransaction t = next.transaction(transaction).orElseThrow(() -> new IllegalStateException("No such transaction"));
        next.transactions.remove(t);
        return t;
    }

    /** A recovered shop returns to closed; the owner reopens it deliberately. */
    private static void release(ShopRecord next) {
        if (next.lifecycle == ShopRecord.Lifecycle.QUARANTINED && next.transactions.isEmpty()) {
            next.lifecycle = next.binding == null ? ShopRecord.Lifecycle.DETACHED : ShopRecord.Lifecycle.CLOSED;
        }
    }
}
