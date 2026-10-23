package io.github.dirtyredz.herbalwitch.data;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import io.github.dirtyredz.herbalwitch.data.decor.DecorManifest;
import io.github.dirtyredz.herbalwitch.data.grimoire.GrimoireManifest;
import io.github.dirtyredz.herbalwitch.data.magic.charm.CharmManifest;
import io.github.dirtyredz.herbalwitch.data.magic.ritual.AltarManifest;
import io.github.dirtyredz.herbalwitch.data.magic.ritual.CircleManifest;
import io.github.dirtyredz.herbalwitch.data.magic.ward.WardManifest;
import io.github.dirtyredz.herbalwitch.data.magic.witch.WitchManifest;
import io.github.dirtyredz.herbalwitch.data.preparations.AdvancedManifest;
import io.github.dirtyredz.herbalwitch.data.preparations.PreparationManifest;
import io.github.dirtyredz.herbalwitch.data.showcase.ShowcaseManifest;
import io.github.dirtyredz.herbalwitch.data.stations.StationManifest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.GENERATED;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generatedFiles;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The whole checked-in generated tree, without starting Minecraft: it is exactly the union of the
 * feature manifests (so any stale or missing file fails), every directory stays under twelve
 * entries, the language file holds exactly the features' keys, and nothing references a namespace
 * outside the allowed set. Feature-specific content checks live in each feature's resources test.
 */
class GeneratedResourcesTest {
    private static final Set<String> ALLOWED_NAMESPACES = Set.of("minecraft", "herbalwitch", "forge");
    /** Files allowed to name an optional mod: the heat tags (as optional entries) and Farmer's Delight's own tag. */
    private static final Map<String, Set<String>> OPTIONAL_REFERENCES = Map.of(
            "data/herbalwitch/tags/blocks/heat_sources.json", Set.of("farmersdelight"),
            "data/herbalwitch/tags/blocks/heat_conductors.json", Set.of("farmersdelight"),
            "data/farmersdelight/tags/blocks/heat_sources.json", Set.of("farmersdelight"));
    private static final Pattern NAMESPACED = Pattern.compile("\"#?([a-z0-9_.-]+):[a-z0-9_./-]+\"");

    static Map<String, String> expectedLang() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (Map<String, String> part : List.of(DecorManifest.lang(), BotanyManifest.lang(), StationManifest.lang(),
                PreparationManifest.lang(), ShowcaseManifest.lang(), GrimoireManifest.lang(), WitchManifest.lang(), AltarManifest.lang(),
                AdvancedManifest.lang(), CharmManifest.lang(), CircleManifest.lang(), WardManifest.lang(),
                io.github.dirtyredz.herbalwitch.data.animals.AnimalManifest.lang(), io.github.dirtyredz.herbalwitch.data.familiars.FamiliarManifest.lang())) {
            part.forEach((key, value) -> {
                assertFalse(lang.containsKey(key), "two features claim " + key);
                lang.put(key, value);
            });
        }
        return lang;
    }

    @Test
    void generatedTreeIsExactlyTheUnionOfTheFeatureManifests() {
        Set<String> expected = new TreeSet<>();
        expected.addAll(DecorManifest.files());
        expected.addAll(BotanyManifest.files());
        expected.addAll(StationManifest.files());
        expected.addAll(PreparationManifest.files());
        expected.addAll(GrimoireManifest.files());
        expected.addAll(WitchManifest.files());
        expected.addAll(AltarManifest.files());
        expected.addAll(AdvancedManifest.files());
        expected.addAll(CharmManifest.files());
        expected.addAll(CircleManifest.files());
        expected.addAll(WardManifest.files());
        expected.addAll(io.github.dirtyredz.herbalwitch.data.animals.AnimalManifest.files());
        expected.addAll(io.github.dirtyredz.herbalwitch.data.familiars.FamiliarManifest.files());
        assertEquals(expected, generatedFiles());
    }

    /**
     * Every generated directory has fewer than twelve entries, except the enumerated registry-derived
     * decor directories (blockstates, item models and loot of a furniture family: their paths are
     * permanent block ids, decor/&lt;family&gt;/&lt;piece&gt;, so they cannot be grouped), and those
     * hold exactly the decor manifest's files: an unexpected file there still fails.
     */
    @Test
    void everyGeneratedDirectoryHasFewerThanTwelveEntries() throws IOException {
        Map<String, Set<String>> entries = new HashMap<>();
        try (Stream<Path> walk = Files.walk(GENERATED)) {
            // Datagen removes stale files but can leave empty directories, which Git never records.
            // Count every ancestor of an actual generated file, so local empty remnants are irrelevant.
            walk.filter(Files::isRegularFile).filter(p -> !GENERATED.relativize(p).toString().startsWith(".cache"))
                    .forEach(file -> {
                        for (Path p = file; !p.equals(GENERATED); p = p.getParent())
                            entries.computeIfAbsent(GENERATED.relativize(p.getParent()).toString().replace('\\', '/'), k -> new TreeSet<>())
                                    .add(p.getFileName().toString());
                    });
        }
        entries.forEach((dir, names) -> {
            if (DecorManifest.REGISTRY_DIRECTORIES.contains(dir)) {
                assertEquals(DecorManifest.entries(dir), names, dir + " holds exactly its registered pieces");
            } else {
                assertTrue(names.size() < 12, dir + " has " + names.size() + " entries");
            }
        });
    }

    @Test
    void languageFileHoldsExactlyTheFeatureKeys() {
        JsonObject lang = json(GENERATED.resolve("assets/herbalwitch/lang/en_us.json"));
        Map<String, String> expected = expectedLang();
        assertEquals(new TreeSet<>(expected.keySet()), new TreeSet<>(lang.keySet()));
        expected.forEach((key, value) -> {
            String actual = lang.get(key).getAsString();
            assertFalse(actual.isBlank(), key);
            if (value != null) {
                assertEquals(value, actual, key);
            }
        });
    }

    @Test
    void optionalModReferencesOnlyWhereDeclared() throws IOException {
        for (String rel : generatedFiles()) {
            String text = Files.readString(GENERATED.resolve(rel));
            Matcher m = NAMESPACED.matcher(text);
            while (m.find()) {
                String namespace = m.group(1);
                assertTrue(ALLOWED_NAMESPACES.contains(namespace) || OPTIONAL_REFERENCES.getOrDefault(rel, Set.of()).contains(namespace),
                        rel + " references " + m.group());
            }
            if (rel.startsWith("data/herbalwitch/") && text.contains("farmersdelight:")) {
                assertTrue(text.contains("\"required\": false"), rel + ": Farmer's Delight references must be optional");
            }
            assertFalse(rel.endsWith(".png"), "textures come from the art pipeline (tools/generate-textures.mjs, tools/compile-blockbench.mjs), never datagen: " + rel);
        }
    }
}
