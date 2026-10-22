package io.github.dirtyredz.herbalwitch.data.magic.ritual;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.BlockstateModels;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The altar's generated data and compiled art against the independent manifest: its shaped
 * acquisition recipe, origin-only loot surviving explosions, axe mining, every cell in every facing
 * and active state, and an item that only names the compiled assembly.
 */
class AltarResourcesTest {
    private static final String[] FACINGS = {"north", "east", "south", "west"};

    @Test
    void theAltarCraftsFromThePlannedIngredients() {
        JsonObject recipe = generated("data/herbalwitch/recipes/" + AltarManifest.ID + ".json");
        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        List<String> pattern = new ArrayList<>();
        recipe.getAsJsonArray("pattern").forEach(r -> pattern.add(r.getAsString()));
        assertEquals(List.of("SAS", "LGL", "L L"), pattern);
        Map<String, Integer> counts = new HashMap<>();
        JsonObject key = recipe.getAsJsonObject("key");
        for (String row : pattern) {
            for (char c : row.toCharArray()) {
                if (c != ' ') {
                    JsonObject k = key.getAsJsonObject(String.valueOf(c));
                    counts.merge(k.has("tag") ? "#" + k.get("tag").getAsString() : k.get("item").getAsString(), 1, Integer::sum);
                }
            }
        }
        assertEquals(Map.of("minecraft:smooth_stone_slab", 2, "minecraft:amethyst_shard", 1, "minecraft:dark_oak_log", 4,
                "#forge:ingots/gold", 1), counts);
        assertEquals("herbalwitch:" + AltarManifest.ID, recipe.getAsJsonObject("result").get("item").getAsString());
        assertEquals(1, recipe.getAsJsonObject("result").has("count") ? recipe.getAsJsonObject("result").get("count").getAsInt() : 1);
    }

    @Test
    void onlyTheOriginDropsTheAltar() {
        JsonObject loot = generated("data/herbalwitch/loot_tables/blocks/" + AltarManifest.ID + ".json");
        String text = loot.toString();
        assertTrue(text.contains("\"part\":\"0\""), "origin-only condition: " + text);
        assertTrue(text.contains("minecraft:survives_explosion"), "explosion condition");
        assertTrue(text.contains("herbalwitch:" + AltarManifest.ID), text);
        JsonObject axe = generated("data/minecraft/tags/blocks/mineable/axe.json");
        assertTrue(axe.toString().contains("herbalwitch:" + AltarManifest.ID));
    }

    /** Every facing, cell and active state shows exactly its part (and its overlay while active), turned to the facing. */
    @Test
    void everyCellFacingAndActiveStateComposes() {
        JsonObject states = generated("assets/herbalwitch/blockstates/" + AltarManifest.ID + ".json");
        for (int f = 0; f < FACINGS.length; f++) {
            int y = f * 90;
            for (int part = 0; part < 4; part++) {
                for (boolean active : new boolean[] {false, true}) {
                    List<BlockstateModels.Applied> applied = BlockstateModels.select(states,
                            Map.of("facing", FACINGS[f], "part", String.valueOf(part), "active", String.valueOf(active)));
                    List<BlockstateModels.Applied> expected = new ArrayList<>();
                    expected.add(new BlockstateModels.Applied("herbalwitch:block/ritual/altar/part_" + part, y));
                    if (active && AltarManifest.ACTIVE_CELLS.contains(part)) {
                        expected.add(new BlockstateModels.Applied("herbalwitch:block/ritual/altar/active_" + part, y));
                    }
                    assertEquals(expected, applied, FACINGS[f] + " part " + part + " active " + active);
                }
            }
        }
    }

    @Test
    void theCompiledModelsAreAuthoredAndTheItemInheritsTheAssembly() {
        for (String model : AltarManifest.models()) {
            String id = "herbalwitch:" + model.replace(".json", "");
            assertTrue(ResourceFiles.MAIN_ASSETS.resolve("models/" + model).toFile().isFile(), model);
            if (model.endsWith("assembly.json")) {
                TemplateChecks.checkAuthored(id, new double[] {-16, 0, 0}, new double[] {16, 32, 16});
            } else {
                TemplateChecks.checkAuthored(id, new double[] {0, 0, 0}, new double[] {16, 17, 16});
            }
        }
        JsonObject item = generated("assets/herbalwitch/models/item/" + AltarManifest.ID + ".json");
        assertEquals("herbalwitch:block/ritual/altar/assembly", item.get("parent").getAsString());
        ItemDisplays.assertInherited("herbalwitch:item/" + AltarManifest.ID);
    }

    @Test
    void theActiveOverlayGlows() {
        for (int cell : AltarManifest.ACTIVE_CELLS) {
            JsonObject overlay = ResourceFiles.json(ResourceFiles.MAIN_ASSETS.resolve("models/block/ritual/altar/active_" + cell + ".json"));
            boolean glows = false;
            for (JsonElement e : overlay.getAsJsonArray("elements")) {
                JsonObject el = e.getAsJsonObject();
                glows |= el.has("forge_data") && el.getAsJsonObject("forge_data").has("block_light");
            }
            assertTrue(glows, "active_" + cell + " has emissive elements");
        }
    }
}
