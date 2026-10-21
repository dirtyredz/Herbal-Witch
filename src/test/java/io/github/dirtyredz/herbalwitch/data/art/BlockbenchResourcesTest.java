package io.github.dirtyredz.herbalwitch.data.art;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Blockbench sources and what they compile to, against the independent BlockbenchManifest:
 * the catalogue lists exactly the twelve projects at the pinned format, the compiler's model
 * directories hold exactly the 32 compiled models, the owned textures exist, and no source file or
 * compiled path leaks into the runtime resources, the datagen tree or a procedural directory.
 */
class BlockbenchResourcesTest {
    private static final Path SOURCES = Path.of("src/art/blockbench");
    private static final Path MODELS = ResourceFiles.MAIN_ASSETS.resolve("models");
    /** Model directories the procedural generator owns (tools/models/catalogue.mjs OWNED, by root). */
    private static final List<String> PROCEDURAL = List.of("block/botany", "block/preparations/bundles");

    private static List<String> strings(JsonElement array) {
        List<String> out = new ArrayList<>();
        array.getAsJsonArray().forEach(e -> out.add(e.getAsString()));
        return out;
    }

    private static Set<String> files(Path root, String suffix) {
        if (!Files.isDirectory(root)) {
            return Set.of();
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return new TreeSet<>(walk.filter(Files::isRegularFile).map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .filter(p -> p.endsWith(suffix)).toList());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void theCatalogueListsTheTwelveProjectsAtThePinnedFormat() {
        JsonObject catalog = ResourceFiles.json(SOURCES.resolve("catalog.json"));
        assertEquals("5.2.2", catalog.get("blockbench").getAsString());
        assertEquals("5.0", catalog.get("format_version").getAsString());
        assertEquals("1.9.0", catalog.get("java_block_version").getAsString());
        assertEquals(BlockbenchManifest.PROJECTS, strings(catalog.get("projects")));
        assertEquals(BlockbenchManifest.MODEL_DIRS, strings(catalog.get("model_dirs")));
        for (String dir : BlockbenchManifest.PROJECTS) {
            JsonObject piece = ResourceFiles.json(SOURCES.resolve(dir).resolve("piece.json"));
            String project = piece.get("project").getAsString();
            assertEquals(dir.substring(dir.indexOf('/') + 1) + ".bbmodel", project, dir);
            JsonObject bbmodel = ResourceFiles.json(SOURCES.resolve(dir).resolve(project));
            JsonObject meta = bbmodel.getAsJsonObject("meta");
            assertEquals("5.0", meta.get("format_version").getAsString(), dir);
            assertEquals("java_block", meta.get("model_format").getAsString(), dir);
            assertEquals("1.9.0", bbmodel.get("java_block_version").getAsString(), dir);
            assertEquals(Set.of("piece.json", project), files(SOURCES.resolve(dir), ""), dir + " holds only its project and descriptor");
        }
    }

    @Test
    void theCompilerOwnsExactlyTheCompiledModels() {
        Set<String> actual = new TreeSet<>();
        for (String dir : BlockbenchManifest.MODEL_DIRS) {
            files(MODELS.resolve(dir), ".json").forEach(f -> actual.add(dir + "/" + f));
            for (String procedural : PROCEDURAL) {
                assertFalse(dir.startsWith(procedural) || procedural.startsWith(dir), dir + " overlaps the procedural " + procedural);
            }
        }
        assertEquals(new TreeSet<>(BlockbenchManifest.models()), actual);
        assertEquals(32, actual.size());
        for (String model : BlockbenchManifest.models()) {
            assertFalse(Files.exists(ResourceFiles.GENERATED_ASSETS.resolve("models/" + model)), model + " also in the datagen tree");
        }
    }

    @Test
    void ownedTexturesExistAndAreDeclaredByTheirProjects() {
        Set<String> declared = new TreeSet<>();
        for (String dir : BlockbenchManifest.PROJECTS) {
            JsonObject piece = ResourceFiles.json(SOURCES.resolve(dir).resolve("piece.json"));
            if (piece.has("owned_textures")) {
                piece.getAsJsonObject("owned_textures").entrySet()
                        .forEach(e -> declared.add(e.getValue().getAsString().replace("herbalwitch:", "") + ".png"));
            }
        }
        assertEquals(new TreeSet<>(BlockbenchManifest.TEXTURES), declared);
        for (String texture : BlockbenchManifest.TEXTURES) {
            assertTrue(Files.isRegularFile(ResourceFiles.MAIN_ASSETS.resolve("textures/" + texture)), texture);
            assertFalse(Files.exists(ResourceFiles.GENERATED_ASSETS.resolve("textures/" + texture)), texture + " in the datagen tree");
        }
    }

    @Test
    void sourcesNeverEnterTheRuntimeResources() {
        for (Path tree : List.of(Path.of("src/main/resources"), ResourceFiles.GENERATED)) {
            assertEquals(Set.of(), files(tree, ".bbmodel"), tree + " holds Blockbench sources");
            assertEquals(Set.of(), files(tree, "piece.json"), tree + " holds piece descriptors");
        }
    }
}
