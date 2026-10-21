package io.github.dirtyredz.herbalwitch.data.fixture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.faceSize;
import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contracts for untinted authored templates (stations, bundles, lantern): elements inside the block,
 * explicit UVs at one unit per model pixel (except textures that deliberately stretch a whole sprite),
 * every texture resolving to an existing PNG, no tint indices, and cutout exactly when an alpha
 * texture is used.
 */
public final class TemplateChecks {
    /** Textures with transparency: the model must render as cutout. */
    public static final Set<String> ALPHA = Set.of("vine", "glass", "flame", "bundle");
    /** Textures stretched over a whole face rather than sampled at model density. */
    public static final Set<String> STRETCHED = Set.of("glass", "bundle");

    private TemplateChecks() {
    }

    /** Checks one resolved model; {@code variables} binds textures the template leaves open (e.g. bundle). */
    public static int check(String id, Map<String, String> variables) {
        ModelResolver.Resolved model = ModelResolver.resolve(id);
        JsonArray elements = model.elements();
        assertFalse(elements.isEmpty(), id + " has no geometry");
        boolean alpha = false;
        int faces = 0;
        for (JsonElement e : elements) {
            JsonObject element = e.getAsJsonObject();
            double[] from = vec(element, "from");
            double[] to = vec(element, "to");
            for (int axis = 0; axis < 3; axis++) {
                assertTrue(from[axis] >= 0 && to[axis] <= 16 && from[axis] < to[axis], id + ": element outside its block");
            }
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject("faces").entrySet()) {
                JsonObject face = entry.getValue().getAsJsonObject();
                String variable = face.get("texture").getAsString().substring(1);
                String what = id + " " + entry.getKey() + " #" + variable;
                assertFalse(face.has("tintindex"), what + ": untinted templates carry no tint index");
                assertTrue(face.has("uv"), what + ": explicit uv required");
                JsonArray uv = face.getAsJsonArray("uv");
                if (!STRETCHED.contains(variable)) {
                    double[] size = faceSize(entry.getKey(), from, to);
                    assertEquals(size[0], Math.abs(uv.get(2).getAsDouble() - uv.get(0).getAsDouble()), 1e-6, what + ": u density");
                    assertEquals(size[1], Math.abs(uv.get(3).getAsDouble() - uv.get(1).getAsDouble()), 1e-6, what + ": v density");
                }
                String texture = variables.getOrDefault(variable, model.texture("#" + variable));
                assertNotNull(texture, what + ": unresolved texture");
                assertTrue(Files.isRegularFile(ModelResolver.texturePng(texture)), what + ": missing " + texture);
                alpha |= ALPHA.contains(variable);
                faces++;
            }
        }
        assertEquals(alpha, "minecraft:cutout".equals(model.renderType()), id + ": cutout exactly when an alpha texture is used");
        assertNotNull(model.texture("#particle"), id + ": no particle");
        return faces;
    }
}
