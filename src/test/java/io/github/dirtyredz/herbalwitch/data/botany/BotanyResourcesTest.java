package io.github.dirtyredz.herbalwitch.data.botany;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest.Herb;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Botany's generated data against {@link BotanyManifest}: loot, recipes, tags, conditions, worldgen, states. */
class BotanyResourcesTest {
    private static final Map<String, Double> DISCOVERY = Map.of("temperate", 1.0 / 32, "dry", 1.0 / 32, "wet", 1.0 / 32,
            "shade", 1.0 / 32, "woodland", 1.0 / 64);
    private static final Map<String, Integer> RARITY = Map.of("temperate", 8, "dry", 8, "wet", 8, "shade", 16, "woodland", 32);

    @Test
    void fourteenDistinctHerbsInFiveGroups() {
        assertEquals(14, BotanyManifest.HERBS.size());
        assertEquals(14, BotanyManifest.HERBS.stream().map(Herb::id).distinct().count());
        for (String group : BotanyManifest.GROUPS) {
            long count = BotanyManifest.HERBS.stream().filter(h -> h.group().equals(group)).count();
            assertTrue(count >= 2 && count < 12, group + " holds " + count);
        }
    }

    @Test
    void breakingDropsOneSeedAndAMaturePlantAlsoTwoFreshHerbs() {
        for (Herb herb : BotanyManifest.HERBS) {
            JsonArray pools = generated("data/herbalwitch/loot_tables/blocks/" + herb.dir() + "/plant.json").getAsJsonArray("pools");
            assertEquals(2, pools.size(), herb.id());
            String seeds = pools.get(0).toString();
            assertTrue(seeds.contains("\"herbalwitch:" + herb.dir() + "/seeds\"") && !seeds.contains("conditions"), seeds);
            JsonObject fresh = pools.get(1).getAsJsonObject();
            assertTrue(fresh.toString().contains("\"herbalwitch:" + herb.dir() + "/fresh\""), herb.id());
            assertTrue(fresh.toString().contains("\"count\":2.0"), fresh.toString());
            assertTrue(fresh.getAsJsonArray("conditions").toString().contains("\"age\":\"3\""), fresh.toString());
        }
    }

    @Test
    void startersCraftFromWheatSeedsAndACompanion() {
        Map<String, String> companions = Map.of("lavender", "minecraft:lilac", "sage", "minecraft:fern",
                "mint", "minecraft:sugar_cane", "chamomile", "minecraft:dandelion");
        companions.forEach((id, companion) -> {
            Herb herb = BotanyManifest.herb(id);
            JsonObject recipe = generated("data/herbalwitch/recipes/" + herb.dir() + "/seeds.json");
            assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
            Set<String> items = new TreeSet<>();
            recipe.getAsJsonArray("ingredients").forEach(e -> items.add(e.getAsJsonObject().get("item").getAsString()));
            assertEquals(Set.of("minecraft:wheat_seeds", companion), items, id);
            assertEquals("herbalwitch:" + herb.dir() + "/seeds", recipe.getAsJsonObject("result").get("item").getAsString());
        });
    }

    @Test
    void herbBedIsThreeSlabsAndTwoDirtForTwo() {
        JsonObject recipe = generated("data/herbalwitch/recipes/" + BotanyManifest.BED + ".json");
        assertEquals("[\"D D\",\"SSS\"]", recipe.getAsJsonArray("pattern").toString());
        assertEquals(2, recipe.getAsJsonObject("result").get("count").getAsInt());
        assertEquals("#minecraft:wooden_slabs", "#" + recipe.getAsJsonObject("key").getAsJsonObject("S").get("tag").getAsString());
    }

    @Test
    void conditionsCarryTheGroupDefaults() {
        for (Herb herb : BotanyManifest.HERBS) {
            JsonObject c = generated("data/herbalwitch/herb_conditions/" + herb.group() + "/" + herb.id() + ".json");
            assertEquals("herbalwitch:" + herb.dir() + "/plant", c.get("herb").getAsString());
            assertEquals("herbalwitch:botany/habitat/" + herb.group(), c.get("habitat").getAsString());
            assertEquals(DISCOVERY.get(herb.group()), c.get("discovery_chance").getAsDouble(), 1e-12, herb.id());
            assertEquals(0.25, c.get("harvest_bonus_chance").getAsDouble(), 1e-12);
            List<String> prefers = new ArrayList<>();
            c.getAsJsonArray("preferences").forEach(e -> prefers.add(e.getAsString()));
            List<String> expected = switch (herb.group()) {
                case "wet" -> List.of("rain");
                case "shade" -> List.of("low_sky");
                case "woodland" -> List.of("night", "full_moon");
                default -> List.of();
            };
            assertEquals(expected, prefers, herb.id());
        }
    }

