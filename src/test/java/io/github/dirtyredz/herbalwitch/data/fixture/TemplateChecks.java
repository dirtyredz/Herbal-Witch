package io.github.dirtyredz.herbalwitch.data.fixture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.faceSize;
import static io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contracts for untinted cuboid models: elements inside their bounds (one block unless the caller
 * allows more), legal one-axis rotations (0, +-22.5, +-45 degrees), explicit finite UVs inside the
 * 0..16 texture space, every texture resolving to an existing square PNG of at least 16 px, no tint
 * indices, a particle texture, and cutout exactly when a used texture has transparent pixels (read
 * from the PNG, not from variable names).
 * <p>
 * Two UV modes: {@link UvMode#POSITIONAL} for the procedural families (herbs, herb beds, hung
 * bundles), whose UVs run at one unit per model pixel except on stretched sprites; and
 * {@link UvMode#AUTHORED} for the Blockbench pieces (stations, furniture, the oil lantern), whose
 * UVs are drawn by hand and may stretch, flip or rotate.
 */
public final class TemplateChecks {
    /** How a model's UVs are made. */
    public enum UvMode { POSITIONAL, AUTHORED }

    /** Procedural variables stretched over a whole face: the botany swatch atlas (and every bundle_<herb> sprite). */
    public static final Set<String> STRETCHED = Set.of("parts");
    private static final Set<Double> ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);
    private static final Map<Path, ResourceFiles.Png> PNGS = new HashMap<>();

    private TemplateChecks() {
    }

    /** Checks one procedural model within one block; {@code variables} binds textures the template leaves open. */
    public static int check(String id, Map<String, String> variables) {
        return check(id, variables, new double[] {0, 0, 0}, new double[] {16, 16, 16}, UvMode.POSITIONAL);
    }

    /** Checks one procedural model whose elements must lie within {@code min..max} (model pixels). */
    public static int check(String id, Map<String, String> variables, double[] min, double[] max) {
        return check(id, variables, min, max, UvMode.POSITIONAL);
    }

    /** Checks one Blockbench-compiled model within one block. */
    public static int checkAuthored(String id) {
        return check(id, Map.of(), new double[] {0, 0, 0}, new double[] {16, 16, 16}, UvMode.AUTHORED);
    }

    /** Checks one Blockbench-compiled model whose elements must lie within {@code min..max}. */
    public static int checkAuthored(String id, double[] min, double[] max) {
        return check(id, Map.of(), min, max, UvMode.AUTHORED);
    }

    /** Whether a texture PNG has any pixel that is not fully opaque. */
    public static boolean transparent(Path png) {
        ResourceFiles.Png image = PNGS.computeIfAbsent(png, ResourceFiles::readPng);
        for (int pixel : image.rgba()) {
            if ((pixel & 0xFF) != 255) {
                return true;
            }
        }
        return false;
    }

    public static int check(String id, Map<String, String> variables, double[] min, double[] max, UvMode mode) {
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
                assertEquals(4, uv.size(), what + ": uv needs four numbers");
                for (JsonElement u : uv) {
                    double v = u.getAsDouble();
                    assertTrue(Double.isFinite(v) && v >= 0 && v <= 16, what + ": uv outside the texture " + uv);
                }
                if (face.has("rotation")) {
                    assertTrue(Set.of(0, 90, 180, 270).contains(face.get("rotation").getAsInt()), what + ": uv rotation " + face.get("rotation"));
                }
                if (mode == UvMode.POSITIONAL && !STRETCHED.contains(variable) && !variable.startsWith("bundle_")) {
                    double[] size = faceSize(entry.getKey(), from, to);
                    assertEquals(size[0], Math.abs(uv.get(2).getAsDouble() - uv.get(0).getAsDouble()), 1e-3, what + ": u density");
                    assertEquals(size[1], Math.abs(uv.get(3).getAsDouble() - uv.get(1).getAsDouble()), 1e-3, what + ": v density");
                }
                String texture = variables.getOrDefault(variable, model.texture("#" + variable));
                assertNotNull(texture, what + ": unresolved texture");
                Path png = ModelResolver.texturePng(texture);
                assertTrue(Files.isRegularFile(png), what + ": missing " + texture);
                int[] size = ResourceFiles.pngSize(png);
                assertTrue(size[0] == size[1] && size[0] >= 16 && Integer.bitCount(size[0]) == 1, what + ": " + texture + " is " + size[0] + " x " + size[1]);
                alpha |= transparent(png);
                faces++;
            }
        }
        assertEquals(alpha, "minecraft:cutout".equals(model.renderType()), id + ": cutout exactly when a used texture has transparent pixels");
        assertNotNull(model.texture("#particle"), id + ": no particle");
        return faces;
    }
}
