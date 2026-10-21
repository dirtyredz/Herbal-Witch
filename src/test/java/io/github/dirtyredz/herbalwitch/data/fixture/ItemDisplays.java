package io.github.dirtyredz.herbalwitch.data.fixture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Item display transforms of the Blockbench pieces: the item model only names its parent, and the
 * parent (compiled from the piece's project) carries a complete set of display contexts. The numbers
 * themselves are the owner's to change in Blockbench, so only their shape is checked here; the
 * migration fixtures under tools/blockbench/test/ keep the historical values.
 */
public final class ItemDisplays {
    /** The contexts every piece sets; the others fall back to vanilla's defaults or the right hand. */
    public static final Set<String> CONTEXTS = Set.of("gui", "ground", "fixed", "thirdperson_righthand",
            "firstperson_righthand", "firstperson_lefthand");

    private ItemDisplays() {
    }

    /** Asserts that {@code item} inherits complete, sane display transforms and overrides none itself. */
    public static void assertInherited(String item) {
        JsonObject own = ResourceFiles.json(ModelResolver.modelFile(item));
        assertFalse(own.has("display"), item + ": display transforms belong to the Blockbench project, not the item model");
        assertEquals(Set.of("parent"), own.keySet(), item + ": a parent-only item model");
        ModelResolver.Resolved resolved = ModelResolver.resolve(item);
        assertTrue(resolved.display().keySet().containsAll(CONTEXTS), item + ": inherits " + resolved.display().keySet());
        for (Map.Entry<String, JsonElement> context : resolved.display().entrySet()) {
            JsonObject t = context.getValue().getAsJsonObject();
            String what = item + " " + context.getKey();
            for (String key : t.keySet()) {
                assertTrue(Set.of("rotation", "translation", "scale").contains(key), what + ": " + key);
                JsonArray v = t.getAsJsonArray(key);
                assertEquals(3, v.size(), what + " " + key);
                for (JsonElement n : v) {
                    assertTrue(Double.isFinite(n.getAsDouble()), what + " " + key);
                }
            }
            if (t.has("scale")) {
                for (JsonElement n : t.getAsJsonArray("scale")) {
                    assertTrue(n.getAsDouble() > 0 && n.getAsDouble() <= 4, what + ": scale " + n);
                }
            }
            if (t.has("translation")) {
                for (JsonElement n : t.getAsJsonArray("translation")) {
                    assertTrue(Math.abs(n.getAsDouble()) <= 80, what + ": translation " + n);
                }
            }
        }
    }
}
