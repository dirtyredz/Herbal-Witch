package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.payload.CapturePayloadValidator;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import net.minecraft.nbt.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

class CapturePayloadTest {
    static final UUID ANIMAL = new UUID(0, 1);
    static CompoundTag payload() { var tag = new CompoundTag(); tag.putUUID("UUID", ANIMAL); tag.putString("id", "minecraft:cow"); tag.putFloat("Health", 10); return tag; }
    static boolean valid(CompoundTag tag) { return CapturePayloadValidator.valid(tag, ANIMAL, "minecraft:cow", AnimalRules.DEFAULTS); }
    static void rejects(Consumer<CompoundTag> edit) { var tag = payload(); edit.accept(tag); assertFalse(valid(tag)); }
    @Test void boundedOrdinaryPayloadIsAcceptedWithoutMutation() { var tag = payload(); var copy = tag.copy(); assertTrue(valid(tag)); assertEquals(copy, tag); }
    @Test void unknownCapabilitiesPersistentDataNestedEntitiesAndIdentityChangesAreRefused() {
        rejects(t -> { var caps = new CompoundTag(); caps.putInt("unknown:inventory", 1); t.put("ForgeCaps", caps); });
        rejects(t -> { var data = new CompoundTag(); data.putString("other:external", "bond"); t.put("ForgeData", data); });
        rejects(t -> t.put("Passengers", new ListTag())); rejects(t -> t.put("Leash", new CompoundTag()));
        rejects(t -> t.putUUID("UUID", new UUID(9, 9))); rejects(t -> t.putString("id", "minecraft:wolf"));
    }
    @Test void nonFiniteNumbersAndOversizedCollectionsDepthAndStringsAreRefused() {
        rejects(t -> t.putFloat("Health", Float.NaN)); rejects(t -> t.putDouble("attribute", Double.POSITIVE_INFINITY));
        rejects(t -> t.putString("CustomName", "x".repeat(2049)));
        rejects(t -> { var list = new ListTag(); for (int i = 0; i < 257; i++) list.add(IntTag.valueOf(i)); t.put("Items", list); });
        rejects(t -> { CompoundTag current = t; for (int i = 0; i < 18; i++) { var next = new CompoundTag(); current.put("nested", next); current = next; } });
    }
}
