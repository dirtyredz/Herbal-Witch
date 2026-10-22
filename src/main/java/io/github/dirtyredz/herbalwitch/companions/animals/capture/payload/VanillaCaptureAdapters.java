package io.github.dirtyredz.herbalwitch.companions.animals.capture.payload;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;

/** Exact vanilla adapters only. Full native serialization preserves inventories, variants and M7 effects. */
public final class VanillaCaptureAdapters {
    private VanillaCaptureAdapters() {}
    public static boolean supported(Mob mob) {
        var profile = AnimalEligibility.profile(mob);
        if (profile.isEmpty() || !profile.get().capturable() || !mob.getType().is(AnimalTags.CAPTURABLE)) return false;
        // A modded subclass sharing a vanilla registry type has no tested serialization contract.
        return mob.getClass().getName().startsWith("net.minecraft.world.entity.")
                && AnimalProfiles.find(EntityType.getKey(mob.getType()).toString()).isPresent();
    }
    public static CompoundTag snapshot(Mob mob) {
        if (!supported(mob)) return null;
        CompoundTag tag = new CompoundTag(); mob.saveWithoutId(tag);
        String type = EntityType.getKey(mob.getType()).toString(); tag.putString("id", type);
        return CapturePayloadValidator.valid(tag, mob.getUUID(), type, AnimalServerConfig.current()) ? tag : null;
    }
    public static Mob restore(ServerLevel level, CaptureRecord record) {
        if (record.adapter() != 1 || !record.storedPayload() || !CapturePayloadValidator.valid(record.payload(), record.animal(), record.type(), AnimalServerConfig.current())) return null;
        var id = ResourceLocation.tryParse(record.type());
        if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) return null;
        var type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null || !(type.create(level) instanceof Mob candidate)) return null;
        if (!supported(candidate)) { candidate.discard(); return null; }
        try {
            candidate.load(record.payload());
            if (!candidate.getUUID().equals(record.animal()) || !candidate.isAlive() || !supported(candidate)
                    || candidate.getHealth() != record.payload().getFloat("Health") || !EntityType.getKey(candidate.getType()).equals(id)) { candidate.discard(); return null; }
            candidate.setTarget(null); candidate.getNavigation().stop(); return candidate;
        } catch (RuntimeException failure) { candidate.discard(); return null; }
    }
}
