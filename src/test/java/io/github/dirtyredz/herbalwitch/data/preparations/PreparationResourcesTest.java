package io.github.dirtyredz.herbalwitch.data.preparations;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
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

    @Test
    void bundlesHangFromCeilingsAndWallsWithTheirHerbSprite() {
        for (BotanyManifest.Herb herb : BotanyManifest.HERBS) {
            String path = PreparationManifest.herbDir(herb) + "/bundle";
            for (String model : List.of(path, path + "_wall")) {
                TemplateChecks.check("herbalwitch:block/" + model, Map.of());
                assertEquals("herbalwitch:item/" + path, ModelResolver.resolve("herbalwitch:block/" + model).texture("#bundle"));
            }
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + path + ".json").getAsJsonObject("variants");
            assertTrue(variants.keySet().stream().noneMatch(k -> k.contains("face=floor") && !variants.getAsJsonObject(k)
                    .get("model").getAsString().endsWith("/bundle")), path);
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
            TemplateChecks.check(model, Map.of());
            boolean glows = ModelResolver.resolve(model).elements().toString().contains("forge_data");
            assertEquals(state.equals("on"), glows, model);
        }
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
