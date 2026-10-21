package io.github.dirtyredz.herbalwitch.data.decor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.PIECES;
import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.ids;
import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.pieceOf;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.GENERATED;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The decor feature's generated data: names, loot, crafting and restyling, tags and blockstates,
 * against {@link DecorManifest}. The whole-tree manifest lives in GeneratedResourcesTest.
 */
class DecorResourcesTest {
    @Test
    void ninetyDistinctIds() {
        assertEquals(90, ids().size());
        assertEquals(90, new TreeSet<>(ids()).size());
    }

    @Test
    void everyDecorKeyHasItsEnglishName() {
        JsonObject lang = json(GENERATED.resolve("assets/herbalwitch/lang/en_us.json"));
        DecorManifest.lang().forEach((key, value) ->
                assertEquals(value, lang.has(key) ? lang.get(key).getAsString() : null, key));
    }

    @Test
    void everyBlockDropsItself() {
        for (String id : ids()) {
            String loot = json(GENERATED.resolve("data/herbalwitch/loot_tables/blocks/" + id + ".json")).toString();
            assertTrue(loot.contains("\"name\":\"herbalwitch:" + id + "\""), id + " drops " + loot);
            assertTrue(loot.contains("minecraft:survives_explosion"), id);
        }
    }

    @Test
    void restylingListsEveryOtherFinishOfTheSamePieceOnly() {
        for (String id : ids()) {
            JsonObject recipe = json(GENERATED.resolve("data/herbalwitch/recipes/" + id + "_from_restyling.json"));
            assertEquals("minecraft:stonecutting", recipe.get("type").getAsString(), id);
            assertEquals("herbalwitch:" + id, recipe.get("result").getAsString(), id);
            assertEquals(1, recipe.get("count").getAsInt(), id);
            Set<String> sources = new TreeSet<>();
            for (JsonElement e : recipe.getAsJsonArray("ingredient")) {
                sources.add(e.getAsJsonObject().get("item").getAsString());
            }
            Set<String> expected = new TreeSet<>();
            ids().stream().filter(o -> pieceOf(o).equals(pieceOf(id)) && !o.equals(id)).forEach(o -> expected.add("herbalwitch:" + o));
            assertEquals(expected, sources, id);
        }
    }

    @Test
    void baseRecipesCraftTheDarkOakAppearance() {
        Map<String, Integer> counts = Map.of("armchair", 1, "apothecary_counter", 1, "small_wall_shelf", 2,
                "table_lamp", 1, "small_rug", 2);
        PIECES.forEach((piece, family) -> {
            String id = "decor/" + family + "/" + piece + "/natural/dark_oak";
            JsonObject recipe = json(GENERATED.resolve("data/herbalwitch/recipes/" + id + ".json"));
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), piece);
            JsonObject result = recipe.getAsJsonObject("result");
            assertEquals("herbalwitch:" + id, result.get("item").getAsString(), piece);
            assertEquals(counts.get(piece), result.has("count") ? result.get("count").getAsInt() : 1, piece);
        });
    }

    @Test
    void tagsCoverEveryPieceAndRugsAreNotAxeMineable() {
        Path tags = GENERATED.resolve("data");
        JsonArray furniture = json(tags.resolve("herbalwitch/tags/blocks/furniture.json")).getAsJsonArray("values");
        assertEquals(90, furniture.size());
        String axe = json(tags.resolve("minecraft/tags/blocks/mineable/axe.json")).getAsJsonArray("values").toString();
        for (String id : ids()) {
            assertEquals(!pieceOf(id).equals("small_rug"), axe.contains("\"herbalwitch:" + id + "\""), "axe " + id);
        }
        for (String piece : PIECES.keySet()) {
            String values = json(tags.resolve("herbalwitch/tags/items/decor/" + piece + ".json")).getAsJsonArray("values").toString();
            for (String id : ids()) {
                assertEquals(pieceOf(id).equals(piece), values.contains("\"herbalwitch:" + id + "\""), piece + " tag " + id);
            }
        }
    }

    @Test
    void blockstatesSelectTheFinishModel() {
        for (String id : ids()) {
            JsonObject variants = json(GENERATED.resolve("assets/herbalwitch/blockstates/" + id + ".json")).getAsJsonObject("variants");
            switch (pieceOf(id)) {
                case "table_lamp" -> {
                    assertEquals(Set.of("lit=false", "lit=true"), variants.keySet(), id);
                    assertEquals("herbalwitch:block/" + id, variants.getAsJsonObject("lit=false").get("model").getAsString());
                    assertTrue(variants.getAsJsonObject("lit=true").get("model").getAsString().contains("/lit/"), id);
                }
                case "small_rug" -> assertEquals(Set.of(""), variants.keySet(), id);
                default -> {
                    assertEquals(Set.of("facing=north", "facing=east", "facing=south", "facing=west"), variants.keySet(), id);
                    Map<String, Integer> turns = Map.of("facing=north", 0, "facing=east", 90, "facing=south", 180, "facing=west", 270);
                    turns.forEach((key, y) -> {
                        JsonObject v = variants.getAsJsonObject(key);
                        assertEquals(y, v.has("y") ? v.get("y").getAsInt() : 0, id + " " + key);
                        assertEquals("herbalwitch:block/" + id, v.get("model").getAsString(), id);
                    });
                }
            }
        }
    }

}
