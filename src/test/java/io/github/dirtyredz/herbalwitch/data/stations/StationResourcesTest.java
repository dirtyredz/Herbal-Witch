package io.github.dirtyredz.herbalwitch.data.stations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.BlockstateModels;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TextureDensity;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import io.github.dirtyredz.herbalwitch.data.magic.charm.CharmManifest;
import io.github.dirtyredz.herbalwitch.data.magic.ritual.CircleManifest;
import io.github.dirtyredz.herbalwitch.data.magic.ward.WardManifest;
import io.github.dirtyredz.herbalwitch.data.preparations.AdvancedManifest;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The stations' generated data and authored art: assembly recipes, the processing matrix, models, GUIs, heat tags. */
class StationResourcesTest {
    private static final Map<String, Integer> DURATIONS = Map.of("drying", 1200, "grinding", 100, "steeping", 200, "infusion", 600,
            "cauldron", 600, "distillation", 800, "bottling", 100, "charm_work", 200, "broom_work", 200, "wand_carving", 100);
    /** One-block stations that show a second model while active (the others never change shape). */
    private static final Set<String> ACTIVE_ONE_BLOCK = Set.of("cauldron");
    private static final String WATER_BOTTLE = "Potion:\\\"minecraft:water\\\"";
    private static final String GLASS_BOTTLE_STACK = "{\"count\":1,\"ingredient\":{\"item\":\"minecraft:glass_bottle\"}}";
    private static final String BOTTLE_RETURN ="[{\"item\":\"minecraft:glass_bottle\"}]";

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

