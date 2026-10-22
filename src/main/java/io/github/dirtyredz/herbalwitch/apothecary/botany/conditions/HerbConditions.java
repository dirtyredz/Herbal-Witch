package io.github.dirtyredz.herbalwitch.apothecary.botany.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.Preference;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The discovery and yield preferences of every herb, as loaded from data
 * ({@code data/<ns>/herb_conditions/**.json}). Immutable; parsing validates the whole set and
 * either yields a complete replacement or a list of errors (the caller keeps the previous set).
 * Plain Java and Gson only, so it is unit-testable without Minecraft.
 */
public final class HerbConditions {
    /** Discovery chance after preference bonuses never exceeds this. */
    public static final double DISCOVERY_CAP = 1.0 / 8;
    /** Each matched preference multiplies the discovery chance by this. */
    public static final double PREFERENCE_MULTIPLIER = 2.0;
    /** Default chance of one extra fresh herb when conditions favour the herb. */
    public static final double DEFAULT_HARVEST_BONUS = 0.25;

    /**
     * @param herb               plant id, e.g. {@code herbalwitch:botany/temperate/lavender/plant}
     * @param habitat            biome tag id where the herb is discovered and favoured
     * @param discoveryChance    chance per qualifying grass break, before preferences
     * @param harvestBonusChance chance of one extra fresh herb on a favoured harvest
     * @param preferences        live conditions the herb likes
     */
    public record Condition(String herb, String habitat, double discoveryChance, double harvestBonusChance,
                            List<Preference> preferences) {
        public Condition {
            preferences = List.copyOf(preferences);
        }

        /** Discovery chance with {@code matched} preferences met, capped. */
        public double discoveryChance(int matched) {
            return Math.min(DISCOVERY_CAP, discoveryChance * Math.pow(PREFERENCE_MULTIPLIER, matched));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("herb", herb);
            json.addProperty("habitat", habitat);
            json.addProperty("discovery_chance", discoveryChance);
            json.addProperty("harvest_bonus_chance", harvestBonusChance);
            JsonArray prefers = new JsonArray();
            preferences.forEach(p -> prefers.add(p.id()));
            json.add("preferences", prefers);
            return json;
        }
    }

    /** Either a complete set or the reasons it was rejected. */
    public record Result(HerbConditions conditions, List<String> errors) {
        public boolean ok() {
            return errors.isEmpty();
        }
    }

    private final Map<String, Condition> byHerb;

    private HerbConditions(Map<String, Condition> byHerb) {
        this.byHerb = Map.copyOf(byHerb);
    }

    public static String plantId(HerbSpec herb) {
        return HerbalWitch.MOD_ID + ":" + herb.plantPath();
    }

    public static String habitatTagId(HerbSpec herb) {
        return HerbalWitch.MOD_ID + ":botany/habitat/" + herb.habitat().directory();
    }

    /** The catalogue defaults; the generated data reproduces exactly these. */
    public static HerbConditions defaults() {
        Map<String, Condition> map = new LinkedHashMap<>();
        for (HerbSpec herb : HerbCatalog.ALL) {
            map.put(plantId(herb), defaultFor(herb));
        }
        return new HerbConditions(map);
    }

    public static Condition defaultFor(HerbSpec herb) {
        return new Condition(plantId(herb), habitatTagId(herb), herb.habitat().discoveryChance(), DEFAULT_HARVEST_BONUS,
                herb.habitat().preferences());
    }

    public Condition get(HerbSpec herb) {
        return byHerb.get(plantId(herb));
    }

    public Collection<Condition> all() {
        return byHerb.values();
    }

    /**
     * Parses every file; any invalid file rejects the whole set. {@code known} holds the plant ids
     * that are registered.
     */
    public static Result parse(Map<String, JsonElement> files, Set<String> known) {
        List<String> errors = new ArrayList<>();
        Map<String, Condition> map = new TreeMap<>();
        new TreeMap<>(files).forEach((file, element) -> {
            try {
                Condition condition = parseOne(element, known);
                if (map.put(condition.herb(), condition) != null) {
                    errors.add(file + ": duplicate conditions for " + condition.herb());
                }
            } catch (IllegalArgumentException | IllegalStateException | ClassCastException | NullPointerException e) {
                errors.add(file + ": " + e.getMessage());
            }
        });
        return new Result(errors.isEmpty() ? new HerbConditions(map) : null, List.copyOf(errors));
    }

    static Condition parseOne(JsonElement element, Set<String> known) {
        JsonObject json = element.getAsJsonObject();
        String herb = required(json, "herb").getAsString();
        if (!known.contains(herb)) {
            throw new IllegalArgumentException("unknown herb " + herb);
        }
        String habitat = required(json, "habitat").getAsString();
        if (!habitat.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("bad habitat tag " + habitat);
        }
        double discovery = probability(json, "discovery_chance");
        double bonus = probability(json, "harvest_bonus_chance");
        List<Preference> preferences = new ArrayList<>();
        if (json.has("preferences")) {
            for (JsonElement p : json.getAsJsonArray("preferences")) {
                Preference preference = Preference.byId(p.getAsString());
                if (preference == null) {
                    throw new IllegalArgumentException("unknown preference " + p.getAsString());
                }
                if (!preferences.contains(preference)) {
                    preferences.add(preference);
                }
            }
        }
        return new Condition(herb, habitat, discovery, bonus, preferences);
    }

    private static JsonElement required(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("missing " + key);
        }
        return json.get(key);
    }

    private static double probability(JsonObject json, String key) {
        double value = required(json, key).getAsDouble();
        if (!(value >= 0 && value <= 1)) {
            throw new IllegalArgumentException(key + " out of range: " + value);
        }
        return value;
    }
}
