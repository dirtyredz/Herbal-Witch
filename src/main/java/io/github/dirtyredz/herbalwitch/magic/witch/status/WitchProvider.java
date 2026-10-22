package io.github.dirtyredz.herbalwitch.magic.witch.status;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Attaches one {@link WitchCapability} to a server player and saves it with the player. The lazy
 * optional is invalidated with the player; cloning revives the old player's capabilities first
 * ({@link WitchLifecycleEvents}).
 */
public final class WitchProvider implements ICapabilitySerializable<CompoundTag> {
    private final WitchCapability capability = new WitchCapability();
    private final LazyOptional<WitchCapability> optional = LazyOptional.of(() -> capability);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return WitchCapability.CAPABILITY.orEmpty(cap, optional);
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
