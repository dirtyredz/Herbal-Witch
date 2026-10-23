package io.github.dirtyredz.herbalwitch.data;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Localisation completeness (Milestone 12) for the one shipped locale. Registry-derived names (blocks,
 * items, entities, effects, tabs) are checked against the live registries by
 * {@code TranslationRegistryGameTests}; this test covers what the source spells out: every complete
 * key literal resolves, every key-prefix literal has at least one entry, and every value is a
 * non-blank template that Minecraft's translator can format. Arbitrary computed keys (enum or record
 * names appended to a prefix) are covered by their prefix only.
 */
class TranslationCoverageTest {
    private static final Path LANG = ResourceFiles.GENERATED_ASSETS.resolve("lang");
    private static final Path SOURCES = Path.of("src/main/java/io/github/dirtyredz/herbalwitch");
    // A Java string literal made of dotted lowercase segments, one of which is the mod id.
    private static final Pattern LITERAL = Pattern.compile("\"((?:[a-z0-9_]+\\.)+[a-z0-9_]*)\"(\\s*\\+)?");
    // The template tokens TranslatableContents understands: %s, %n$s and %%; any other letter after % fails to format.
    private static final Pattern TOKEN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    @Test
    void englishIsTheOnlyShippedLocale() throws IOException {
        try (Stream<Path> files = Files.list(LANG)) {
            assertEquals(List.of("en_us.json"), files.map(p -> p.getFileName().toString()).toList(),
                    "a new locale needs the same keys and placeholders checked here");
        }
    }

    @Test
    void everyKeyTheSourceSpellsOutExists() {
        JsonObject lang = english();
        List<String> missing = new ArrayList<>();
        List<String> orphanPrefixes = new ArrayList<>();
        literals().forEach((key, where) -> {
            boolean prefix = key.endsWith(".") || where.concatenated;
            if (prefix) {
                if (lang.keySet().stream().noneMatch(k -> k.startsWith(key))) orphanPrefixes.add(key + " (" + where.file + ")");
            } else if (!lang.has(key)) {
                missing.add(key + " (" + where.file + ")");
            }
        });
        assertTrue(missing.isEmpty(), "translation keys used by the source but absent from en_us.json: " + missing);
        assertTrue(orphanPrefixes.isEmpty(), "key prefixes with no entry at all: " + orphanPrefixes);
    }

    @Test
    void everyValueIsANonBlankFormattableTemplate() {
        List<String> bad = new ArrayList<>();
        for (var entry : english().entrySet()) {
            String value = entry.getValue().getAsString();
            if (value.isBlank()) bad.add(entry.getKey() + " is blank");
            Matcher token = TOKEN.matcher(value);
            while (token.find()) {
                String kind = token.group(2);
                if (!kind.equals("s") && !kind.equals("%")) bad.add(entry.getKey() + " uses %" + kind + " (only %s, %n$s and %% format)");
            }
        }
        assertTrue(bad.isEmpty(), String.join("; ", bad));
    }

    private record Where(String file, boolean concatenated) {
    }

    private static JsonObject english() {
        return ResourceFiles.json(LANG.resolve("en_us.json"));
    }

    /** Every literal in main sources with a "herbalwitch" segment, except config, property and logger names. */
    private static Map<String, Where> literals() {
        Map<String, Where> found = new TreeMap<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                Matcher literal = LITERAL.matcher(source);
                while (literal.find()) {
                    String key = literal.group(1);
                    if (!List.of(key.split("\\.")).contains("herbalwitch") || notATranslation(source, literal.start())) continue;
                    found.merge(key, new Where(SOURCES.relativize(file).toString(), literal.group(2) != null),
                            (a, b) -> new Where(a.file, a.concatenated || b.concatenated));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return found;
    }

    /** System properties and similar dotted names that are not language keys. */
    private static boolean notATranslation(String source, int at) {
        String before = source.substring(Math.max(0, at - 40), at);
        return before.contains("getProperty(") || before.contains("getLogger(");
    }
}
