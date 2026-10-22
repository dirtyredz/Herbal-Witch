package io.github.dirtyredz.herbalwitch.magic.witch.status;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyRecovery;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * A server player's witch capability, {@code herbalwitch:witch/status}: the durable
 * {@link WitchState} (through {@link WitchCodec}) and transient bookkeeping that is never saved:
 * passive-rest tracking, the cached environmental bonus, whether something disturbed rest this tick,
 * what is recovering, and synchronisation (epoch, revision, last snapshot sent, pending changes).
 * Nothing here is reachable from a packet.
 */
public final class WitchCapability {
    public static final Capability<WitchCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "witch/status");
    private static final Logger LOGGER = LogUtils.getLogger();

    private WitchCodec.Loaded loaded = WitchCodec.read(null);
    private final EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
    private int bonusPercent;
    private long bonusCheckedTick = Long.MIN_VALUE / 2;
    private boolean disturbed;
    private boolean meditating;
    private long epoch;
    private int revision;
    private boolean changed;
    private boolean immediate;
    private long lastSentTick = Long.MIN_VALUE / 2;
    private WitchSnapshot lastSent;

    public static Optional<WitchCapability> of(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public WitchState state() {
        return loaded.state();
    }

    /** Whether the state may change (false when unreadable saved data is preserved). */
    public boolean writable() {
        return loaded.writable();
    }

    public Optional<String> problem() {
        return loaded.problem();
    }

    public CompoundTag save() {
        return WitchCodec.write(loaded);
    }

    public void load(CompoundTag tag) {
        loaded = WitchCodec.read(tag);
        loaded.problem().ifPresent(p -> LOGGER.error("Witch status could not be read ({}); it is kept unchanged, the player is "
                + "treated as a non-witch and nothing about it changes until an operator repairs it", p));
    }

    /** Takes over another player object's durable state, once (death or end-portal cloning). */
    public void copyFrom(WitchCapability other) {
        loaded = other.loaded.copy();
    }

    public EnergyRecovery.Rest rest() {
        return rest;
    }

    /** The environmental bonus, re-evaluated at most once per {@code interval} ticks by {@code evaluate}. */
    public int bonusPercent(long tick, int interval, java.util.function.IntSupplier evaluate) {
        if (tick - bonusCheckedTick >= interval) {
            bonusCheckedTick = tick;
            bonusPercent = evaluate.getAsInt();
        }
        return bonusPercent;
    }

    /** Something broke rest this tick (damage, spending); read and cleared by the rest tick. */
    public void disturb() {
        disturbed = true;
    }

    public boolean takeDisturbed() {
        boolean d = disturbed;
        disturbed = false;
        return d;
    }

    public boolean meditating() {
        return meditating;
    }

    /** Meditation and passive rest exclude each other; the altar session sets this. */
    public void setMeditating(boolean value) {
        meditating = value;
        if (value) {
            rest.reset();
        }
        changed();
    }

    /** Clears every activity (death, logout, dimension change). */
    public void clearActivities() {
        rest.reset();
        meditating = false;
        disturbed = false;
        changed();
    }

    /** The status changed; it is sent within the sync interval, or at once when {@code now}. */
    public void changed(boolean now) {
        changed = true;
        immediate |= now;
    }

    public void changed() {
        changed(false);
    }

    /** A new player object or connection: a fresh epoch, everything is sent again. */
    public void newEpoch(long value) {
        epoch = value;
        revision = 0;
        lastSent = null;
        changed(true);
    }

    public long epoch() {
        return epoch;
    }

    /** Whether a snapshot is due at {@code tick}. */
    boolean due(long tick, int interval) {
        return changed && (immediate || tick - lastSentTick >= interval);
    }

    /** Records a snapshot as sent if it differs from the last one; returns the snapshot to send, or null. */
    WitchSnapshot sent(long tick, WitchSnapshot.Recovering recovering, boolean initiated, int energy, int capacity, int practice,
                       boolean restful) {
        changed = false;
        immediate = false;
        WitchSnapshot candidate = new WitchSnapshot(epoch, revision + 1, initiated, energy, capacity, practice, restful, recovering,
                !writable());
        if (lastSent != null && lastSent.initiated() == initiated && lastSent.energy() == energy && lastSent.capacity() == capacity
                && lastSent.practice() == practice && lastSent.restful() == restful && lastSent.recovering() == recovering
                && lastSent.readOnly() == candidate.readOnly()) {
            return null;
        }
        revision++;
        lastSent = candidate;
        lastSentTick = tick;
        return candidate;
    }
}
