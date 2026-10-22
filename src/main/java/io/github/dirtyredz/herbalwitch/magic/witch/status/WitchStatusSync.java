package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchPackets;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchRulesUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.server.level.ServerPlayer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Epochs, pending snapshots and private rules emission; durable mutation belongs to the service. */
final class WitchStatusSync {
    private static final AtomicLong EPOCHS = new AtomicLong();

    private WitchStatusSync() { }

    // ---------------------------------------------------------------- synchronisation

    /** Login, respawn or a dimension change: a new epoch and a full snapshot now. */
    static void resync(ServerPlayer player) {
        WitchCapability.of(player).ifPresent(c -> c.newEpoch(EPOCHS.incrementAndGet()));
        flush(player);
    }

    /** Sends the snapshot now if anything changed. */
    static void flush(ServerPlayer player) {
        WitchCapability.of(player).ifPresent(c -> c.changed(true));
        tick(player);
    }

    /** Sends a pending snapshot when due (every server tick per player). */
    static void tick(ServerPlayer player) {
        Optional<WitchCapability> cap = WitchCapability.of(player);
        if (cap.isEmpty() || player.getServer() == null) {
            return;
        }
        WitchCapability c = cap.get();
        long now = player.getServer().getTickCount();
        if (!c.due(now, WitchStatusService.SYNC_INTERVAL)) {
            return;
        }
        WitchState s = c.state();
        WitchSnapshot.Recovering recovering = c.meditating() ? WitchSnapshot.Recovering.MEDITATION
                : c.rest().resting() && s.initiated() ? WitchSnapshot.Recovering.REST : WitchSnapshot.Recovering.NONE;
        WitchSnapshot snapshot = c.sent(now, recovering, s.initiated(), s.energyUnits(), EnergyService.capacityUnits(s, WitchRules.server().energy()),
                s.practiceSpent(), s.restfulTicks() > 0);
        if (snapshot != null) {
            WitchPackets.send(player, snapshot);
        }
    }

    static void sendRules(ServerPlayer player) {
        WitchPackets.send(player, new WitchRulesUpdate(WitchRules.server()));
    }
}
