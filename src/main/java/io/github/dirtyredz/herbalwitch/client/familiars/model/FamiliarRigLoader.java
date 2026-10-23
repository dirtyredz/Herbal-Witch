package io.github.dirtyredz.herbalwitch.client.familiars.model;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import java.io.*;
import java.util.*;

/** Reads the bounded compiled entity contract; no Blockbench parser or animation library at runtime. */
public final class FamiliarRigLoader {
    public record Rig(ModelPart root, Map<String, ModelPart> bones, List<ModelPart> equipmentPath, Map<String, ResourceLocation> appearances) {}
    private FamiliarRigLoader() {}
    public static Rig load(String species) {
        var location = new ResourceLocation("herbalwitch", "entity_models/familiars/" + species + "/rig.json");
        try (var reader = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).openAsReader()) {
            return bake(species, JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | RuntimeException invalid) { throw new IllegalStateException("Cannot load familiar rig " + location, invalid); }
    }
    /** Headless geometry seam: uses the same ModelParts and UVs as the live renderer. */
    public static Rig bake(String species, JsonObject json) {
            if (json.get("schema").getAsInt() != 1 || !species.equals(json.get("species").getAsString())) throw new IllegalArgumentException("Rig identity/schema");
            var mesh = new MeshDefinition(); var origin = mesh.getRoot().addOrReplaceChild("rig", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
            Map<String, PartDefinition> parts = new LinkedHashMap<>(); Map<String, float[]> pivots = new HashMap<>(); Map<String, String> parents = new HashMap<>();
            var bones = json.getAsJsonArray("bones"); if (bones.size() > 64) throw new IllegalArgumentException("Rig exceeds bone limit"); int cubes = 0;
            for (var entry : bones) {
                var bone = entry.getAsJsonObject(); var name = bone.get("name").getAsString();
                if (!name.matches("[a-z][a-z0-9_]{0,31}") || parts.containsKey(name)) throw new IllegalArgumentException("Invalid bone name");
                String parent = bone.get("parent").isJsonNull() ? null : bone.get("parent").getAsString();
                var target = parent == null ? origin : Objects.requireNonNull(parts.get(parent), "Parent must precede child");
                float[] pivot = vector(bone.getAsJsonArray("pivot")), rotation = vector(bone.getAsJsonArray("rotation")), previous = parent == null ? new float[3] : pivots.get(parent);
                var builder = CubeListBuilder.create();
                for (var element : bone.getAsJsonArray("cubes")) {
                    if (++cubes > 128) throw new IllegalArgumentException("Rig exceeds cube limit");
                    var cube = element.getAsJsonObject(); float[] from = vector(cube.getAsJsonArray("from")), to = vector(cube.getAsJsonArray("to")); var uv = cube.getAsJsonArray("uv");
                    if (uv.size() != 2 || uv.get(0).getAsInt() < 0 || uv.get(1).getAsInt() < 0) throw new IllegalArgumentException("Invalid box UV");
                    builder.texOffs(uv.get(0).getAsInt(), uv.get(1).getAsInt()).mirror(cube.get("mirror").getAsBoolean())
                            .addBox(from[0] - pivot[0], pivot[1] - to[1], from[2] - pivot[2], to[0] - from[0], to[1] - from[1], to[2] - from[2]);
                }
                var part = target.addOrReplaceChild(name, builder, PartPose.offsetAndRotation(pivot[0] - previous[0], previous[1] - pivot[1], pivot[2] - previous[2], radians(-rotation[0]), radians(rotation[1]), radians(-rotation[2])));
                parts.put(name, part); pivots.put(name, pivot); parents.put(name, parent);
            }
            var root = LayerDefinition.create(mesh, 32, 32).bakeRoot().getChild("rig"); Map<String, ModelPart> baked = new LinkedHashMap<>();
            for (var name : parts.keySet()) baked.put(name, (parents.get(name) == null ? root : baked.get(parents.get(name))).getChild(name));
            var path = new ArrayList<ModelPart>(); String name = json.get("equipmentBone").getAsString();
            while (name != null) { path.add(Objects.requireNonNull(baked.get(name), "Missing equipment bone")); name = parents.get(name); }
            path.add(root); Collections.reverse(path);
            Map<String, ResourceLocation> appearances = new LinkedHashMap<>();
            json.getAsJsonObject("appearances").entrySet().forEach(e -> appearances.put(e.getKey(), new ResourceLocation(e.getValue().getAsString())));
            return new Rig(root, Map.copyOf(baked), List.copyOf(path), Map.copyOf(appearances));
    }
    private static float[] vector(JsonArray array) {
        if (array.size() != 3) throw new IllegalArgumentException("Expected vector"); var result = new float[3];
        for (int i = 0; i < 3; i++) { result[i] = array.get(i).getAsFloat(); if (!Float.isFinite(result[i]) || Math.abs(result[i]) > 128) throw new IllegalArgumentException("Unbounded rig vector"); }
        return result;
    }
    private static float radians(float degrees) { return (float) Math.toRadians(degrees); }
}
