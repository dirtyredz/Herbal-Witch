package io.github.dirtyredz.herbalwitch.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the checked-in generated tree without starting Minecraft. Pieces and finishes are
 * written out here independently of the mod's catalogue, so a catalogue mistake shows up as a diff,
 * and the exact file manifest makes any stale or missing generated file fail.
 */
class GeneratedResourcesTest {
    static final Path GENERATED = Path.of("src/generated/resources");
    static final List<String> NATURAL = List.of("dark_oak", "walnut", "cherry", "rosewood", "pale_oak", "spruce", "birch");
    static final List<String> PAINTED = List.of("black", "stone", "moss", "sage_green", "forest_green", "plum",
            "burgundy", "dusty_pink", "blush", "cream", "lilac");
    /** piece -> family directory, in tab order. */
    static final Map<String, String> PIECES = linked("armchair", "seating", "apothecary_counter", "surfaces",
            "small_wall_shelf", "storage", "table_lamp", "lighting", "small_rug", "textiles");
    static final Map<String, String> NAMES = Map.of("armchair", "Armchair", "apothecary_counter", "Apothecary Counter",
            "small_wall_shelf", "Small Wall Shelf", "table_lamp", "Table Lamp", "small_rug", "Small Rug");
    private static final Set<String> ALLOWED_NAMESPACES = Set.of("minecraft", "herbalwitch", "forge");
    private static final Pattern NAMESPACED = Pattern.compile("\"#?([a-z0-9_.-]+):[a-z0-9_./-]+\"");

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    /** {@code decor/<family>/<piece>/<palette>/<finish>} for every piece and finish: 90 ids. */
    static List<String> ids() {
        List<String> ids = new ArrayList<>();
        PIECES.forEach((piece, family) -> {
            NATURAL.forEach(f -> ids.add("decor/" + family + "/" + piece + "/natural/" + f));
            PAINTED.forEach(f -> ids.add("decor/" + family + "/" + piece + "/painted/" + f));
        });
        return ids;
    }

    static String finishOf(String id) {
        return id.substring(id.lastIndexOf('/') + 1);
    }

    static String pieceOf(String id) {
        return id.split("/")[2];
    }

    static JsonObject json(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch (IOException e) {
            throw new AssertionError("cannot read " + path, e);
        }
    }

    private static String display(String finish) {
        StringBuilder out = new StringBuilder();
        for (String word : finish.split("_")) {
            out.append(out.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private static Set<String> generatedFiles() throws IOException {
        try (Stream<Path> walk = Files.walk(GENERATED)) {
            return new TreeSet<>(walk.filter(Files::isRegularFile)
                    .map(p -> GENERATED.relativize(p).toString().replace('\\', '/'))
                    .filter(p -> !p.startsWith(".cache/"))
                    .toList());
        }
    }

    @Test
    void ninetyDistinctIds() {
        assertEquals(90, ids().size());
        assertEquals(90, new TreeSet<>(ids()).size());
    }

    @Test
    void generatedTreeIsExactlyTheExpectedManifest() throws IOException {
        Set<String> expected = new TreeSet<>();
        expected.add("assets/herbalwitch/lang/en_us.json");
        for (String id : ids()) {
            expected.add("assets/herbalwitch/blockstates/" + id + ".json");
            expected.add("assets/herbalwitch/models/block/" + id + ".json");
            expected.add("assets/herbalwitch/models/item/" + id + ".json");
            expected.add("data/herbalwitch/loot_tables/blocks/" + id + ".json");
            expected.add("data/herbalwitch/recipes/" + id + "_from_restyling.json");
            expected.add("data/herbalwitch/advancements/recipes/decorations/" + id + "_from_restyling.json");
            if (pieceOf(id).equals("table_lamp")) {
                String palette = id.contains("/natural/") ? "natural" : "painted";
                expected.add("assets/herbalwitch/models/block/decor/lighting/table_lamp/lit/" + palette + "/" + finishOf(id) + ".json");
            }
            if (finishOf(id).equals("dark_oak")) {
                expected.add("data/herbalwitch/recipes/" + id + ".json");
                expected.add("data/herbalwitch/advancements/recipes/decorations/" + id + ".json");
            }
        }
        expected.add("data/herbalwitch/tags/blocks/furniture.json");
        expected.add("data/herbalwitch/tags/items/furniture.json");
        PIECES.keySet().forEach(piece -> expected.add("data/herbalwitch/tags/items/decor/" + piece + ".json"));
        expected.add("data/minecraft/tags/blocks/mineable/axe.json");
        assertEquals(expected, generatedFiles());
    }

    @Test
    void everyGeneratedLeafDirectoryHasFewerThanTwelveEntries() throws IOException {
        Map<String, Integer> entries = new HashMap<>();
        try (Stream<Path> walk = Files.walk(GENERATED)) {
            walk.filter(p -> !p.equals(GENERATED) && !GENERATED.relativize(p).toString().startsWith(".cache"))
                    .forEach(p -> entries.merge(p.getParent().toString(), 1, Integer::sum));
        }
        entries.forEach((dir, count) -> assertTrue(count < 12, dir + " has " + count + " entries"));
    }

    @Test
    void everyBlockHasItsEnglishName() {
        JsonObject lang = json(GENERATED.resolve("assets/herbalwitch/lang/en_us.json"));
        for (String id : ids()) {
            String key = "block.herbalwitch." + id.replace('/', '.');
            assertEquals(display(finishOf(id)) + " " + NAMES.get(pieceOf(id)), lang.has(key) ? lang.get(key).getAsString() : null, key);
        }
        assertEquals("The Herbal Witch: Cottage Decor", lang.get("itemGroup.herbalwitch.decor").getAsString());
        assertEquals(91, lang.size());
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

    @Test
    void noOptionalModReferences() throws IOException {
        for (String rel : generatedFiles()) {
            Matcher m = NAMESPACED.matcher(Files.readString(GENERATED.resolve(rel)));
            while (m.find()) {
                assertTrue(ALLOWED_NAMESPACES.contains(m.group(1)), rel + " references " + m.group());
            }
            assertFalse(rel.endsWith(".png"), "textures are authored by tools/generate-textures.mjs, not datagen: " + rel);
        }
    }
}
