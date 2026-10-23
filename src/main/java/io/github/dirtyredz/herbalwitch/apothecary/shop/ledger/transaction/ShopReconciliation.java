package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopSavedData;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Applies {@link ShopRecovery} decisions to real evidence. At startup every retained transaction of a
 * newly indexed shard is decided from the saved player file (or the villager budget) before the shop
 * can trade. A player's pending receipt is resolved only from data proven durable: data just loaded at
 * login, or data checkpointed and verified now. Unproven cases stay quarantined for an operator.
 */
public final class ShopReconciliation {
    private static BiPredicate<MinecraftServer, UUID> npcDebited = (server, transaction) -> false;

    private ShopReconciliation() {
    }

    /** Installed by commerce registration: whether a villager-sale budget debit is durably recorded. */
    public static void npcEvidence(BiPredicate<MinecraftServer, UUID> evidence) {
        npcDebited = evidence;
    }

    /** Startup: reconcile a freshly loaded shard before it is usable. */
    public static void startup(MinecraftServer server, ShopStore store, ShopSavedData shard) {
        ShopRecord record = shard.record();
        if (record == null || record.transactions.isEmpty()) return;
        for (var t : List.copyOf(record.transactions)) {
            ShopRecovery.Outcome outcome = t.playerEndpoint()
                    ? ShopRecovery.decide(t, ShopPlayerCheckpoint.saved(server, t.actor()))
                    : ShopRecovery.decideNpc(t, npcDebited.test(server, t.id()));
            ShopRecord current = shard.record();
            if (!store.reconcile(shard, apply(current, t.id(), outcome, server.overworld().getGameTime(), "restart"))) return;
        }
    }

    static ShopRecord apply(ShopRecord current, UUID transaction, ShopRecovery.Outcome outcome, long time, String reason) {
        var t = current.transaction(transaction).orElseThrow();
        return switch (outcome) {
            case FINALIZE -> ShopRecovery.finalizeTransfer(current, transaction, time, ShopTransfer.historyType(t.kind()));
            case ROLLBACK -> ShopRecovery.rollback(current, transaction, time, true);
            case QUARANTINE -> ShopRecovery.quarantine(current, transaction, reason);
        };
    }

    /**
     * Resolves a player's pending receipt. {@code fresh}: the live data was just loaded from disk;
     * otherwise the player is checkpointed and verified first, so the decision rests on durable data.
     * Returns the outcome applied, or empty when nothing could be proven.
     */
    public static Optional<ShopRecovery.Outcome> player(ServerPlayer player, boolean fresh) {
        ShopPlayerReceipt.Marker marker = ShopPlayerReceipt.of(player).orElse(null);
        if (marker == null || !marker.pending() || marker.shop() == null || marker.transaction() == null) return Optional.empty();
        ShopStore store = ShopStore.get(player.server);
        if (!store.ready()) return Optional.empty();
        UUID shop = marker.shop();
        if (store.problem(shop).isPresent()) return Optional.empty(); // an unreadable shard may hold the transaction
        ShopRecord record = store.record(shop).orElse(null);
        var t = record == null ? null : record.transaction(marker.transaction()).orElse(null);
        if (t == null) {
            // Not outstanding on the shop side (committed, discarded or never prepared there): nothing to replay.
            ShopPlayerReceipt.settle(player, marker.transaction());
            return Optional.of(ShopRecovery.Outcome.FINALIZE);
        }
        if (!fresh && !ShopPlayerCheckpoint.save(player)) return Optional.empty();
        ShopRecovery.Outcome outcome = ShopRecovery.decide(t, ShopPlayerCheckpoint.live(player));
        if (outcome == ShopRecovery.Outcome.QUARANTINE && t.phase() == ShopTransaction.Phase.QUARANTINED) return Optional.empty();
        if (!store.update(apply(record, t.id(), outcome, player.serverLevel().getGameTime(), "player_evidence"), true)) return Optional.empty();
        if (outcome == ShopRecovery.Outcome.FINALIZE) ShopPlayerReceipt.settle(player, t.id());
        return Optional.of(outcome);
    }

    /** Operator reconcile: proven endpoint state only. Online players are checkpointed first. */
    public static Optional<ShopRecovery.Outcome> operator(MinecraftServer server, UUID shop, UUID transaction) {
        ShopStore store = ShopStore.get(server);
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !store.writable(shop)) return Optional.empty();
        var t = record.transaction(transaction).orElse(null);
        if (t == null) return Optional.empty();
        if (!t.playerEndpoint()) {
            var outcome = ShopRecovery.decideNpc(t, npcDebited.test(server, t.id()));
            return store.update(apply(record, transaction, outcome, server.overworld().getGameTime(), "operator"), true) ? Optional.of(outcome) : Optional.empty();
        }
        ServerPlayer online = server.getPlayerList().getPlayer(t.actor());
        if (online != null) return player(online, false);
        var outcome = ShopRecovery.decide(t, ShopPlayerCheckpoint.saved(server, t.actor()));
        if (outcome == ShopRecovery.Outcome.QUARANTINE) return Optional.of(outcome);
        return store.update(apply(record, transaction, outcome, server.overworld().getGameTime(), "operator"), true) ? Optional.of(outcome) : Optional.empty();
    }

    /** Operator discard: the shop-side reservation is lost and the identity retired; nothing is granted. */
    public static boolean discard(MinecraftServer server, UUID shop, UUID transaction, UUID operator) {
        ShopStore store = ShopStore.get(server);
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !store.writable(shop) || record.transaction(transaction).isEmpty()) return false;
        var t = record.transaction(transaction).orElseThrow();
        if (!store.update(ShopRecovery.discard(record, transaction, operator, server.overworld().getGameTime()), true)) return false;
        ServerPlayer online = t.playerEndpoint() ? server.getPlayerList().getPlayer(t.actor()) : null;
        if (online != null) ShopPlayerReceipt.settle(online, transaction);
        return true;
    }

    /** Login: data was just read from disk. */
    public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) player(player, true);
    }
}