    /** The four Milestone 7 stations, the ritual circle and the ward stone are assembled shapeless from these ingredients. */
    @Test
    void witchBlocksAssembleShapelessFromThePlannedIngredients() {
        Map<String, Map<String, Integer>> expected = Map.of(
                StationManifest.station("cauldron"), Map.of("minecraft:cauldron", 1, "minecraft:copper_ingot", 1, "minecraft:amethyst_shard", 1),
                StationManifest.station("still"), Map.of("minecraft:copper_ingot", 4, "minecraft:glass", 2, "minecraft:iron_ingot", 1,
                        "minecraft:amethyst_shard", 1),
                StationManifest.station("bottling_bench"), Map.of("minecraft:crafting_table", 1, "minecraft:dark_oak_planks", 2,
                        "minecraft:glass_bottle", 2, "minecraft:iron_nugget", 1),
                StationManifest.station("charm_workbench"), Map.of("minecraft:crafting_table", 1, "minecraft:dark_oak_planks", 2,
                        "minecraft:string", 2, "minecraft:gold_ingot", 1, "minecraft:amethyst_shard", 1),
                CircleManifest.ID, Map.of("minecraft:smooth_stone_slab", 4, "minecraft:calcite", 2, "minecraft:amethyst_shard", 1,
                        "minecraft:gold_nugget", 1),
                WardManifest.ID, Map.of("minecraft:stone_bricks", 4, "minecraft:amethyst_shard", 1, "minecraft:gold_ingot", 1));
        expected.forEach((id, counts) -> {
            JsonObject recipe = generated("data/herbalwitch/recipes/" + id + ".json");
            assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString(), id);
            Map<String, Integer> actual = new HashMap<>();
            for (JsonElement ingredient : recipe.getAsJsonArray("ingredients")) {
                actual.merge(ingredient.getAsJsonObject().get("item").getAsString(), 1, Integer::sum);
            }
            assertEquals(counts, actual, id);
            assertEquals("herbalwitch:" + id, recipe.getAsJsonObject("result").get("item").getAsString(), id);
        });
    }

    /** Ordinary recipes carry no requirements; a witch recipe carries exactly {witch: true, energy: N} from the manifest. */
    private static void assertRequirements(String path, JsonObject recipe) {
        Integer energy = AdvancedManifest.WITCH_ENERGY.get(path.substring("processing/".length()));
        if (path.startsWith("processing/charm_work/")) {
            energy = CharmManifest.ENERGY.get(path.substring("processing/charm_work/".length()));
        }
        if (path.equals("processing/animals/charm_work/capture_charm")) energy = 10;
        if (path.startsWith("processing/charm_work/familiars/equipment/")) energy = 5;
        if (path.equals("processing/broom_work/craft")) energy = 10;
        if (path.equals("processing/broom_work/repair") || path.equals("processing/wand_carving/craft")) energy = 5;
        if (energy == null) {
            assertFalse(recipe.has("requirements"), path + " is ordinary work");
            return;
        }
        JsonObject requirements = recipe.getAsJsonObject("requirements");
        assertEquals(Set.of("witch", "energy"), requirements.keySet(), path);
        assertTrue(requirements.get("witch").getAsBoolean(), path);
        assertEquals(energy.intValue(), requirements.get("energy").getAsInt(), path);
    }

    @Test
    void processingRecipesAreTheFullMatrixWithTheirDurationsAndContainers() {
        Set<String> actual = new TreeSet<>();
        generatedFiles().stream().filter(f -> f.startsWith("data/herbalwitch/recipes/processing/"))
                .forEach(f -> actual.add(f.substring("data/herbalwitch/recipes/".length(), f.length() - 5)));
        assertEquals(StationManifest.processingRecipes(), actual);
        for (String path : actual) {
            String type = path.split("/")[path.startsWith("processing/animals/") ? 2 : 1];
            JsonObject recipe = generated("data/herbalwitch/recipes/" + path + ".json");
            assertEquals("herbalwitch:processing/" + type, recipe.get("type").getAsString(), path);
            assertEquals(path.equals("processing/broom_work/repair") ? 100 : DURATIONS.get(type), recipe.get("duration").getAsInt(), path);
            switch (type) {
                case "steeping" -> {
                    assertTrue(recipe.getAsJsonObject("base").toString().contains(WATER_BOTTLE), path);
                    assertTrue(!recipe.has("returns") && !recipe.has("packaging"), path + ": the water bottle becomes the drink");
                }
                case "infusion" -> {
                    assertTrue(recipe.has("base") && recipe.has("packaging"), path);
                    assertEquals(BOTTLE_RETURN, recipe.getAsJsonArray("returns").toString(), path);
                }
                case "cauldron" -> {
                    assertTrue(recipe.getAsJsonObject("base").toString().contains(WATER_BOTTLE), path + ": brewed from a water bottle");
                    assertFalse(recipe.has("packaging"), path + ": the mixture is not poured into anything yet");
                    assertEquals(BOTTLE_RETURN, recipe.getAsJsonArray("returns").toString(), path);
                    assertTrue(recipe.getAsJsonArray("ingredients").size() <= 3, path);
                }
                case "distillation" -> {
                    // the venomleaf extract is distilled from plant oil, the other essences from water
                    String base = recipe.getAsJsonObject("base").toString();
                    assertTrue(path.endsWith("/venomleaf") ? base.contains("preparations/ingredients/plant_oil") : base.contains(WATER_BOTTLE), path);
                    assertEquals(GLASS_BOTTLE_STACK, recipe.getAsJsonObject("packaging").toString(), path);
                    assertEquals(BOTTLE_RETURN, recipe.getAsJsonArray("returns").toString(), path);
                }
                case "bottling" -> {
                    assertFalse(recipe.has("base"), path);
                    assertEquals("{\"count\":4,\"ingredient\":{\"item\":\"minecraft:glass_bottle\"}}", recipe.getAsJsonObject("packaging").toString(), path);
                    assertEquals(4, recipe.getAsJsonObject("result").get("count").getAsInt(), path + ": a mixture is four doses");
                    assertFalse(recipe.has("returns"), path);
                }
                case "broom_work" -> {
                    assertEquals(path.endsWith("/repair"), recipe.has("base"));
                    assertFalse(recipe.has("packaging") || recipe.has("returns"));
                }
                case "charm_work" -> {
                    assertTrue(!recipe.has("base") && !recipe.has("packaging") && !recipe.has("returns"), path);
                    assertEquals(3, recipe.getAsJsonArray("ingredients").size(), path);
                }
                default -> assertTrue(!recipe.has("base") && !recipe.has("packaging"), path);
            }
            assertRequirements(path, recipe);
        }
    }

    /**
     * Bottling is ordinary work: every mixture has a plain and a labelled recipe (labelled takes one paper more and
     * labels the result), a vial mixture also takes gunpowder, and each bottles the product its mixture is named for.
     */
    @Test
    void bottlingTurnsEachMixtureIntoItsProductPlainOrLabelled() {
        AdvancedManifest.BOTTLED.forEach((mixture, product) -> {
            for (boolean labelled : new boolean[] {false, true}) {
                String path = "processing/bottling/" + mixture + "/" + (labelled ? "labelled" : "plain");
                JsonObject recipe = generated("data/herbalwitch/recipes/" + path + ".json");
                List<String> inputs = new ArrayList<>();
                recipe.getAsJsonArray("ingredients").forEach(e -> inputs.add(e.getAsJsonObject().getAsJsonObject("ingredient").get("item").getAsString()));
                List<String> expected = new ArrayList<>(List.of("herbalwitch:preparations/mixtures/" + mixture));
                if (AdvancedManifest.THROWN.contains(mixture)) {
                    expected.add("minecraft:gunpowder");
                }
                if (labelled) {
                    expected.add("minecraft:paper");
                }
                assertEquals(expected, inputs, path);
                JsonObject result = recipe.getAsJsonObject("result");
                assertEquals("herbalwitch:preparations/" + product, result.get("item").getAsString(), path);
                assertEquals(labelled, result.has("nbt") && result.get("nbt").getAsString().contains("HerbalWitchLabel"), path);
            }
        });
    }

    @Test
    void noTwoRecipesOfATypeShareTheirInputs() {
        Map<String, String> seen = new HashMap<>();
        for (String path : StationManifest.processingRecipes()) {
            JsonObject recipe = generated("data/herbalwitch/recipes/" + path + ".json");
            List<String> inputs = new ArrayList<>();
            recipe.getAsJsonArray("ingredients").forEach(e -> inputs.add(e.getAsJsonObject().get("ingredient").toString()));
            inputs.sort(null);
            String key = recipe.get("type") + inputs.toString() + recipe.get("base") + recipe.get("packaging");
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
            String dir = "herbalwitch:block/stations/" + station + "/";
            if (ACTIVE_ONE_BLOCK.contains(station)) {
                // a working station swaps to its active model, turned to the same facing
                assertEquals(Set.of(dir + "base", dir + "active"), models, station);
                assertEquals(8, variants.size(), station);
                variants.entrySet().forEach(v -> assertEquals(dir + (v.getKey().contains("active=true") ? "active" : "base"),
                        v.getValue().getAsJsonObject().get("model").getAsString(), station + " " + v.getKey()));
            } else {
                assertEquals(Set.of(dir + "base"), models, station);
            }
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
            // the state an overlay shows on and its model prefix: bundles while drying, fire while lit, "active" for the witch stations
            String shown = StationManifest.SHOWN.get(station);
            String overlay = StationManifest.OVERLAYS.get(station);
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
                        boolean hasOverlay = java.nio.file.Files.isRegularFile(ModelResolver.modelFile(dir + overlay + part));
                        if (!on || !hasOverlay) {
                            assertEquals(1, applied.size(), what + " shows an overlay it should not");
                        } else {
                            assertEquals(2, applied.size(), what + " lacks its overlay");
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

    /** The witch stations drop themselves (a multiblock only from its origin cell) and mine with the tool of their material. */
    @Test
    void witchStationsDropThemselvesAndMineWithTheirTool() {
        String axe = generated("data/minecraft/tags/blocks/mineable/axe.json").getAsJsonArray("values").toString();
        String pickaxe = generated("data/minecraft/tags/blocks/mineable/pickaxe.json").getAsJsonArray("values").toString();
        for (String station : List.of("cauldron", "still", "bottling_bench", "charm_workbench")) {
            String id = StationManifest.station(station);
            String loot = generated("data/herbalwitch/loot_tables/blocks/" + id + ".json").toString();
            assertTrue(loot.contains("\"name\":\"herbalwitch:" + id + "\""), station);
            assertTrue(loot.contains("minecraft:survives_explosion"), station);
            assertEquals(StationManifest.MULTIBLOCKS.containsKey(station), loot.contains("\"part\":\"0\""), station + " drops from its origin only");
            boolean wooden = station.endsWith("_bench") || station.endsWith("_workbench");
            assertTrue((wooden ? axe : pickaxe).contains("\"herbalwitch:" + id + "\""), station + " mines with the tool of its material");
            assertFalse((wooden ? pickaxe : axe).contains("\"herbalwitch:" + id + "\""), station);
        }
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
