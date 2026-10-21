package io.github.dirtyredz.herbalwitch.data.fixture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Resolves models the way the game does: the parent chain across the generated tree and the
 * authored models, merged texture variables, display and render type. Vanilla parents
 * ({@code minecraft:...}) end the chain; they are recorded but not read.
 */
public final class ModelResolver {
    private ModelResolver() {
    }

    /** A model after inheritance: nearest elements, merged textures and display, render type. */
    public record Resolved(JsonArray elements, Map<String, String> textures, JsonObject display, String renderType,
                           List<String> chain) {
        public String texture(String ref) {
            String value = ref;
            for (int i = 0; i < 10 && value != null && value.startsWith("#"); i++) {
                value = textures.get(value.substring(1));
            }
            return value;
        }

        /** The last model in the chain: the root parent. */
        public String root() {
            return chain.get(chain.size() - 1);
        }
    }

    public static Path modelFile(String id) {
        String[] parts = id.split(":", 2);
        Path generated = ResourceFiles.GENERATED_ASSETS.resolve("models/" + parts[1] + ".json");
        return Files.isRegularFile(generated) ? generated : ResourceFiles.MAIN_ASSETS.resolve("models/" + parts[1] + ".json");
    }

    public static Resolved resolve(String id) {
        List<JsonObject> chain = new ArrayList<>();
        List<String> names = new ArrayList<>();
        String next = id.contains(":") ? id : "minecraft:" + id;
        while (next != null) {
            names.add(next);
            if (next.startsWith("minecraft:")) {
                break;
            }
            Path file = modelFile(next);
            assertTrue(Files.isRegularFile(file), "missing model " + next);
            JsonObject model = ResourceFiles.json(file);
            chain.add(model);
            next = model.has("parent") ? qualify(model.get("parent").getAsString()) : null;
        }
        JsonArray elements = null;
        Map<String, String> textures = new HashMap<>();
        JsonObject display = new JsonObject();
        String renderType = null;
        for (int i = chain.size() - 1; i >= 0; i--) { // root parent first, children override
            JsonObject model = chain.get(i);
            if (model.has("elements")) {
                elements = model.getAsJsonArray("elements");
            }
            if (model.has("textures")) {
                model.getAsJsonObject("textures").entrySet().forEach(e -> textures.put(e.getKey(), e.getValue().getAsString()));
            }
            if (model.has("display")) {
                model.getAsJsonObject("display").entrySet().forEach(e -> display.add(e.getKey(), e.getValue()));
            }
            if (model.has("render_type")) {
                renderType = model.get("render_type").getAsString();
            }
        }
        return new Resolved(elements == null ? new JsonArray() : elements, textures, display, renderType, names);
    }

    private static String qualify(String id) {
        return id.contains(":") ? id : "minecraft:" + id;
    }

    /** The PNG a resolved texture id points at in the authored tree. */
    public static Path texturePng(String textureId) {
        String[] parts = textureId.split(":", 2);
        return ResourceFiles.MAIN_ASSETS.resolve("textures/" + parts[1] + ".png");
    }

    public static double[] vec(JsonObject element, String key) {
        JsonArray a = element.getAsJsonArray(key);
        return new double[] {a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble()};
    }

    /** Face size in model pixels along its (u, v) axes. */
    public static double[] faceSize(String face, double[] from, double[] to) {
        double dx = to[0] - from[0];
        double dy = to[1] - from[1];
        double dz = to[2] - from[2];
        return switch (face) {
            case "north", "south" -> new double[] {dx, dy};
            case "east", "west" -> new double[] {dz, dy};
            case "up", "down" -> new double[] {dx, dz};
            default -> throw new AssertionError(face);
        };
    }

}
