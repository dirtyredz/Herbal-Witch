package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public final class AnimalProvider implements ICapabilitySerializable<CompoundTag> {
    private final AnimalCapability value = new AnimalCapability();
    private final LazyOptional<AnimalCapability> optional = LazyOptional.of(() -> value);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> type, Direction side) { return AnimalCapability.TYPE.orEmpty(type, optional); }
    @Override public CompoundTag serializeNBT() { return value.save(); }
    @Override public void deserializeNBT(CompoundTag tag) { value.load(tag); }
    public void invalidate() { optional.invalidate(); }
}
