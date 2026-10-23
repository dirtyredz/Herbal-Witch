package io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

public final class FamiliarBindingProvider implements ICapabilitySerializable<CompoundTag> {
    private final FamiliarBinding value = new FamiliarBinding();
    private final LazyOptional<FamiliarBinding> optional = LazyOptional.of(() -> value);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> type, Direction side) { return FamiliarBinding.TYPE.orEmpty(type, optional); }
    @Override public CompoundTag serializeNBT() { return value.save(); }
    @Override public void deserializeNBT(CompoundTag tag) { value.load(tag); }
    public void invalidate() { optional.invalidate(); }
}
