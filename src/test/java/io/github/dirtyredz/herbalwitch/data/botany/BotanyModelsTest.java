package io.github.dirtyredz.herbalwitch.data.botany;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest.Herb;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.Resolved;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.Png;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Herb art and models. Young ages are the planted cross over a 16 px sprite with binary alpha; the
 * mature age is a 3D model of small cubes from the parts atlas; seeds and fresh herbs are flat
 * items. Every age is rooted in the soil: its lowest geometry meets the herb bed's soil surface
 * (the dev.5 bug was a cross standing on the bed's rim, three pixels above its soil).
 */
class BotanyModelsTest {
    private static final String CROSS = "herbalwitch:block/botany/templates/planted_cross";
    private static final String SOIL_CORE = "herbalwitch:block/botany/garden/parts/soil/core";
    private static final double FARMLAND_TOP = 15;

    private static Png sprite(String textureId) {
        Path file = ModelResolver.texturePng(textureId);
        assertTrue(Files.isRegularFile(file), "missing " + file);
        Png png = ResourceFiles.readPng(file);
        assertEquals(16, png.width(), file + " width");
        assertEquals(16, png.height(), file + " height");
        for (int i = 0; i < png.rgba().length; i++) {
            int alpha = png.rgba()[i] & 0xFF;
            assertTrue(alpha == 0 || alpha == 255, file + " has partial alpha " + alpha);
        }
        return png;
    }

    private static int difference(Set<Integer> a, Set<Integer> b) {
        Set<Integer> union = new HashSet<>(a);
        union.addAll(b);
        Set<Integer> both = new HashSet<>(a);
        both.retainAll(b);
        return union.size() - both.size();
    }

    /** Lowest and highest y of a model's elements (model pixels, before rotation). */
    private static double[] heights(JsonArray elements) {
        double low = Double.MAX_VALUE;
        double high = -Double.MAX_VALUE;
        for (JsonElement e : elements) {
            low = Math.min(low, vec(e.getAsJsonObject(), "from")[1]);
            high = Math.max(high, vec(e.getAsJsonObject(), "to")[1]);
        }
        return new double[] {low, high};
    }

    @Test
    void everyAgeIsRootedInBedSoilAndInFarmland() {
        double soil = heights(ModelResolver.resolve(SOIL_CORE).elements())[1];
        for (Herb herb : BotanyManifest.HERBS) {
            for (int age = 0; age < 4; age++) {
                double root = heights(ModelResolver.resolve("herbalwitch:block/" + herb.dir() + "/stage_" + age).elements())[0];
                String what = herb.id() + " stage " + age;
                assertEquals(soil, 16 + root, 1e-6, what + " does not meet the bed's soil");
                assertTrue(16 + root <= FARMLAND_TOP, what + " floats over farmland");
                assertTrue(root >= -2, what + " sinks more than two pixels into full ground");
            }
        }
    }

    @Test
    void youngAgesAreCutoutCrossesWhoseSilhouetteGrows() {
        for (Herb herb : BotanyManifest.HERBS) {
            int previous = 0;
            Set<Set<Integer>> masks = new HashSet<>();
            for (int age = 0; age < 3; age++) {
                Resolved model = ModelResolver.resolve("herbalwitch:block/" + herb.dir() + "/stage_" + age);
                assertEquals(CROSS, model.chain().get(1), herb.id());
                assertEquals("minecraft:cutout", model.renderType(), herb.id());
                Png png = sprite(model.texture("#cross"));
                Set<Integer> mask = png.mask();
                assertTrue(mask.size() > previous, herb.id() + " stage " + age + " does not grow");
                assertTrue(masks.add(mask), herb.id() + " stage " + age + " repeats a silhouette");
                boolean grounded = false;
                for (int x = 0; x < 16; x++) {
                    grounded |= png.alpha(x, 15) != 0 || png.alpha(x, 14) != 0;
                }
                assertTrue(grounded, herb.id() + " stage " + age + " sprite does not reach its roots");
                previous = mask.size();
            }
        }
    }

    @Test
    void matureHerbsAreDistinctCubeModelsWithinBudget() {
        Set<String> shapes = new HashSet<>();
        for (Herb herb : BotanyManifest.HERBS) {
            String id = "herbalwitch:block/" + herb.dir() + "/stage_3";
            Resolved model = ModelResolver.resolve(id);
            assertEquals("herbalwitch:block/" + herb.dir() + "/mature", model.chain().get(1), herb.id());
            JsonArray elements = model.elements();
            assertTrue(elements.size() >= 10 && elements.size() <= 64, herb.id() + " has " + elements.size() + " elements");
            double[] h = heights(elements);
            assertTrue(h[1] <= 16, herb.id() + " grows out of its block");
            assertTrue(h[1] - h[0] >= 6, herb.id() + " is not taller than a seedling");
            for (JsonElement e : elements) {
                for (Map.Entry<String, JsonElement> face : e.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                    assertEquals("herbalwitch:block/botany/parts", model.texture(face.getValue().getAsJsonObject().get("texture").getAsString()),
                            herb.id() + " face " + face.getKey());
                }
            }
            assertTrue(shapes.add(elements.toString()), herb.id() + " repeats another herb's mature model");
        }
        Png atlas = ResourceFiles.readPng(ModelResolver.texturePng("herbalwitch:block/botany/parts"));
        assertEquals(32, atlas.width());
    }

    @Test
    void freshHerbsAndSeedsAreDistinct() {
        List<Herb> herbs = BotanyManifest.HERBS;
        for (int i = 0; i < herbs.size(); i++) {
            for (int j = i + 1; j < herbs.size(); j++) {
                Png fa = sprite("herbalwitch:item/" + herbs.get(i).dir() + "/fresh");
                Png fb = sprite("herbalwitch:item/" + herbs.get(j).dir() + "/fresh");
                assertTrue(difference(fa.mask(), fb.mask()) >= 12, herbs.get(i).id() + " and " + herbs.get(j).id() + " fresh");
                Png sa = sprite("herbalwitch:item/" + herbs.get(i).dir() + "/seeds");
                Png sb = sprite("herbalwitch:item/" + herbs.get(j).dir() + "/seeds");
                assertNotEquals(java.util.Arrays.toString(sa.rgba()), java.util.Arrays.toString(sb.rgba()));
            }
        }
    }

    @Test
    void seedsAndFreshHerbsAreFlatItems() {
        for (Herb herb : BotanyManifest.HERBS) {
            for (String item : List.of("seeds", "fresh")) {
                Resolved model = ModelResolver.resolve("herbalwitch:item/" + herb.dir() + "/" + item);
                assertEquals("minecraft:item/generated", model.root());
                assertEquals("herbalwitch:item/" + herb.dir() + "/" + item, model.texture("#layer0"));
                sprite(model.texture("#layer0"));
            }
        }
    }

    @Test
    void herbBedPartsAreUntintedWoodAndSoilTemplates() {
        String parts = "herbalwitch:block/botany/garden/parts/";
        for (String wood : List.of("rail", "rail_end", "rail_start", "post")) {
            TemplateChecks.check(parts + "wood/" + wood, Map.of());
            Resolved part = ModelResolver.resolve(parts + "wood/" + wood);
            String grain = part.texture(wood.equals("post") ? "#post" : "#wood");
            assertEquals(wood.equals("post") ? "herbalwitch:block/material/wood_post" : "herbalwitch:block/material/wood", grain, wood);
        }
        for (String soil : List.of("core", "edge", "corner")) {
            for (String kind : List.of("soil", "moist")) {
                String id = parts + kind + "/" + soil;
                TemplateChecks.check(id, Map.of());
                String expected = kind.equals("soil") ? "soil_dry" : "soil_moist";
                assertEquals("herbalwitch:block/botany/garden/" + expected, ModelResolver.resolve(id).texture("#soil"), id);
            }
        }
        assertEquals(32, ResourceFiles.pngSize(ModelResolver.texturePng("herbalwitch:block/botany/garden/soil_dry"))[0]);
        String bed = "herbalwitch:block/" + BotanyManifest.BED;
        TemplateChecks.check(bed, Map.of());
        Resolved item = ModelResolver.resolve("herbalwitch:item/" + BotanyManifest.BED);
        assertEquals(bed, item.chain().get(1));
        JsonObject gui = item.display().getAsJsonObject("gui");
        assertEquals("[30,225,0]", gui.getAsJsonArray("rotation").toString(), "the bed item shows as a block");
    }
}