    @Test
    void worldgenPlacesMaturePatchesInTheGroupHabitat() {
        for (Herb herb : BotanyManifest.HERBS) {
            JsonObject configured = generated("data/herbalwitch/worldgen/configured_feature/" + herb.dir() + ".json");
            JsonObject config = configured.getAsJsonObject("config");
            assertEquals(8, config.get("tries").getAsInt());
            assertEquals(4, config.get("xz_spread").getAsInt());
            String text = config.toString();
            assertTrue(text.contains("\"Name\":\"herbalwitch:" + herb.dir() + "/plant\"") && text.contains("\"age\":\"3\""), text);
            assertTrue(text.contains("\"tag\":\"minecraft:dirt\""), text);
            JsonObject placed = generated("data/herbalwitch/worldgen/placed_feature/" + herb.dir() + ".json");
            assertEquals("herbalwitch:" + herb.dir(), placed.get("feature").getAsString());
            assertTrue(placed.toString().contains("\"chance\":" + RARITY.get(herb.group())), placed.toString());
        }
        for (String group : BotanyManifest.GROUPS) {
            JsonObject modifier = generated("data/herbalwitch/forge/biome_modifier/botany/" + group + ".json");
            assertEquals("forge:add_features", modifier.get("type").getAsString());
            assertEquals("#herbalwitch:botany/habitat/" + group, modifier.get("biomes").getAsString());
            Set<String> features = new TreeSet<>();
            modifier.getAsJsonArray("features").forEach(e -> features.add(e.getAsString()));
            Set<String> expected = new TreeSet<>();
            BotanyManifest.HERBS.stream().filter(h -> h.group().equals(group)).forEach(h -> expected.add("herbalwitch:" + h.dir()));
            assertEquals(expected, features, group);
            assertTrue(generated("data/herbalwitch/tags/worldgen/biome/botany/habitat/" + group + ".json")
                    .getAsJsonArray("values").size() >= 3, group);
        }
    }

    @Test
    void tagsAndForaging() {
        JsonArray plants = generated("data/herbalwitch/tags/blocks/botany/plants.json").getAsJsonArray("values");
        assertEquals(14, plants.size());
        for (String vanilla : List.of("crops", "maintains_farmland")) {
            assertTrue(generated("data/minecraft/tags/blocks/" + vanilla + ".json").toString()
                    .contains("\"#herbalwitch:botany/plants\""), vanilla);
        }
        assertTrue(generated("data/minecraft/tags/blocks/mineable/axe.json").toString().contains("\"herbalwitch:" + BotanyManifest.BED + "\""));
        assertEquals(Set.of("minecraft:grass", "minecraft:tall_grass", "minecraft:fern", "minecraft:large_fern"),
                strings(generated("data/herbalwitch/tags/blocks/botany/forage_sources.json").getAsJsonArray("values")));
        assertEquals(14, generated("data/herbalwitch/tags/items/botany/seeds.json").getAsJsonArray("values").size());
        assertEquals(14, generated("data/herbalwitch/tags/items/botany/fresh_herbs.json").getAsJsonArray("values").size());
        JsonObject forage = generated("data/herbalwitch/loot_modifiers/botany/herb_forage.json");
        assertEquals("herbalwitch:botany/herb_forage", forage.get("type").getAsString());
        assertEquals(1.0 / 16, forage.get("starter_chance").getAsDouble(), 1e-12);
        assertTrue(strings(generated("data/forge/loot_modifiers/global_loot_modifiers.json").getAsJsonArray("entries"))
                .contains("herbalwitch:botany/herb_forage"));
    }

    @Test
    void blockstatesSelectAStageModelPerAgeAndTheBedByMoisture() {
        for (Herb herb : BotanyManifest.HERBS) {
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + herb.dir() + "/plant.json").getAsJsonObject("variants");
            assertEquals(Set.of("age=0", "age=1", "age=2", "age=3"), variants.keySet(), herb.id());
            for (int age = 0; age < 4; age++) {
                assertEquals("herbalwitch:block/" + herb.dir() + "/stage_" + age,
                        variants.getAsJsonObject("age=" + age).get("model").getAsString());
            }
        }
        JsonObject bed = generated("assets/herbalwitch/blockstates/" + BotanyManifest.BED + ".json").getAsJsonObject("variants");
        assertEquals(8, bed.size());
        assertEquals("herbalwitch:block/" + BotanyManifest.BED, bed.getAsJsonObject("moisture=0").get("model").getAsString());
        assertEquals("herbalwitch:block/" + BotanyManifest.BED + "_moist", bed.getAsJsonObject("moisture=7").get("model").getAsString());
    }

    private static Set<String> strings(JsonArray array) {
        Set<String> out = new TreeSet<>();
        for (JsonElement e : array) {
            out.add(e.isJsonObject() ? e.getAsJsonObject().get("id").getAsString() : e.getAsString());
        }
        return out;
    }
}
