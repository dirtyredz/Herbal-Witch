package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Attaches one {@link DiscoveryCapability} to a server player and saves it with the player. The
 * lazy optional is invalidated with the player; cloning revives the old player's capabilities
 * first ({@link DiscoveryEvents}).
 */
public final class DiscoveryProvider implements ICapabilitySerializable<CompoundTag> {
    private final DiscoveryCapability capability = new DiscoveryCapability();
    private LazyOptional<DiscoveryCapability> optional = LazyOptional.of(() -> capability);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        // The owner dispatcher gates invalid entities; a revived player needs a fresh handle,
        // not a new discovery record or the permanently invalid old LazyOptional.
        if (cap == DiscoveryCapability.CAPABILITY && !optional.isPresent()) optional = LazyOptional.of(() -> capability);
        return DiscoveryCapability.CAPABILITY.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return capability.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        capability.load(tag);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
