package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The one commerce transfer path (deposits, stock withdrawals, purchases, proceeds, villager sales),
 * on the server thread, in five durable steps:
 * <ol>
 * <li><b>Prepare:</b> checkpoint the shop with the transaction, its outgoing value reserved and its
 * incoming value not yet spendable.</li>
 * <li><b>Apply:</b> change the player's known slots and attach the receipt in the same step.</li>
 * <li><b>Checkpoint the player:</b> write, force, read back and verify the actual player data.</li>
 * <li><b>Commit:</b> checkpoint the shop with the incoming value credited and the receipt settled.</li>
 * <li><b>Publish:</b> the caller refreshes projections and answers the client.</li>
 * </ol>
 * A failure before a successful prepare changes nothing; a failure before apply rolls back only after
 * the rollback checkpoint succeeds; a failed player checkpoint quarantines. A villager sale replaces
 * steps 2-3 with its durable budget debit.
 */
public final class ShopTransfer {
    public enum Status { SUCCESS, REFUSED, UNAVAILABLE, RECOVERY }

    public record Result(Status status, UUID transaction, String reason) {
        public static Result refused(String reason) {
            return new Result(Status.REFUSED, null, reason);
        }

        public boolean success() {
            return status == Status.SUCCESS;
        }
    }

    /**
     * Everything one transfer moves, derived by the server from current state.
     *
     * @param slots player slots with exact before/after item NBT (empty for a villager sale)
     */
    public record Plan(ShopTransaction.Kind kind, UUID offer, List<ShopTransaction.SlotChange> slots, List<ShopTransaction.Goods> stockOut,
                       List<ShopTransaction.Goods> stockIn, long proceedsOut, long proceedsIn, UUID order, int orderQuantity) {
        public Plan {
            slots = List.copyOf(slots);
            stockOut = List.copyOf(stockOut);
            stockIn = List.copyOf(stockIn);
        }
    }

    private ShopTransfer() {
    }

    public static ShopHistoryEntry.Type historyType(ShopTransaction.Kind kind) {
        return switch (kind) {
            case PURCHASE -> ShopHistoryEntry.Type.SALE;
            case NPC_SALE -> ShopHistoryEntry.Type.NPC_SALE;
            case DEPOSIT -> ShopHistoryEntry.Type.DEPOSIT;
            case WITHDRAW -> ShopHistoryEntry.Type.WITHDRAW;
            case PROCEEDS -> ShopHistoryEntry.Type.PROCEEDS;
        };
    }

    /** Prepared record: the transaction attached, outgoing value reserved. Throws when the plan does not fit. */
    static ShopRecord prepare(ShopRecord current, ShopTransaction t) {
        ShopRecord next = current.next();
        for (ShopTransaction.Goods goods : t.stockOut()) {
            if (!next.stock.remove(goods.key(), goods.count())) throw new IllegalStateException("Stock changed");
        }
        if (t.proceedsOut() > next.proceeds) throw new IllegalStateException("Proceeds changed");
        next.proceeds -= t.proceedsOut();
        // Incoming stock must fit after the reservation; nothing else can change the shop while it is blocked.
        var probe = next.stock.copy();
        for (ShopTransaction.Goods goods : t.stockIn()) {
            if (!probe.insert(goods.key(), goods.count(), goods.limit())) throw new IllegalStateException("No room for incoming stock");
        }
        next.transactions.add(t);
        return next;
    }

