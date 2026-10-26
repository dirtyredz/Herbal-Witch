package io.github.dirtyredz.herbalwitch.data.grimoire.ponder;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.GENERATED;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.structure;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The generated first-preparation structure, decoded independently: vanilla structure format, complete
 * station geometry, supports, the kettle on the stove's origin burner and no gameplay payloads. Also checks
 * the lesson's associations and captions, and pins the bundled recipe the animation illustrates.
 */
class PonderResourcesTest {
    private static final String RACK = "herbalwitch:stations/drying_rack/station";
    private static final String STOVE = "herbalwitch:stations/stove/station";
    private static final String KETTLE = "herbalwitch:stations/kettle/station";
    private static final String MORTAR = "herbalwitch:stations/mortar/station";
    private static final String LAVENDER = "herbalwitch:botany/temperate/lavender/plant";

    private record Block(String id, Map<String, String> properties) {
    }

    /** Position "x,y,z" -> block, decoded from the generated file. */
    private static Map<String, Block> blocks() {
        CompoundTag tag = structure(GENERATED.resolve(PonderManifest.FIRST_PREPARATION_STRUCTURE));
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<Block> states = new ArrayList<>();
        for (int i = 0; i < palette.size(); i++) {
            CompoundTag entry = palette.getCompound(i);
            Map<String, String> properties = new HashMap<>();
            CompoundTag props = entry.getCompound("Properties");
            props.getAllKeys().forEach(key -> properties.put(key, props.getString(key)));
            states.add(new Block(entry.getString("Name"), properties));
        }
        Map<String, Block> blocks = new HashMap<>();
        ListTag list = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag block = list.getCompound(i);
            assertFalse(block.contains("nbt"), "structure blocks carry no block-entity data (inventories, jobs, owners)");
            int state = block.getInt("state");
            assertTrue(state >= 0 && state < states.size(), "palette reference " + state);
            ListTag pos = block.getList("pos", Tag.TAG_INT);
            String key = pos.getInt(0) + "," + pos.getInt(1) + "," + pos.getInt(2);
            assertNull(blocks.put(key, states.get(state)), "two blocks at " + key);
        }
        return blocks;
    }

    /** The block has this id and at least these properties (the palette stores every property of a state). */
    private static void assertState(String id, Map<String, String> expected, Block actual, String what) {
        assertNotNull(actual, what);
        assertEquals(id, actual.id(), what);
        expected.forEach((key, value) -> assertEquals(value, actual.properties().get(key), what + " " + key));
    }

    @Test
    void structureIsCompressedVanillaNbtWithoutEntities() {
        CompoundTag tag = structure(GENERATED.resolve(PonderManifest.FIRST_PREPARATION_STRUCTURE));
        assertEquals(3465, tag.getInt("DataVersion"));
        ListTag size = tag.getList("size", Tag.TAG_INT);
        assertEquals(List.of(11, 4, 5), List.of(size.getInt(0), size.getInt(1), size.getInt(2)));
        assertTrue(tag.contains("entities", Tag.TAG_LIST));
        assertEquals(0, tag.getList("entities", Tag.TAG_COMPOUND).size());
        Set<String> names = new HashSet<>();
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int i = 0; i < palette.size(); i++) assertTrue(names.add(palette.getCompound(i).toString()), "duplicate palette entry");
        for (String pos : blocks().keySet()) {
            String[] xyz = pos.split(",");
            assertTrue(Integer.parseInt(xyz[0]) < 11 && Integer.parseInt(xyz[1]) < 4 && Integer.parseInt(xyz[2]) < 5
                    && !pos.contains("-"), "inside the structure: " + pos);
        }
    }

    @Test
    void floorIsCompleteAndLavenderIsMatureOnFarmland() {
        var blocks = blocks();
        for (int x = 0; x < 11; x++) {
            for (int z = 0; z < 5; z++) {
                assertNotNull(blocks.get(x + ",0," + z), "floor at " + x + "," + z);
            }
        }
        assertEquals("minecraft:farmland", blocks.get("9,0,2").id());
        assertState(LAVENDER, Map.of("age", "3"), blocks.get("9,1,2"), "lavender");
    }

    @Test
    void stationsAreCompleteSupportedAndInert() {
        var blocks = blocks();
        // North-facing: the viewer's right is west, so the second column sits at x - 1.
        Map<String, String> rack = Map.of("7,1,2", "0", "6,1,2", "1", "7,2,2", "2", "6,2,2", "3");
        rack.forEach((pos, part) -> assertState(RACK, Map.of("facing", "north", "part", part, "active", "false"),
                blocks.get(pos), "rack cell " + part));
        Map<String, String> stove = Map.of("2,1,2", "0", "1,1,2", "1");
        stove.forEach((pos, part) -> assertState(STOVE, Map.of("facing", "north", "part", part, "lit", "false"),
                blocks.get(pos), "stove cell " + part));
        assertEquals(KETTLE, blocks.get("2,2,2").id(), "the kettle stands on the stove's origin burner");
        assertEquals("north", blocks.get("2,2,2").properties().get("facing"));
        assertEquals(MORTAR, blocks.get("4,1,2").id());
        long stations = blocks.values().stream().filter(b -> b.id().startsWith("herbalwitch:stations/")).count();
        assertEquals(8, stations, "four rack cells, two stove cells, the mortar and the kettle");
        blocks.forEach((pos, block) -> {
            String[] xyz = pos.split(",");
            int y = Integer.parseInt(xyz[1]);
            if (y > 0) assertNotNull(blocks.get(xyz[0] + "," + (y - 1) + "," + xyz[2]), "support under " + block.id() + " at " + pos);
        });
    }

    @Test
    void lessonFactsMatchTheGeneratedResources() throws Exception {
        var lesson = GuideLessons.FIRST_PREPARATION;
        assertEquals(PonderManifest.FIRST_PREPARATION_STRUCTURE, lesson.schematicAsset());
        assertEquals("herbalwitch:botany/temperate/lavender/fresh", lesson.canonicalItem());
        assertEquals(14, lesson.items().size());
        for (String item : lesson.items()) {
            String path = item.substring("herbalwitch:".length());
            assertTrue(Files.isRegularFile(GENERATED.resolve("assets/herbalwitch/models/item/" + path + ".json")), "item model for " + item);
        }
        String page = lesson.page().substring("herbalwitch:".length());
        assertTrue(Files.isRegularFile(GENERATED.resolve("assets/herbalwitch/guide/" + page)), lesson.page());
    }

    @Test
    void captionsTitleAndWatchLabelAreInTheLanguageFile() {
        JsonObject lang = generated("assets/herbalwitch/lang/en_us.json");
        assertEquals(FirstPreparationText.TITLE, lang.get(FirstPreparationText.titleKey()).getAsString());
        assertEquals(PonderManifest.FIRST_PREPARATION_CAPTIONS.size(), FirstPreparationText.ALL.size());
        for (var caption : FirstPreparationText.ALL) {
            assertEquals(caption.english(), lang.get(caption.key()).getAsString(), caption.key());
            assertFalse(caption.english().matches(".*\\d.*"), "captions quote no amounts, durations or yields: " + caption.id());
        }
        assertTrue(lang.has(GuideLessons.WATCH_LABEL_KEY));
    }

    /** The animation illustrates this exact bundled recipe; a change here must prompt a storyboard review. */
    @Test
    void illustratedRecipeIsStillTheBundledRestfulInfusion() {
        JsonObject recipe = generated("data/herbalwitch/recipes/processing/steeping/restful_infusion.json");
        Set<String> ingredients = new HashSet<>();
        recipe.getAsJsonArray("ingredients").forEach(e -> ingredients.add(
                e.getAsJsonObject().getAsJsonObject("ingredient").get("item").getAsString()));
        assertEquals(Set.of("herbalwitch:preparations/herbs/temperate/lavender/powder",
                "herbalwitch:preparations/herbs/shade/valerian/powder"), ingredients);
        JsonObject base = recipe.getAsJsonObject("base").getAsJsonObject("ingredient");
        assertEquals("minecraft:potion", base.get("item").getAsString());
        assertEquals("{Potion:\"minecraft:water\"}", base.get("nbt").getAsString());
        assertEquals("herbalwitch:preparations/drinks/restful_infusion", recipe.getAsJsonObject("result").get("item").getAsString());
        assertFalse(recipe.has("packaging"), "the illustrated recipe needs no separate packaging item");
    }
}
