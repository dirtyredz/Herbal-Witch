package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * A server player's grimoire capability, {@code herbalwitch:grimoire/discoveries}: the durable
 * {@link DiscoveryState} (persisted through {@link DiscoveryCodec}) and transient bookkeeping that is
 * never saved: the observation fingerprint that lets an unchanged inventory skip recipe evaluation.
 * Knowledge belongs to the player, never to a book item.
 */
public final class DiscoveryCapability {
    public static final Capability<DiscoveryCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "grimoire/discoveries");
    private static final Logger LOGGER = LogUtils.getLogger();

    private DiscoveryCodec.Loaded loaded = DiscoveryCodec.read(null);
    private long observedFingerprint;
    private int observedContentRevision = -1;

    public static Optional<DiscoveryCapability> of(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public DiscoveryState state() {
        return loaded.state();
    }

    public DiscoveryState.Grant grant(EntryKey key) {
        return loaded.state().grant(key);
    }

    public boolean knows(EntryKey key) {
        return loaded.state().knows(key);
    }

    public CompoundTag save() {
        return DiscoveryCodec.write(loaded);
    }

    public void load(CompoundTag tag) {
        loaded = DiscoveryCodec.read(tag);
        loaded.problem().ifPresent(p -> LOGGER.error("Grimoire discoveries could not be read ({}); they are kept unchanged "
                + "and no new discoveries are recorded for this player", p));
        forgetObservation();
    }

    /** Takes over another capability's knowledge (death or end-portal cloning). */
    public void copyFrom(DiscoveryCapability other) {
        loaded = new DiscoveryCodec.Loaded(other.loaded.state().copy(), other.loaded.preserved().map(CompoundTag::copy),
                other.loaded.problem());
        forgetObservation();
    }

    /** Whether the inventory and content are unchanged since the last observation. */
    boolean observedAlready(long fingerprint, int contentRevision) {
        return fingerprint == observedFingerprint && contentRevision == observedContentRevision;
    }

    void markObserved(long fingerprint, int contentRevision) {
        observedFingerprint = fingerprint;
        observedContentRevision = contentRevision;
    }

    /** The next observation evaluates every recipe again. */
    public void forgetObservation() {
        observedContentRevision = -1;
    }
}
