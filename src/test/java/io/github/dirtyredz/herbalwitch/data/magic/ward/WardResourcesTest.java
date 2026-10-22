package io.github.dirtyredz.herbalwitch.data.magic.ward;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ward stone's generated data and compiled art against the independent manifest: one block that
 * drops itself and mines with a pickaxe, turns a base model through the four facings and swaps to
 * its active model while charged, and an item that names the base model.
 */
class WardResourcesTest {
    @Test
    void theStoneDropsItselfAndAPickaxeMinesIt() {
        String text = generated("data/herbalwitch/loot_tables/blocks/" + WardManifest.ID + ".json").toString();
        assertTrue(text.contains("minecraft:survives_explosion"), "explosion condition");
        assertTrue(text.contains("\"name\":\"herbalwitch:" + WardManifest.ID + "\""), text);
        assertTrue(generated("data/minecraft/tags/blocks/mineable/pickaxe.json").toString().contains("herbalwitch:" + WardManifest.ID));
    }

    @Test
    void theBlockstateTurnsBaseOrActiveThroughTheFourFacings() {
        Map<String, Integer> turns = Map.of("north", 0, "east", 90, "south", 180, "west", 270);
        JsonObject variants = generated("assets/herbalwitch/blockstates/" + WardManifest.ID + ".json").getAsJsonObject("variants");
        assertEquals(8, variants.size());
        turns.forEach((facing, y) -> {
            for (boolean active : new boolean[] {false, true}) {
                JsonObject variant = variants.getAsJsonObject("active=" + active + ",facing=" + facing);
                assertEquals("herbalwitch:block/ward/ward_stone/" + (active ? "active" : "base"), variant.get("model").getAsString(), facing);
                assertEquals(y, variant.has("y") ? variant.get("y").getAsInt() : 0, facing + " " + active);
            }
        });
    }

    @Test
    void theCompiledModelsAreAuthoredAndTheItemInheritsTheBase() {
        for (String model : WardManifest.models()) {
            assertTrue(ResourceFiles.MAIN_ASSETS.resolve("models/" + model).toFile().isFile(), model);
            assertTrue(TemplateChecks.checkAuthored("herbalwitch:" + model.replace(".json", "")) > 0, model);
        }
        assertEquals("herbalwitch:block/ward/ward_stone/base", ModelResolver.resolve("herbalwitch:item/" + WardManifest.ID).chain().get(1));
        ItemDisplays.assertInherited("herbalwitch:item/" + WardManifest.ID);
    }
}