    /** A player transfer. The caller has validated session, access and the plan against current state this tick. */
    public static Result player(ServerPlayer player, ShopStore store, UUID shop, long expectedRevision, Plan plan) {
        ShopRecord current = store.record(shop).orElse(null);
        if (current == null || !store.writable(shop)) return Result.refused("unavailable");
        if (current.revision != expectedRevision) return Result.refused("stale");
        if (current.blocked()) return Result.refused("recovery");
        ShopPlayerReceipt.Marker marker = ShopPlayerReceipt.of(player).orElse(null);
        if (marker == null || marker.pending()) return Result.refused("receipt_pending");
        for (ShopTransaction.SlotChange change : plan.slots()) {
            if (!ItemStack.matches(player.getInventory().items.get(change.slot()), ItemStack.of(change.before()))) return Result.refused("inventory_changed");
        }
        long sequence = Math.addExact(marker.sequence(), 1);
        long time = player.serverLevel().getGameTime();
        ShopTransaction t = new ShopTransaction(UUID.randomUUID(), plan.kind(), ShopTransaction.Phase.PREPARED, player.getUUID(),
                bounded(player.getGameProfile().getName()), true, sequence, plan.slots(), plan.stockOut(), plan.stockIn(),
                plan.proceedsOut(), plan.proceedsIn(), plan.order(), plan.orderQuantity(), plan.offer(), time, "");
        ShopRecord prepared;
        try {
            prepared = prepare(current, t);
        } catch (IllegalStateException | ArithmeticException changed) {
            return Result.refused("stale");
        }
        // 1. Prepare.
        if (!store.update(prepared, true)) {
            return store.writable(shop) ? Result.refused("bounds") : new Result(Status.UNAVAILABLE, t.id(), "prepare_failed");
        }
        // 2. Apply: inventory and receipt in one server-thread step.
        for (ShopTransaction.SlotChange change : plan.slots()) player.getInventory().setItem(change.slot(), ItemStack.of(change.after()));
        ShopPlayerReceipt.set(player, new ShopPlayerReceipt.Marker(sequence, t.id(), shop, true));
        player.getInventory().setChanged();
        // 3. Player checkpoint.
        if (!ShopPlayerCheckpoint.save(player)) {
            ShopRecord now = store.record(shop).orElseThrow();
            store.update(ShopRecovery.quarantine(now, t.id(), "player_checkpoint"), true);
            return new Result(Status.RECOVERY, t.id(), "player_checkpoint");
        }
        // 4. Commit.
        return commit(store, shop, t, time, () -> ShopPlayerReceipt.settle(player, t.id()));
    }

    /**
     * A villager sale: prepare, then the caller's durable budget debit, then commit. A failed debit
     * rolls back (checkpointed); an uncertain one keeps the transaction for restart reconciliation.
     */
    public static Result villager(ShopStore store, UUID shop, UUID villager, long time, Plan plan, Predicate<UUID> debit) {
        ShopRecord current = store.record(shop).orElse(null);
        if (current == null || !store.writable(shop)) return Result.refused("unavailable");
        if (current.blocked()) return Result.refused("recovery");
        ShopTransaction t = new ShopTransaction(UUID.randomUUID(), ShopTransaction.Kind.NPC_SALE, ShopTransaction.Phase.PREPARED, villager,
                "Villager", false, 0, List.of(), plan.stockOut(), plan.stockIn(), plan.proceedsOut(), plan.proceedsIn(), null, 0, plan.offer(), time, "");
        ShopRecord prepared;
        try {
            prepared = prepare(current, t);
        } catch (IllegalStateException | ArithmeticException changed) {
            return Result.refused("stale");
        }
        if (!store.update(prepared, true)) {
            return store.writable(shop) ? Result.refused("bounds") : new Result(Status.UNAVAILABLE, t.id(), "prepare_failed");
        }
        if (!debit.test(t.id())) {
            // The budget refused or could not be written: never sell; return the reservation if provable.
            ShopRecord now = store.record(shop).orElseThrow();
            store.update(ShopRecovery.rollback(now, t.id(), time, false), true);
            return Result.refused("budget");
        }
        return commit(store, shop, t, time, () -> { });
    }

    private static Result commit(ShopStore store, UUID shop, ShopTransaction t, long time, Runnable settled) {
        ShopRecord now = store.record(shop).orElseThrow();
        ShopRecord committed;
        try {
            committed = ShopRecovery.finalizeTransfer(now, t.id(), time, historyType(t.kind()));
        } catch (IllegalStateException | ArithmeticException impossible) {
            store.update(ShopRecovery.quarantine(now, t.id(), "commit_invalid"), true);
            return new Result(Status.RECOVERY, t.id(), "commit_invalid");
        }
        if (!store.update(committed, true)) return new Result(Status.RECOVERY, t.id(), "commit_failed");
        settled.run();
        return new Result(Status.SUCCESS, t.id(), "");
    }

    static String bounded(String name) {
        return name.length() > 64 ? name.substring(0, 64) : name;
    }
}
