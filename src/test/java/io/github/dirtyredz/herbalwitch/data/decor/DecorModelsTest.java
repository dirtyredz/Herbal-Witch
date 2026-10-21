package io.github.dirtyredz.herbalwitch.data.decor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.Resolved;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.finishOf;
import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.ids;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.json;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.pngSize;
import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.faceSize;
import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.resolve;
import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.vec;
import static io.github.dirtyredz.herbalwitch.data.decor.DecorManifest.pieceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Resolves item and block models the way the game does (parent chain across the generated tree and
 * the authored templates, texture variables) and checks the model contracts: textures exist at 32
 * pixels, every face has explicit UVs inside one sprite at one UV unit per model pixel, frame and
 * fabric faces carry their tint index, glass renders as cutout, the lit lamp glows and the unlit one
 * does not, and every item has deliberate display transforms. Independent of the mod's code.
 */
class DecorModelsTest {
    private static final Path TEMPLATES = ResourceFiles.MAIN_ASSETS.resolve("models/block/decor/templates");
    private static final List<String> TEMPLATE_NAMES = List.of("armchair", "apothecary_counter", "wall_shelf",
            "table_lamp_off", "table_lamp_on", "rug");
    private static final Map<String, Integer> TINTS = Map.of("frame", 0, "fabric", 1, "shade", 1, "rug", 1);
    private static final Set<String> CONTEXTS = Set.of("gui", "ground", "fixed", "thirdperson_righthand",
            "firstperson_righthand", "firstperson_lefthand");

    private static String frameMaterial(String finish) {
        if (DecorManifest.NATURAL.contains(finish)) {
            return "herbalwitch:block/material/wood_grain";
        }
        return finish.equals("stone") ? "herbalwitch:block/material/cut_stone" : "herbalwitch:block/material/painted_wood";
    }


