package io.github.dirtyredz.herbalwitch.data.grimoire;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import io.github.dirtyredz.herbalwitch.data.fixture.TextureDensity;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The grimoire's generated resources: the book and the two blocks craft from exact shapeless
 * ingredient lists, the blocks drop themselves and are axe-mineable, the blockstates turn the
 * authored base model, the items inherit their transforms, the art has its pinned sizes, and the
 * guide and station entries are prose only, never mechanics.
 */
class GrimoireResourcesTest {
    private static final Set<String> MECHANICS = Set.of("ingredients", "result", "count", "duration", "heat", "base",
            "packaging", "returns");
    private static final Pattern ENTRY = Pattern.compile("(guide|station)\\|herbalwitch:[a-z0-9_/]+");
    private static final Set<String> STARTER_HERBS = Set.of("herbalwitch:botany/temperate/lavender/fresh",
            "herbalwitch:botany/temperate/sage/fresh", "herbalwitch:botany/wet/mint/fresh",
            "herbalwitch:botany/temperate/chamomile/fresh");

    /** Item ids an ingredient accepts, sorted and joined with {@code /or/}. */
    private static String accepted(JsonElement ingredient) {
        Set<String> items = new TreeSet<>();
        if (ingredient.isJsonArray()) {
            ingredient.getAsJsonArray().forEach(e -> items.add(e.getAsJsonObject().get("item").getAsString()));
        } else {
            items.add(ingredient.getAsJsonObject().get("item").getAsString());
        }
        return String.join(" /or/ ", items);
    }

    private static Map<String, Integer> ingredientCounts(JsonObject recipe) {
        Map<String, Integer> counts = new TreeMap<>();
        for (JsonElement ingredient : recipe.getAsJsonArray("ingredients")) {
            counts.merge(accepted(ingredient), 1, Integer::sum);
        }
        return counts;
    }

