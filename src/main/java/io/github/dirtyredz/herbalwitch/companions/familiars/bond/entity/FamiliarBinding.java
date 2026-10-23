package io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.*;
import java.util.*;

/** Entity-side reference only. An unknown schema is retained and cannot gain gameplay authority. */
public final class FamiliarBinding {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("herbalwitch", "familiars/binding");
    public static final Capability<FamiliarBinding> TYPE = CapabilityManager.get(new CapabilityToken<>() {});
    public record Reference(UUID bond, UUID owner, long generation) {}
    private CompoundTag stored = new CompoundTag();
    private Reference reference;
    public static Optional<FamiliarBinding> of(Mob mob) { return mob.getCapability(TYPE).resolve(); }
    public Optional<Reference> reference() { return Optional.ofNullable(reference); }
    public boolean empty() { return stored.isEmpty(); }
    public boolean valid() { return empty() || reference != null; }
    public void bind(UUID bond, UUID owner, long generation) {
        if (!empty() || generation < 1) throw new IllegalStateException("Familiar reference already assigned");
        stored.putInt("schema", 1); stored.putUUID("bond", bond); stored.putUUID("owner", owner); stored.putLong("generation", generation);
        reference = new Reference(bond, owner, generation);
    }
    public CompoundTag save() { return stored.copy(); }
    public void load(CompoundTag tag) {
        stored = tag.copy(); reference = null;
        if (tag.getAllKeys().equals(Set.of("schema", "bond", "owner", "generation")) && tag.getInt("schema") == 1
                && tag.hasUUID("bond") && tag.hasUUID("owner") && tag.contains("generation", 4) && tag.getLong("generation") > 0)
            reference = new Reference(tag.getUUID("bond"), tag.getUUID("owner"), tag.getLong("generation"));
    }
}
