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
 * Contracts for untinted cuboid models (stations, furniture, the herb bed, bundles, lantern):
 * elements inside their bounds (one block unless the caller allows more), legal one-axis rotations
 * (0, +-22.5, +-45 degrees), explicit UVs at one unit per model pixel except on decal textures that
 * deliberately stretch a whole sprite, every texture resolving to an existing PNG, no tint indices,
 * and cutout exactly when an alpha texture is used.
 */
public final class TemplateChecks {
    /** Textures with transparency: the model must render as cutout. */
    public static final Set<String> ALPHA = Set.of("vine", "glass", "flame", "bundle", "ivy", "runner", "cloth",
            "bundle_green", "bundle_lavender", "bundle_white", "bundle_yellow");
    /** Decal textures stretched over a whole face rather than sampled at model density. */
    public static final Set<String> STRETCHED = Set.of("glass", "bundle", "ivy", "runner", "cloth", "plaque", "rug", "flame",
            "bundle_green", "bundle_lavender", "bundle_white", "bundle_yellow");
    private static final Set<Double> ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);

    private TemplateChecks() {
    }

    /** Checks one resolved model within one block; {@code variables} binds textures the template leaves open. */
    public static int check(String id, Map<String, String> variables) {
        return check(id, variables, new double[] {0, 0, 0}, new double[] {16, 16, 16});
    }

    /** Checks one resolved model whose elements must lie within {@code min..max} (model pixels). */
    public static int check(String id, Map<String, String> variables, double[] min, double[] max) {
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
                assertTrue(from[axis] >= min[axis] && to[axis] <= max[axis] && from[axis] <= to[axis],
                        id + ": element outside its bounds " + element.get("from") + " " + element.get("to"));
            }
            if (element.has("rotation")) {
                JsonObject rotation = element.getAsJsonObject("rotation");
                assertTrue(ANGLES.contains(rotation.get("angle").getAsDouble()), id + ": illegal rotation " + rotation);
                assertTrue(Set.of("x", "y", "z").contains(rotation.get("axis").getAsString()), id + ": rotation axis " + rotation);
            }
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject("faces").entrySet()) {
                JsonObject face = entry.getValue().getAsJsonObject();
                String variable = face.get("texture").getAsString().substring(1);
                String what = id + " " + entry.getKey() + " #" + variable;
                assertFalse(face.has("tintindex"), what + ": untinted templates carry no tint index");
                assertTrue(face.has("uv"), what + ": explicit uv required");
                JsonArray uv = face.getAsJsonArray("uv");
                for (JsonElement u : uv) {
                    assertTrue(u.getAsDouble() >= 0 && u.getAsDouble() <= 16, what + ": uv outside the texture " + uv);
                }
                if (!STRETCHED.contains(variable)) {
                    double[] size = faceSize(entry.getKey(), from, to);
                    assertEquals(size[0], Math.abs(uv.get(2).getAsDouble() - uv.get(0).getAsDouble()), 1e-3, what + ": u density");
                    assertEquals(size[1], Math.abs(uv.get(3).getAsDouble() - uv.get(1).getAsDouble()), 1e-3, what + ": v density");
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
