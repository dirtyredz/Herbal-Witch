package io.github.dirtyredz.herbalwitch.client.magic.witch;

import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireClientState;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchPackets;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchRulesUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;

import java.util.Optional;

/**
 * The connection-local copy of this player's witch status: the newest snapshot the server sent
 * (older epochs and revisions are ignored) and the server's rules (installed for the grimoire and
 * the altar, which then rebuild their text). A cache, never a store: it is cleared on disconnect
 * and when the local player object is replaced, so another server's or another life's energy is
 * never shown. The client predicts nothing.
 */
public final class WitchClientState implements WitchPackets.ClientHandlers {
    public static final WitchClientState INSTANCE = new WitchClientState();

    private WitchSnapshot snapshot;
    private long epoch = Long.MIN_VALUE;
    private int revision = -1;

    private WitchClientState() {
    }

    /** The newest snapshot, if one arrived since the last reset. */
    public Optional<WitchSnapshot> snapshot() {
        return Optional.ofNullable(snapshot);
    }

    public boolean witch() {
        return snapshot != null && snapshot.initiated();
    }

    @Override
    public void snapshot(WitchSnapshot update) {
        if (snapshot == null || update.newerThan(epoch, revision)) {
            snapshot = update;
            epoch = update.epoch();
            revision = update.revision();
        }
    }

    @Override
    public void rules(WitchRulesUpdate update) {
        WitchRules.applyClient(update.rules());
        GrimoireClientState.INSTANCE.invalidate(); // pages quote the rules
    }

    /** Disconnect or player replacement: forget everything. */
    public void reset() {
        snapshot = null;
        epoch = Long.MIN_VALUE;
        revision = -1;
    }

    /** Disconnect: also forget the server's rules. */
    public void disconnected() {
        reset();
        WitchRules.resetClient();
    }
}
