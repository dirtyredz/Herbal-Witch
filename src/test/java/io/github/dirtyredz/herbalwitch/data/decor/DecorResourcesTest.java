package io.github.dirtyredz.herbalwitch.data.decor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.Resolved;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.ids;
import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.pieceOf;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The single-design furniture (owner direction 2026-10-21): five pieces, one block each, crafted
 * directly, never restyled; untinted templates in dark wood, forest-green velvet, magenta, plum
 * and gold, with trailing ivy (art pass 0.2.0).
 */
class DecorResourcesTest {
    private static final Set<String> CONTEXTS = Set.of("gui", "ground", "fixed", "thirdperson_righthand",
            "firstperson_righthand", "firstperson_lefthand");
    private static final Set<String> PALETTE = Set.of("dark_wood", "wood", "wood_post", "glass", "tincture", "velvet",
            "fabric_pink", "trim_purple", "gold", "iron", "paint_green", "runner", "runner_magenta", "ivy", "rug", "lamp_shade", "bundle_lavender");

    @Test
    void oneDesignAndFivePiecesWithStablePaths() {
        assertEquals(List.of(FinishCatalog.COTTAGE), FinishCatalog.ALL);
        assertEquals(DecorManifest.PIECES.keySet().stream().toList(), ArtSpikeCatalog.ALL.stream().map(FurnitureSpec::id).toList());
        assertEquals(ids(), ArtSpikeCatalog.ALL.stream().map(FurnitureSpec::path).toList());
    }

    @Test
    void everyPieceCraftsDirectlyAndDropsItself() {
        Map<String, Integer> counts = Map.of("armchair", 1, "apothecary_counter", 1, "small_wall_shelf", 2,
                "table_lamp", 1, "small_rug", 2);
        for (String id : ids()) {
            JsonObject recipe = generated("data/herbalwitch/recipes/" + id + ".json");
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), id);
            JsonObject result = recipe.getAsJsonObject("result");
            assertEquals("herbalwitch:" + id, result.get("item").getAsString(), id);
            assertEquals(counts.get(pieceOf(id)), result.has("count") ? result.get("count").getAsInt() : 1, id);
            String loot = generated("data/herbalwitch/loot_tables/blocks/" + id + ".json").toString();
            assertTrue(loot.contains("\"name\":\"herbalwitch:" + id + "\"") && loot.contains("minecraft:survives_explosion"), id);
        }
    }

    @Test
    void tagsCoverEveryPieceAndRugsAreNotAxeMineable() {
        JsonArray furniture = generated("data/herbalwitch/tags/blocks/furniture.json").getAsJsonArray("values");
        assertEquals(5, furniture.size());
        String axe = generated("data/minecraft/tags/blocks/mineable/axe.json").getAsJsonArray("values").toString();
        for (String id : ids()) {
            assertEquals(!pieceOf(id).equals("small_rug"), axe.contains("\"herbalwitch:" + id + "\""), "axe " + id);
        }
    }

    @Test
    void blockstatesUseTheTemplatesDirectly() {
        for (String id : ids()) {
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + id + ".json").getAsJsonObject("variants");
            String template = "herbalwitch:block/decor/templates/" + DecorManifest.TEMPLATES.get(pieceOf(id));
            switch (pieceOf(id)) {
                case "table_lamp" -> {
                    assertEquals(template + "_off", variants.getAsJsonObject("lit=false").get("model").getAsString());
                    assertEquals(template + "_on", variants.getAsJsonObject("lit=true").get("model").getAsString());
                }
                case "small_rug" -> assertEquals(template, variants.getAsJsonObject("").get("model").getAsString());
                default -> {
                    Map<String, Integer> turns = Map.of("facing=north", 0, "facing=east", 90, "facing=south", 180, "facing=west", 270);
                    turns.forEach((key, y) -> {
                        JsonObject v = variants.getAsJsonObject(key);
                        assertEquals(y, v.has("y") ? v.get("y").getAsInt() : 0, id + " " + key);
                        assertEquals(template, v.get("model").getAsString(), id);
                    });
                }
            }
        }
    }

    @Test
    void templatesAreUntintedInTheCottagePaletteWithVines() {
        boolean vines = false;
        for (String template : List.of("armchair", "apothecary_counter", "wall_shelf", "table_lamp_off", "table_lamp_on", "rug")) {
            String id = "herbalwitch:block/decor/templates/" + template;
            TemplateChecks.check(id, Map.of());
            Resolved model = ModelResolver.resolve(id);
            for (Map.Entry<String, String> texture : model.textures().entrySet()) {
                if (!texture.getKey().equals("particle")) {
                    String name = texture.getValue().substring(texture.getValue().lastIndexOf('/') + 1);
                    assertTrue(PALETTE.contains(name), id + " uses " + texture.getValue());
                    assertTrue(!name.contains("floral"), id + ": no floral fabric");
                }
            }
            vines |= model.textures().containsKey("ivy");
        }
        assertTrue(vines, "no template trails ivy");
    }

    @Test
    void onlyTheLitLampGlowsAndBothLampsShareTheirShape() {
        JsonArray off = ModelResolver.resolve("herbalwitch:block/decor/templates/table_lamp_off").elements();
        JsonArray on = ModelResolver.resolve("herbalwitch:block/decor/templates/table_lamp_on").elements();
        assertEquals(off.size(), on.size());
        int glowing = 0;
        for (int i = 0; i < off.size(); i++) {
            assertEquals(off.get(i).getAsJsonObject().get("from"), on.get(i).getAsJsonObject().get("from"));
            assertEquals(off.get(i).getAsJsonObject().get("to"), on.get(i).getAsJsonObject().get("to"));
            assertTrue(!off.get(i).getAsJsonObject().has("forge_data"), "the unlit lamp glows");
            JsonObject element = on.get(i).getAsJsonObject();
            if (element.has("forge_data")) {
                assertTrue(element.toString().contains("\"#lamp_shade\""), "only the shade glows");
                glowing++;
            } else {
                assertTrue(!element.toString().contains("\"#lamp_shade\""), "the whole shade glows");
            }
        }
        assertTrue(glowing >= 2, "the lit shade glows");
    }

    @Test
    void everyItemIsItsTemplateWithDeliberateTransforms() {
        for (String id : ids()) {
            Resolved item = ModelResolver.resolve("herbalwitch:item/" + id);
            String template = DecorManifest.TEMPLATES.get(pieceOf(id)) + (pieceOf(id).equals("table_lamp") ? "_off" : "");
            assertEquals("herbalwitch:block/decor/templates/" + template, item.chain().get(1), id);
            assertEquals(CONTEXTS, item.display().keySet(), id);
            JsonElement scale = item.display().getAsJsonObject("gui").getAsJsonArray("scale").get(0);
            double expected = Set.of("small_rug", "small_wall_shelf").contains(pieceOf(id)) ? 0.75 : 0.625;
            assertEquals(expected, scale.getAsDouble(), 1e-6, id);
        }
    }
}
