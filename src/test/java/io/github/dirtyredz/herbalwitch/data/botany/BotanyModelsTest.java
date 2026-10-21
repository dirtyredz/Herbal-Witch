package io.github.dirtyredz.herbalwitch.data.botany;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Herb art and models: every stage is an untinted cutout cross over a 16 px sprite with binary
 * alpha; stages change silhouette and grow; the fourteen mature silhouettes differ from each other;
 * seeds and fresh herbs are flat 16 px items; the raised bed keeps one UV unit per model pixel.
 */
class BotanyModelsTest {
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

    @Test
    void everyStageIsACutoutCrossWhoseSilhouetteGrows() {
        for (Herb herb : BotanyManifest.HERBS) {
            int previous = 0;
            Set<Set<Integer>> masks = new HashSet<>();
            for (int age = 0; age < 4; age++) {
                Resolved model = ModelResolver.resolve("herbalwitch:block/" + herb.dir() + "/stage_" + age);
                assertEquals("minecraft:block/cross", model.root(), herb.id());
                assertEquals("minecraft:cutout", model.renderType(), herb.id());
                Png png = sprite(model.texture("#cross"));
                Set<Integer> mask = png.mask();
                assertTrue(mask.size() > previous, herb.id() + " stage " + age + " does not grow");
                assertTrue(masks.add(mask), herb.id() + " stage " + age + " repeats a silhouette");
                boolean grounded = false;
                for (int x = 0; x < 16; x++) {
                    grounded |= png.alpha(x, 15) != 0 || png.alpha(x, 14) != 0;
                }
                assertTrue(grounded, herb.id() + " stage " + age + " floats");
                previous = mask.size();
            }
        }
    }

    @Test
    void matureSilhouettesAndFreshHerbsAreDistinct() {
        List<Herb> herbs = BotanyManifest.HERBS;
        for (int i = 0; i < herbs.size(); i++) {
            for (int j = i + 1; j < herbs.size(); j++) {
                Set<Integer> a = sprite("herbalwitch:block/" + herbs.get(i).dir() + "/stage_3").mask();
                Set<Integer> b = sprite("herbalwitch:block/" + herbs.get(j).dir() + "/stage_3").mask();
                assertTrue(difference(a, b) >= 20, herbs.get(i).id() + " and " + herbs.get(j).id() + " look alike when mature");
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
    void herbBedIsAnUntintedDarkWoodTemplateWithDryAndMoistSoil() {
        Map<String, String> soils = Map.of("herbalwitch:block/" + BotanyManifest.BED, "soil_dry",
                "herbalwitch:block/" + BotanyManifest.BED + "_moist", "soil_moist");
        soils.forEach((id, soil) -> {
            TemplateChecks.check(id, Map.of());
            Resolved model = ModelResolver.resolve(id);
            assertEquals("herbalwitch:block/botany/garden/" + soil, model.texture("#soil"), id);
            assertEquals("herbalwitch:block/material/dark_wood", model.texture("#dark_wood"), id);
            assertEquals(32, ResourceFiles.pngSize(ModelResolver.texturePng(model.texture("#soil")))[0]);
        });
        assertEquals("herbalwitch:block/" + BotanyManifest.BED,
                ModelResolver.resolve("herbalwitch:item/" + BotanyManifest.BED).chain().get(1));
    }
}
