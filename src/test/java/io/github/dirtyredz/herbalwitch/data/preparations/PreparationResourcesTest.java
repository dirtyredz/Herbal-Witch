package io.github.dirtyredz.herbalwitch.data.preparations;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.Resolved;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.Png;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Preparation art and data: sprites, bundle and lantern models, loot, tags and the resin modifier. */
class PreparationResourcesTest {
    private static Png sprite(String path) {
        Png png = ResourceFiles.readPng(ResourceFiles.MAIN_ASSETS.resolve("textures/item/" + path + ".png"));
        assertEquals(16, png.width(), path);
        assertEquals(16, png.height(), path);
        for (int pixel : png.rgba()) {
            int alpha = pixel & 0xFF;
            assertTrue(alpha == 0 || alpha == 255, path + ": partial alpha");
        }
        return png;
    }

    private static void allDistinct(List<String> paths) {
        Set<String> seen = new HashSet<>();
        for (String path : paths) {
            assertTrue(seen.add(Arrays.toString(sprite(path).rgba())), path + " repeats another sprite");
        }
    }

    @Test
    void everyBundlePowderAndProductHasItsOwnFlatSprite() {
        List<String> bundles = new ArrayList<>();
        List<String> powders = new ArrayList<>();
        for (BotanyManifest.Herb herb : BotanyManifest.HERBS) {
            bundles.add(PreparationManifest.herbDir(herb) + "/bundle");
            powders.add(PreparationManifest.herbDir(herb) + "/powder");
        }
        List<String> products = PreparationManifest.PRODUCTS.keySet().stream().map(p -> "preparations/" + p).toList();
        allDistinct(bundles);
        allDistinct(powders);
        allDistinct(products);
        for (List<String> group : List.of(bundles, powders, products)) {
            for (String path : group) {
                ModelResolver.Resolved item = ModelResolver.resolve("herbalwitch:item/" + path);
                assertEquals("minecraft:item/generated", item.root(), path);
                assertEquals("herbalwitch:item/" + path, item.texture("#layer0"), path);
            }
        }
    }

    /**
     * Hung bundles are flat sprite planes with a small 3D tie and peg (the owner's preferred 2D look;
     * the dev.1 sprites bled onto the host block, so the planes are zero-thickness and inset, which
     * tools/preview/test/bundles.test.mjs guards), one ceiling and one wall form per herb, each
     * binding its own sprite; the wall form turns with the facing and floor states reuse the ceiling form.
     */
    @Test
    void bundlesHangFromCeilingsAndWallsAsSpritePlanes() {
        Set<String> shapes = new HashSet<>();
        for (BotanyManifest.Herb herb : BotanyManifest.HERBS) {
            String path = PreparationManifest.herbDir(herb) + "/bundle";
            String dir = "herbalwitch:block/preparations/bundles/" + herb.group() + "/" + herb.id() + "/";
            for (String form : List.of("ceiling", "wall")) {
                TemplateChecks.check(dir + form, Map.of());
                Resolved model = ModelResolver.resolve(dir + form);
                assertEquals("minecraft:cutout", model.renderType(), dir + form + " is a cutout sprite");
                assertEquals("herbalwitch:block/botany/parts", model.texture("#parts"), dir + form);
                assertEquals("herbalwitch:item/" + path, model.texture("#particle"), dir + form);
                assertEquals("herbalwitch:item/" + path, model.texture("#bundle_" + herb.id()), dir + form);
                assertTrue(model.elements().size() >= 3, dir + form + " is a peg, a tie and a sprite plane");
                assertTrue(shapes.add(model.elements().toString()), dir + form + " repeats another bundle");
            }
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + path + ".json").getAsJsonObject("variants");
            for (String key : variants.keySet()) {
                String expected = dir + (key.contains("face=wall") ? "wall" : "ceiling");
                assertEquals(expected, variants.getAsJsonObject(key).get("model").getAsString(), path + " " + key);
            }
            assertTrue(generated("data/herbalwitch/loot_tables/blocks/" + path + ".json").toString()
                    .contains("\"name\":\"herbalwitch:" + path + "\""), path + " drops itself");
        }
    }

    @Test
    void theOilLanternGlowsOnlyWhenLit() {
        JsonObject variants = generated("assets/herbalwitch/blockstates/" + PreparationManifest.LANTERN + ".json").getAsJsonObject("variants");
        assertEquals("herbalwitch:block/preparations/templates/oil_lantern_on", variants.getAsJsonObject("lit=true").get("model").getAsString());
        assertEquals("herbalwitch:block/preparations/templates/oil_lantern_off", variants.getAsJsonObject("lit=false").get("model").getAsString());
        for (String state : List.of("on", "off")) {
            String model = "herbalwitch:block/preparations/templates/oil_lantern_" + state;
            TemplateChecks.checkAuthored(model);
            boolean glows = ModelResolver.resolve(model).elements().toString().contains("forge_data");
            assertEquals(state.equals("on"), glows, model);
        }
        ItemDisplays.assertInherited("herbalwitch:item/" + PreparationManifest.LANTERN);
        JsonObject recipe = generated("data/herbalwitch/recipes/" + PreparationManifest.LANTERN + ".json");
        assertEquals("[\"N N\",\"NTN\",\" G \"]", recipe.getAsJsonArray("pattern").toString());
    }

    @Test
    void tagsAndResin() {
        assertEquals(14, generated("data/herbalwitch/tags/items/preparations/powders.json").getAsJsonArray("values").size());
        assertEquals(14, generated("data/herbalwitch/tags/blocks/preparations/bundles.json").getAsJsonArray("values").size());
        assertEquals("[\"minecraft:silverfish\",\"minecraft:endermite\"]",
                generated("data/herbalwitch/tags/entity_types/repelled_pests.json").getAsJsonArray("values").toString());
        assertEquals("[\"minecraft:spruce_leaves\"]",
                generated("data/herbalwitch/tags/blocks/preparations/resin_sources.json").getAsJsonArray("values").toString());
        JsonObject resin = generated("data/herbalwitch/loot_modifiers/preparations/resin.json");
        assertEquals(0.125, resin.get("chance").getAsDouble(), 1e-12);
        assertTrue(generated("data/forge/loot_modifiers/global_loot_modifiers.json").toString().contains("herbalwitch:preparations/resin"));
    }
}
