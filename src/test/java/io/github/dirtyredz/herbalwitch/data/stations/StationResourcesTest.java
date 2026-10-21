package io.github.dirtyredz.herbalwitch.data.stations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.BlockstateModels;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TextureDensity;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generatedFiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The stations' generated data and authored art: assembly recipes, the processing matrix, models, GUIs, heat tags. */
class StationResourcesTest {
    private static final Map<String, Integer> DURATIONS = Map.of("drying", 1200, "grinding", 100, "steeping", 200, "infusion", 600);

    @Test
    void stationsAssembleFromThePlannedIngredients() {
        Map<String, Map<String, Integer>> expected = Map.of(
                "drying_rack", Map.of("#forge:rods/wooden", 4, "#forge:string", 2, "#minecraft:wooden_slabs", 2),
                "mortar", Map.of("minecraft:smooth_stone", 3, "minecraft:flint", 1, "#forge:rods/wooden", 1),
                "kettle", Map.of("#forge:ingots/iron", 5, "#forge:ingots/copper", 1),
                "hearth", Map.of("minecraft:brick", 6, "minecraft:campfire", 1),
                "stove", Map.of("#forge:ingots/iron", 5, "minecraft:campfire", 1, "minecraft:brick", 2),
                "infusion_pot", Map.of("minecraft:brick", 4, "#forge:ingots/iron", 2, "minecraft:glass_bottle", 1));
        expected.forEach((station, counts) -> {
            JsonObject recipe = generated("data/herbalwitch/recipes/" + StationManifest.station(station) + ".json");
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), station);
            Map<String, Integer> actual = new HashMap<>();
            JsonObject key = recipe.getAsJsonObject("key");
            for (JsonElement row : recipe.getAsJsonArray("pattern")) {
                for (char c : row.getAsString().toCharArray()) {
                    if (c != ' ') {
                        JsonObject k = key.getAsJsonObject(String.valueOf(c));
                        actual.merge(k.has("tag") ? "#" + k.get("tag").getAsString() : k.get("item").getAsString(), 1, Integer::sum);
                    }
                }
            }
            assertEquals(counts, actual, station);
            assertEquals("herbalwitch:" + StationManifest.station(station), recipe.getAsJsonObject("result").get("item").getAsString());
        });
    }

    @Test
    void processingRecipesAreTheFullMatrixWithTheirDurationsAndContainers() {
        Set<String> actual = new TreeSet<>();
        generatedFiles().stream().filter(f -> f.startsWith("data/herbalwitch/recipes/processing/"))
                .forEach(f -> actual.add(f.substring("data/herbalwitch/recipes/".length(), f.length() - 5)));
        assertEquals(StationManifest.processingRecipes(), actual);
        for (String path : actual) {
            String type = path.split("/")[1];
            JsonObject recipe = generated("data/herbalwitch/recipes/" + path + ".json");
            assertEquals("herbalwitch:processing/" + type, recipe.get("type").getAsString(), path);
            assertEquals(DURATIONS.get(type), recipe.get("duration").getAsInt(), path);
            switch (type) {
                case "steeping" -> {
                    assertTrue(recipe.getAsJsonObject("base").toString().contains("Potion:\\\"minecraft:water\\\""), path);
                    assertTrue(!recipe.has("returns") && !recipe.has("packaging"), path + ": the water bottle becomes the drink");
                }
                case "infusion" -> {
                    assertTrue(recipe.has("base") && recipe.has("packaging"), path);
                    assertEquals("[{\"item\":\"minecraft:glass_bottle\"}]", recipe.getAsJsonArray("returns").toString(), path);
                }
                default -> assertTrue(!recipe.has("base") && !recipe.has("packaging"), path);
            }
        }
    }

    @Test
    void noTwoRecipesOfATypeShareTheirInputs() {
        Map<String, String> seen = new HashMap<>();
        for (String path : StationManifest.processingRecipes()) {
            JsonObject recipe = generated("data/herbalwitch/recipes/" + path + ".json");
            List<String> inputs = new ArrayList<>();
            recipe.getAsJsonArray("ingredients").forEach(e -> inputs.add(e.getAsJsonObject().get("ingredient").toString()));
            inputs.sort(null);
            String key = path.split("/")[1] + inputs + recipe.get("base") + recipe.get("packaging");
            assertNull(seen.put(key, path), "ambiguous recipes: " + path + " and " + seen.get(key));
        }
    }

    @Test
    void oneBlockStationsTurnTheirBaseModelToEachFacing() {
        for (String station : StationManifest.STATIONS.keySet()) {
            if (StationManifest.MULTIBLOCKS.containsKey(station)) {
                continue;
            }
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + StationManifest.station(station) + ".json")
                    .getAsJsonObject("variants");
            Set<String> models = new TreeSet<>();
            variants.entrySet().forEach(v -> models.add(v.getValue().getAsJsonObject().get("model").getAsString()));
            assertEquals(Set.of("herbalwitch:block/stations/" + station + "/base"), models, station);
            models.forEach(m -> assertTrue(TemplateChecks.checkAuthored(m) >= 6, m));
            assertEquals("herbalwitch:block/stations/" + station + "/base",
                    ModelResolver.resolve("herbalwitch:item/" + StationManifest.station(station)).chain().get(1));
            ItemDisplays.assertInherited("herbalwitch:item/" + StationManifest.station(station));
        }
    }

    /**
     * A multiblock station shows, for every facing and cell, exactly its permanent part turned to the
     * facing, plus its overlay (fire or bundles) only while lit or active; every model keeps the
     * template contracts; the item shows the whole assembly.
     */
    @Test
    void multiblockStationsComposeCellsWithOverlaysInEveryFacing() {
        Map<String, Integer> turns = Map.of("north", 0, "east", 90, "south", 180, "west", 270);
        StationManifest.MULTIBLOCKS.forEach((station, cells) -> {
            String dir = "herbalwitch:block/stations/" + station + "/";
            String shown = station.equals("drying_rack") ? "active" : "lit";
            String overlay = station.equals("drying_rack") ? "bundles_" : "fire_";
            JsonObject states = generated("assets/herbalwitch/blockstates/" + StationManifest.station(station) + ".json");
            assertTrue(states.has("multipart"), station);
            for (int part = 0; part < cells; part++) {
                for (Map.Entry<String, Integer> facing : turns.entrySet()) {
                    for (boolean on : new boolean[] {false, true}) {
                        List<BlockstateModels.Applied> applied = BlockstateModels.select(states,
                                Map.of("facing", facing.getKey(), "part", String.valueOf(part), shown, String.valueOf(on)));
                        String what = station + " part " + part + " " + facing.getKey() + " " + shown + "=" + on;
                        assertEquals(dir + "part_" + part, applied.get(0).model(), what);
                        applied.forEach(a -> assertEquals(facing.getValue(), a.y(), what));
                        assertTrue(applied.size() <= 2, what);
                        if (!on) {
                            assertEquals(1, applied.size(), what + " shows its overlay");
                        } else if (applied.size() == 2) {
                            assertEquals(dir + overlay + part, applied.get(1).model(), what);
                        }
                    }
                }
                // the stove's pipe rises above its cell; every other cell stays inside its block
                double top = station.equals("stove") ? 32 : 16;
                TemplateChecks.checkAuthored(dir + "part_" + part, new double[] {0, 0, 0}, new double[] {16, top, 16});
            }
            int overlays = 0;
            for (int part = 0; part < cells; part++) {
                if (java.nio.file.Files.isRegularFile(ModelResolver.modelFile(dir + overlay + part))) {
                    TemplateChecks.checkAuthored(dir + overlay + part, new double[] {0, 0, 0}, new double[] {16, 17, 16});
                    overlays++;
                }
            }
            assertTrue(overlays > 0, station + " has no overlay");
            assertEquals(dir + "assembly", ModelResolver.resolve("herbalwitch:item/" + StationManifest.station(station)).chain().get(1));
            ItemDisplays.assertInherited("herbalwitch:item/" + StationManifest.station(station));
            TemplateChecks.checkAuthored(dir + "assembly", new double[] {-16, 0, 0}, new double[] {16, 32, 16});
        });
    }

    @Test
    void everyStationHasAGuiAtlasAtTheSetDensity() {
        // vanilla 256-unit container sheet (the screens blit in those units) at two texels per GUI pixel
        for (String station : StationManifest.STATIONS.keySet()) {
            if (station.equals("stove")) {
                continue; // the stove opens the hearth's menu and screen
            }
            int[] size = ResourceFiles.pngSize(ResourceFiles.MAIN_ASSETS.resolve("textures/gui/stations/" + station + ".png"));
            assertEquals(TextureDensity.GUI_SIZE, size[0], station);
            assertEquals(TextureDensity.GUI_SIZE, size[1], station);
        }
    }

    @Test
    void heatComesFromLitSourcesAndOneConductor() {
        assertEquals("[\"herbalwitch:stations/hearth/station\",\"herbalwitch:stations/stove/station\",\"minecraft:campfire\",\"minecraft:soul_campfire\","
                        + "\"minecraft:magma_block\",{\"id\":\"farmersdelight:stove\",\"required\":false}]",
                generated("data/herbalwitch/tags/blocks/heat_sources.json").getAsJsonArray("values").toString());
        assertEquals("[\"minecraft:hopper\",{\"id\":\"#farmersdelight:heat_conductors\",\"required\":false}]",
                generated("data/herbalwitch/tags/blocks/heat_conductors.json").getAsJsonArray("values").toString());
        assertEquals("[\"herbalwitch:stations/hearth/station\",\"herbalwitch:stations/stove/station\"]",
                generated("data/farmersdelight/tags/blocks/heat_sources.json").getAsJsonArray("values").toString());
    }
}
