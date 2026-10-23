package io.github.dirtyredz.herbalwitch.companions.familiars.species;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.payload.CapturePayloadValidator;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarBinding;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import java.util.Optional;

public final class FamiliarSpeciesAdapters {
    private FamiliarSpeciesAdapters() {}
    public static Optional<CompoundTag> snapshot(Mob mob) {
        var species = FamiliarCatalog.type(EntityType.getKey(mob.getType()).toString()).orElse(null);
        if (species == null) return Optional.empty();
        var tag = new CompoundTag(); mob.saveWithoutId(tag); tag.putString("id", EntityType.getKey(mob.getType()).toString());
        var validation = tag.copy();
        var caps = validation.getCompound("ForgeCaps");
        if (caps.contains(FamiliarBinding.ID.toString())) {
            var binding = new FamiliarBinding(); binding.load(caps.getCompound(FamiliarBinding.ID.toString()));
            if (!binding.valid() || !caps.contains(FamiliarBinding.ID.toString(), 10)) return Optional.empty();
            caps.remove(FamiliarBinding.ID.toString());
        }
        if (!CapturePayloadValidator.valid(validation, mob.getUUID(), tag.getString("id"), FamiliarCodec.MAX_BYTES - 4096, 14)) return Optional.empty();
        return Optional.of(tag);
    }
    public static Mob restore(ServerLevel level, FamiliarRecord r) {
        var payload = r.payload();
        if (!payload.hasUUID("UUID") || !r.creature().equals(payload.getUUID("UUID"))) return null;
        Mob candidate = FamiliarEntities.type(r.species()).create(level);
        if (candidate == null) return null;
        // Binding reference changes with each incarnation, while care/transport state stays untouched.
        payload.getCompound("ForgeCaps").remove(FamiliarBinding.ID.toString());
        candidate.load(payload);
        var binding = FamiliarBinding.of(candidate).orElse(null);
        if (binding == null || !binding.empty()) { candidate.discard(); return null; }
        binding.bind(r.bond(), r.owner(), r.generation());
        candidate.setPersistenceRequired(); candidate.setCanPickUpLoot(false); candidate.setTarget(null);
        candidate.goalSelector.removeAllGoals(goal -> true); candidate.targetSelector.removeAllGoals(goal -> true);
        if (candidate instanceof TamableAnimal pet) { pet.setOrderedToSit(false); pet.setInSittingPose(false); }
        return candidate;
    }
}
