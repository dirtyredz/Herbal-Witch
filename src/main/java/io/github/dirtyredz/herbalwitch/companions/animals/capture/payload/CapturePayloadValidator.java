package io.github.dirtyredz.herbalwitch.companions.animals.capture.payload;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureCodec;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCodec;
import net.minecraft.nbt.*;
import java.util.Set;
import java.util.UUID;

/** Validate before constructing any entity. Unknown capabilities and persistent mod data are never stripped. */
public final class CapturePayloadValidator {
    private CapturePayloadValidator() {}
    public static boolean valid(CompoundTag payload, UUID animal, String type, AnimalRules rules) {
        try {
            if (!payload.hasUUID("UUID") || !payload.getUUID("UUID").equals(animal) || !payload.getString("id").equals(type)
                    || payload.contains("Passengers") || payload.contains("Leash") || !payload.contains("Health", Tag.TAG_ANY_NUMERIC)
                    || payload.getFloat("Health") <= 0 || payload.getFloat("Health") > 1_000_000) return false;
            if (payload.contains("ForgeData") && (!payload.contains("ForgeData", Tag.TAG_COMPOUND) || !payload.getCompound("ForgeData").isEmpty())) return false;
            if (payload.contains("ForgeCaps")) {
                if (!payload.contains("ForgeCaps", Tag.TAG_COMPOUND)) return false;
                CompoundTag caps = payload.getCompound("ForgeCaps");
                if (!Set.of("herbalwitch:animals/care").containsAll(caps.getAllKeys())) return false;
                if (caps.contains("herbalwitch:animals/care") && (!caps.contains("herbalwitch:animals/care", Tag.TAG_COMPOUND)
                        || !AnimalCodec.read(caps.getCompound("herbalwitch:animals/care")).writable())) return false;
            }
            walk(payload, 0, rules.nbtDepth());
            if (payload.contains("Attributes")) {
                if (!(payload.get("Attributes") instanceof ListTag attributes) || !attributes.isEmpty() && attributes.getElementType() != Tag.TAG_COMPOUND) return false;
                for (Tag entry : attributes) {
                    CompoundTag attribute = (CompoundTag) entry;
                    var id = net.minecraft.resources.ResourceLocation.tryParse(attribute.getString("Name"));
                    var definition = id == null ? null : net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(id);
                    if (definition == null || !attribute.contains("Base", Tag.TAG_ANY_NUMERIC)
                            || definition.sanitizeValue(attribute.getDouble("Base")) != attribute.getDouble("Base")) return false;
                }
            }
            CaptureCodec.bytes(payload, rules.payloadBytes());
            return true;
        } catch (Exception failure) { return false; }
    }
    private static void walk(Tag tag, int depth, int maximum) {
        if (depth > maximum) throw new IllegalArgumentException("Capture NBT too deep");
        if (tag instanceof CompoundTag compound) {
            if (compound.size() > 256) throw new IllegalArgumentException("Too many NBT fields");
            for (String key : compound.getAllKeys()) {
                if (key.length() > 256 || key.equals("Passengers") || key.equals("Leash") || key.equals("EntityTag") || depth > 0 && key.equals("ForgeCaps"))
                    throw new IllegalArgumentException("Nested entity, unknown item capability or oversized key");
                walk(compound.get(key), depth + 1, maximum);
            }
            if (compound.contains("Slot", Tag.TAG_ANY_NUMERIC) && (compound.getInt("Slot") < 0 || compound.getInt("Slot") > 255))
                throw new IllegalArgumentException("Invalid inventory slot");
            if (compound.contains("id", Tag.TAG_STRING) && compound.contains("Count")) {
                var id = net.minecraft.resources.ResourceLocation.tryParse(compound.getString("id"));
                if (id == null || !net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(id) || !compound.contains("Count", Tag.TAG_BYTE)
                        || compound.getByte("Count") < 1 || compound.getByte("Count") > 64) throw new IllegalArgumentException("Invalid inventory item");
            }
        } else if (tag instanceof CollectionTag<?> list) {
            if (list.size() > 256) throw new IllegalArgumentException("Capture collection too large");
            for (Tag value : list) walk(value, depth + 1, maximum);
        } else if (tag instanceof StringTag string && string.getAsString().length() > 2048) throw new IllegalArgumentException("Capture text too large");
        else if (tag instanceof NumericTag number && !Double.isFinite(number.getAsDouble())) throw new IllegalArgumentException("Non-finite capture number");
    }
}