    private static void assertShapeless(String path, String result, Map<String, Integer> expected) {
        JsonObject recipe = generated("data/herbalwitch/recipes/grimoire/" + path + ".json");
        assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString(), path);
        assertEquals(expected, ingredientCounts(recipe), path);
        JsonObject out = recipe.getAsJsonObject("result");
        assertEquals(result, out.get("item").getAsString(), path);
        assertEquals(1, out.has("count") ? out.get("count").getAsInt() : 1, path);
    }

    @Test
    void theBookCraftsFromABookInkAndAStarterHerb() {
        assertShapeless("book", "herbalwitch:grimoire/book", Map.of("minecraft:book", 1, "minecraft:ink_sac", 1,
                String.join(" /or/ ", new TreeSet<>(STARTER_HERBS)), 1));
    }

    @Test
    void theStandAndBookshelfCraftFromTheBook() {
        assertShapeless("stand", "herbalwitch:grimoire/stand", Map.of("minecraft:dark_oak_slab", 3, "minecraft:stick", 2,
                "herbalwitch:grimoire/book", 1));
        assertShapeless("bookshelf", "herbalwitch:grimoire/bookshelf", Map.of("minecraft:dark_oak_planks", 6,
                "minecraft:book", 2, "herbalwitch:grimoire/book", 1));
    }

    @Test
    void bothBlocksDropThemselvesAndMineWithAnAxe() {
        String axe = generated("data/minecraft/tags/blocks/mineable/axe.json").getAsJsonArray("values").toString();
        for (String block : GrimoireManifest.BLOCKS.keySet()) {
            JsonObject loot = generated("data/herbalwitch/loot_tables/blocks/grimoire/" + block + ".json");
            JsonArray pools = loot.getAsJsonArray("pools");
            assertEquals(1, pools.size(), block);
            JsonObject pool = pools.get(0).getAsJsonObject();
            assertEquals(1, pool.get("rolls").getAsInt(), block);
            assertEquals(1, pool.getAsJsonArray("entries").size(), block);
            assertEquals("herbalwitch:grimoire/" + block, pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
            assertTrue(pool.toString().contains("minecraft:survives_explosion"), block);
            assertTrue(axe.contains("\"herbalwitch:grimoire/" + block + "\""), "axe " + block);
        }
    }

    @Test
    void blockstatesTurnTheBaseModelThroughTheFourFacings() {
        Map<String, Integer> turns = Map.of("facing=north", 0, "facing=east", 90, "facing=south", 180, "facing=west", 270);
        for (String block : GrimoireManifest.BLOCKS.keySet()) {
            JsonObject variants = generated("assets/herbalwitch/blockstates/grimoire/" + block + ".json").getAsJsonObject("variants");
            assertEquals(turns.keySet(), variants.keySet(), block);
            turns.forEach((key, y) -> {
                JsonObject variant = variants.getAsJsonObject(key);
                assertEquals("herbalwitch:block/grimoire/" + block + "/base", variant.get("model").getAsString(), block + " " + key);
                assertEquals(y, variant.has("y") ? variant.get("y").getAsInt() : 0, block + " " + key);
            });
        }
    }

    @Test
    void itemModelsAreTheSpriteAndTheAuthoredBase() {
        JsonObject book = generated("assets/herbalwitch/models/item/grimoire/book.json");
        assertEquals("minecraft:item/generated", book.get("parent").getAsString());
        assertEquals("herbalwitch:item/grimoire/book", book.getAsJsonObject("textures").get("layer0").getAsString());
        for (String block : GrimoireManifest.BLOCKS.keySet()) {
            String item = "herbalwitch:item/grimoire/" + block;
            assertEquals("herbalwitch:block/grimoire/" + block + "/base", ModelResolver.resolve(item).chain().get(1), block);
            ItemDisplays.assertInherited(item);
        }
    }

    @Test
    void baseModelsAreAuthoredWithinOneCell() {
        for (String block : GrimoireManifest.BLOCKS.keySet()) {
            assertTrue(TemplateChecks.checkAuthored("herbalwitch:block/grimoire/" + block + "/base",
                    new double[] {0, 0, 0}, new double[] {16, 16, 16}) > 0, block);
        }
    }

    @Test
    void theBookSpriteAndReaderAtlasHavePinnedSizes() {
        int[] sprite = ResourceFiles.pngSize(ResourceFiles.MAIN_ASSETS.resolve("textures/item/grimoire/book.png"));
        assertEquals(TextureDensity.TEXTURE_SIZE + " x " + TextureDensity.TEXTURE_SIZE, sprite[0] + " x " + sprite[1]);
        assertEquals(32, sprite[0]);
        int[] reader = ResourceFiles.pngSize(ResourceFiles.MAIN_ASSETS.resolve("textures/gui/grimoire/reader.png"));
        assertEquals(TextureDensity.GUI_SIZE + " x " + TextureDensity.GUI_SIZE, reader[0] + " x " + reader[1]);
        assertEquals(512, reader[0]);
    }

    @Test
    void entriesAreProseOnlyAndKeyedByKindAndPath() {
        for (String guide : GrimoireManifest.GUIDES.keySet()) {
            JsonObject entry = generated("data/herbalwitch/grimoire_entries/guide/" + guide + ".json");
            assertEquals("guide|herbalwitch:" + guide, entry.get("entry").getAsString(), guide);
            assertEquals(GrimoireManifest.GUIDES.get(guide), entry.getAsJsonArray("pages").size(), guide);
            assertNoMechanics(guide, entry);
        }
        for (String station : GrimoireManifest.STATION_ENTRIES) {
            JsonObject entry = generated("data/herbalwitch/grimoire_entries/stations/" + station + ".json");
            assertEquals("station|herbalwitch:stations/" + station + "/station", entry.get("entry").getAsString(), station);
            assertEquals(1, entry.getAsJsonArray("pages").size(), station);
            assertNoMechanics(station, entry);
        }
    }

    private static void assertNoMechanics(String name, JsonObject entry) {
        assertTrue(ENTRY.matcher(entry.get("entry").getAsString()).matches(), name);
        for (String key : entry.keySet()) {
            assertTrue(!MECHANICS.contains(key), name + " carries mechanics field " + key);
        }
    }
}
