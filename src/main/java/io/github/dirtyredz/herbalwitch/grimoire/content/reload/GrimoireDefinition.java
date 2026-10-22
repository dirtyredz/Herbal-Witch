package io.github.dirtyredz.herbalwitch.grimoire.content.reload;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * One editorial definition from {@code data/<ns>/grimoire_entries/**.json}: prose, grouping, links
 * and a discovery hint for an entry. Text values are translation keys; a value with no translation
 * shows as written, so a datapack can also use plain text. A definition can never state recipe
 * mechanics (ingredients, counts, outputs, time, heat): those always come from the live recipe, and
 * a file that tries is rejected. Plain Java and Gson, so validation is unit-testable.
 *
 * <pre>
 * {"entry": "guide|herbalwitch:getting_started", "category": "guide", "title": "...",
 *  "pages": ["...", ...], "links": ["herb|herbalwitch:botany/temperate/lavender/plant"], "hint": "..."}
 * </pre>
 *
 * @param key      the entry it describes (a guide exists only through its definition)
 * @param category grouping override
 * @param title    title override (required for guides)
 * @param pages    paragraphs, in order
 * @param links    related entries
 * @param hint     how to discover the entry
 */
public record GrimoireDefinition(EntryKey key, Optional<String> category, Optional<String> title, List<String> pages,
                                 List<EntryKey> links, Optional<String> hint) {
    public static final int MAX_PAGES = 16;
    public static final int MAX_LINKS = 16;
    public static final int MAX_TEXT = 512;
    public static final int MAX_DEFINITIONS = 1024;
    private static final Pattern CATEGORY = Pattern.compile("[a-z0-9_]{1,32}");
    private static final Set<String> FIELDS = Set.of("entry", "category", "title", "pages", "links", "hint");
    private static final Set<String> MECHANICS = Set.of("ingredients", "ingredient", "base", "packaging", "result", "results",
            "returns", "count", "duration", "time", "heat", "heated", "station", "type");

    public GrimoireDefinition {
        pages = List.copyOf(pages);
        links = List.copyOf(links);
    }

    /** Either a complete, valid set or every reason it was rejected. */
    public record Result(Map<EntryKey, GrimoireDefinition> definitions, List<String> errors) {
        public boolean ok() {
            return errors.isEmpty();
        }
    }

    /** Parses one definition, throwing {@link IllegalArgumentException} with the reason. */
    public static GrimoireDefinition parse(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException("not a JSON object");
        }
        JsonObject json = element.getAsJsonObject();
        for (String field : json.keySet()) {
            if (MECHANICS.contains(field)) {
                throw new IllegalArgumentException("editorial content cannot change recipe mechanics (" + field + ")");
            }
            if (!FIELDS.contains(field)) {
                throw new IllegalArgumentException("unknown field " + field);
            }
        }
        EntryKey key = key(string(json, "entry").orElseThrow(() -> new IllegalArgumentException("missing entry")));
        Optional<String> category = string(json, "category");
        category.ifPresent(c -> {
            if (!CATEGORY.matcher(c).matches()) {
                throw new IllegalArgumentException("bad category " + c);
            }
        });
        Optional<String> title = string(json, "title");
        if (key.kind() == EntryKey.Kind.GUIDE && title.isEmpty()) {
            throw new IllegalArgumentException("a guide needs a title");
        }
        List<String> pages = strings(json, "pages", MAX_PAGES);
        List<EntryKey> links = new ArrayList<>();
        for (String link : strings(json, "links", MAX_LINKS)) {
            EntryKey target = key(link);
            if (target.equals(key)) {
                throw new IllegalArgumentException("links to itself");
            }
            links.add(target);
        }
        return new GrimoireDefinition(key, category, title, pages, links, string(json, "hint"));
    }

    /**
     * Validates a whole reload: every file must parse, no entry may be defined twice, and every link
     * to a guide must name a guide in the same set (guides exist only through definitions). Links to
     * recipes, herbs and stations are checked against the live content when it is built, since
     * those come and go with datapacks. Any error rejects the entire set.
     */
    public static Result parseAll(Map<String, JsonElement> files) {
        List<String> errors = new ArrayList<>();
        Map<EntryKey, GrimoireDefinition> definitions = new LinkedHashMap<>();
        Map<EntryKey, String> origin = new LinkedHashMap<>();
        if (files.size() > MAX_DEFINITIONS) {
            errors.add(files.size() + " definitions exceed the " + MAX_DEFINITIONS + " ceiling");
            return new Result(Map.of(), errors);
        }
        new TreeMap<>(files).forEach((file, json) -> {
            try {
                GrimoireDefinition definition = parse(json);
                String previous = origin.putIfAbsent(definition.key(), file);
                if (previous != null) {
                    errors.add(file + ": " + definition.key() + " is already defined by " + previous);
                } else {
                    definitions.put(definition.key(), definition);
                }
            } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
                errors.add(file + ": " + e.getMessage());
            }
        });
        Set<EntryKey> guides = new HashSet<>();
        definitions.keySet().stream().filter(k -> k.kind() == EntryKey.Kind.GUIDE).forEach(guides::add);
        definitions.values().forEach(d -> d.links().stream()
                .filter(l -> l.kind() == EntryKey.Kind.GUIDE && !guides.contains(l))
                .forEach(l -> errors.add(origin.get(d.key()) + ": broken reference to " + l)));
        return errors.isEmpty() ? new Result(definitions, List.of()) : new Result(Map.of(), List.copyOf(errors));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("entry", key.serialized());
        category.ifPresent(c -> json.addProperty("category", c));
        title.ifPresent(t -> json.addProperty("title", t));
        if (!pages.isEmpty()) {
            JsonArray array = new JsonArray();
            pages.forEach(array::add);
            json.add("pages", array);
        }
        if (!links.isEmpty()) {
            JsonArray array = new JsonArray();
            links.forEach(l -> array.add(l.serialized()));
            json.add("links", array);
        }
        hint.ifPresent(h -> json.addProperty("hint", h));
        return json;
    }

    private static EntryKey key(String s) {
        return EntryKey.parse(s).orElseThrow(() -> new IllegalArgumentException("bad entry key " + s));
    }

    private static Optional<String> string(JsonObject json, String field) {
        if (!json.has(field)) {
            return Optional.empty();
        }
        JsonElement e = json.get(field);
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(field + " must be a string");
        }
        return Optional.of(text(e.getAsString(), field));
    }

    private static List<String> strings(JsonObject json, String field, int max) {
        if (!json.has(field)) {
            return List.of();
        }
        if (!json.get(field).isJsonArray()) {
            throw new IllegalArgumentException(field + " must be an array");
        }
        JsonArray array = json.getAsJsonArray(field);
        if (array.size() > max) {
            throw new IllegalArgumentException(field + " holds " + array.size() + " values, at most " + max);
        }
        List<String> out = new ArrayList<>();
        for (JsonElement e : array) {
            if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(field + " must hold strings");
            }
            out.add(text(e.getAsString(), field));
        }
        return out;
    }

    private static String text(String s, String field) {
        if (s.isBlank() || s.length() > MAX_TEXT) {
            throw new IllegalArgumentException(field + " must be 1 to " + MAX_TEXT + " characters");
        }
        return s;
    }
}