    @Test
    void templateFacesHaveExplicitUvsAtOneUnitPerPixelAndTheRightTint() {
        int faces = 0;
        for (String name : TEMPLATE_NAMES) {
            JsonObject model = json(TEMPLATES.resolve(name + ".json"));
            for (JsonElement e : model.getAsJsonArray("elements")) {
                JsonObject element = e.getAsJsonObject();
                double[] from = vec(element, "from");
                double[] to = vec(element, "to");
                for (int axis = 0; axis < 3; axis++) {
                    assertTrue(from[axis] >= 0 && to[axis] <= 16 && from[axis] < to[axis], name + ": element outside its block");
                }
                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject("faces").entrySet()) {
                    JsonObject face = entry.getValue().getAsJsonObject();
                    String what = name + " " + element.get("name").getAsString() + " " + entry.getKey();
                    assertTrue(face.has("uv"), what + ": explicit uv required");
                    JsonArray uv = face.getAsJsonArray("uv");
                    for (JsonElement value : uv) {
                        assertTrue(value.getAsDouble() >= 0 && value.getAsDouble() <= 16, what + ": uv outside the sprite " + uv);
                    }
                    String texture = face.get("texture").getAsString().substring(1);
                    if (!texture.equals("glass")) { // glass panes deliberately stretch the whole rim over each face
                        double[] size = faceSize(entry.getKey(), from, to);
                        assertEquals(size[0], Math.abs(uv.get(2).getAsDouble() - uv.get(0).getAsDouble()), 1e-6, what + ": u density");
                        assertEquals(size[1], Math.abs(uv.get(3).getAsDouble() - uv.get(1).getAsDouble()), 1e-6, what + ": v density");
                    }
                    Integer tint = TINTS.get(texture);
                    if (tint == null) {
                        assertFalse(face.has("tintindex"), what + ": " + texture + " must stay untinted");
                    } else {
                        assertEquals(tint, face.has("tintindex") ? face.get("tintindex").getAsInt() : null, what + ": tint index");
                    }
                    faces++;
                }
            }
        }
        assertTrue(faces > 300, "suspiciously few faces: " + faces);
    }

    @Test
    void glassRendersAsCutoutAndOnlyTheLitLampGlows() {
        for (String name : TEMPLATE_NAMES) {
            JsonObject model = json(TEMPLATES.resolve(name + ".json"));
            String elements = model.getAsJsonArray("elements").toString();
            boolean glass = elements.contains("\"#glass\"");
            assertEquals(glass, model.has("render_type") && model.get("render_type").getAsString().equals("minecraft:cutout"),
                    name + ": cutout exactly when it has glass");
            int glowing = 0;
            for (JsonElement e : model.getAsJsonArray("elements")) {
                JsonObject element = e.getAsJsonObject();
                if (element.has("forge_data")) {
                    assertEquals(15, element.getAsJsonObject("forge_data").get("block_light").getAsInt(), name);
                    assertTrue(element.toString().contains("\"#shade\""), name + ": only the shade glows");
                    glowing++;
                }
            }
            assertEquals(name.equals("table_lamp_on") ? 2 : 0, glowing, name + " glowing elements");
        }
        // The lit and unlit lamp are the same shape.
        JsonArray off = json(TEMPLATES.resolve("table_lamp_off.json")).getAsJsonArray("elements");
        JsonArray on = json(TEMPLATES.resolve("table_lamp_on.json")).getAsJsonArray("elements");
        assertEquals(off.size(), on.size());
        for (int i = 0; i < off.size(); i++) {
            assertEquals(off.get(i).getAsJsonObject().get("from"), on.get(i).getAsJsonObject().get("from"));
            assertEquals(off.get(i).getAsJsonObject().get("to"), on.get(i).getAsJsonObject().get("to"));
        }
    }

    @Test
    void everyBlockModelResolvesItsTexturesToGeneratedThirtyTwoPixelSprites() throws IOException {
        for (String id : ids()) {
            List<String> models = new ArrayList<>(List.of("herbalwitch:block/" + id));
            if (pieceOf(id).equals("table_lamp")) {
                models.add("herbalwitch:block/" + id.replace("/table_lamp/", "/table_lamp/lit/"));
            }
            for (String modelId : models) {
                Resolved model = resolve(modelId);
                assertFalse(model.elements().isEmpty(), modelId + " has no geometry");
                assertTrue(model.chain().get(model.chain().size() - 1).startsWith("herbalwitch:block/decor/templates/"),
                        modelId + " does not inherit a template: " + model.chain());
                for (JsonElement e : model.elements()) {
                    for (Map.Entry<String, JsonElement> face : e.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                        String texture = model.texture(face.getValue().getAsJsonObject().get("texture").getAsString());
                        assertNotNull(texture, modelId + ": unresolved texture variable");
                        assertTrue(texture.startsWith("herbalwitch:block/material/"), modelId + ": " + texture);
                        Path png = ResourceFiles.MAIN_ASSETS.resolve("textures/" + texture.substring("herbalwitch:".length()) + ".png");
                        assertTrue(Files.isRegularFile(png), modelId + ": missing " + png);
                        int[] size = pngSize(png);
                        assertEquals(32, size[0], png + " width");
                        assertEquals(32, size[1], png + " height");
                    }
                }
                String particle = model.texture("#particle");
                assertNotNull(particle, modelId + ": no particle texture");
                if (!pieceOf(id).equals("small_rug")) {
                    assertEquals(frameMaterial(finishOf(id)), model.texture("#frame"), modelId + ": frame material");
                }
            }
        }
    }

    @Test
    void everyItemIsItsPlacedModelWithDeliberateTransforms() {
        for (String id : ids()) {
            Resolved item = resolve("herbalwitch:item/" + id);
            assertEquals("herbalwitch:block/" + id, item.chain().get(1), id + ": item must show the placed (unlit) model");
            assertEquals(CONTEXTS, item.display().keySet(), id + ": display contexts");
            JsonObject gui = item.display().getAsJsonObject("gui");
            assertEquals("[30,225,0]", gui.getAsJsonArray("rotation").toString(), id);
            double scale = gui.getAsJsonArray("scale").get(0).getAsDouble();
            double expected = Set.of("small_rug", "small_wall_shelf").contains(pieceOf(id)) ? 0.75 : 0.625;
            assertEquals(expected, scale, 1e-6, id + ": gui scale");
        }
    }
}
