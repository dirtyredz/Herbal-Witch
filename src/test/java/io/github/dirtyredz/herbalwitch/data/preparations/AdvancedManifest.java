package io.github.dirtyredz.herbalwitch.data.preparations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Milestone 7's player, familiar and mixture preparations, their witch and bottling recipes, entity
 * and item tags and language, written out independently of the product catalogues and the recipe
 * providers. Item ids are {@code preparations/<path>}; recipe paths sit under {@code processing/}.
 */
public final class AdvancedManifest {
    /** Player preparations: path under preparations/ -> English name (16). */
    public static final Map<String, String> ADVANCED = linked(
            "advanced/drinks/night_vision", "Night Vision Draught", "advanced/drinks/water_breathing", "Water Breathing Draught",
            "advanced/drinks/resistance", "Resistance Draught", "advanced/drinks/healing", "Healing Draught",
            "advanced/throwables/poison", "Poison Vial", "advanced/throwables/concealment", "Concealment Vial",
            "advanced/throwables/slowing", "Slowing Vial", "advanced/throwables/binding", "Binding Vial",
            "advanced/throwables/weakening", "Weakening Vial", "advanced/throwables/sleep", "Sleep Vial",
            "advanced/oils/venom", "Venom Oil", "advanced/oils/thorn", "Thorn Oil",
            "advanced/oils/undead_repellent", "Undead Repellent", "advanced/oils/arthropod_repellent", "Arthropod Repellent",
            "advanced/dusts/blinding", "Blinding Dust", "advanced/essences/moon", "Moon Essence");
    /** Familiar preparations (10). */
    public static final Map<String, String> FAMILIAR = linked(
            "familiar/tonics/guardian", "Guardian's Brew", "familiar/tonics/swiftpaw", "Swiftpaw Tonic",
            "familiar/tonics/nightwatch", "Nightwatch Infusion", "familiar/tonics/moonveil", "Moonveil Elixir",
            "familiar/tonics/spiritbond", "Spiritbond Potion", "familiar/tonics/hearthkeeper", "Hearthkeeper's Brew",
            "familiar/tonics/shadowstep", "Shadowstep Draught", "familiar/essences/warding", "Warding Essence",
            "familiar/essences/venomleaf", "Venomleaf Extract", "familiar/oils/emberguard", "Emberguard Oil");
    /** The cauldron's mixtures (17): path under preparations/ -> English name. */
    public static final Map<String, String> MIXTURES = linked(
            "mixtures/utility/night_vision", "Night Vision Mixture", "mixtures/utility/water_breathing", "Water Breathing Mixture",
            "mixtures/utility/resistance", "Resistance Mixture", "mixtures/utility/healing", "Healing Mixture",
            "mixtures/combat/poison", "Poison Mixture", "mixtures/combat/concealment", "Concealment Mixture",
            "mixtures/combat/slowing", "Slowing Mixture", "mixtures/combat/binding", "Binding Mixture",
            "mixtures/combat/weakening", "Weakening Mixture", "mixtures/combat/sleep", "Sleep Mixture",
            "mixtures/familiar/guardian", "Guardian's Brew Mixture", "mixtures/familiar/swiftpaw", "Swiftpaw Tonic Mixture",
            "mixtures/familiar/nightwatch", "Nightwatch Infusion Mixture", "mixtures/familiar/moonveil", "Moonveil Elixir Mixture",
            "mixtures/familiar/spiritbond", "Spiritbond Potion Mixture", "mixtures/familiar/hearthkeeper", "Hearthkeeper's Brew Mixture",
            "mixtures/familiar/shadowstep", "Shadowstep Draught Mixture");
    /** Mixture group/product -> the bottled product's path under preparations/ (bottling bench, ordinary work). */
    public static final Map<String, String> BOTTLED = bottled();
    /** Vial mixtures also take gunpowder. */
    public static final Set<String> THROWN = Set.of("combat/poison", "combat/concealment", "combat/slowing", "combat/binding",
            "combat/weakening", "combat/sleep");

    /** Witch recipes: path under recipes/processing/ -> energy per batch (before the server multiplier). */
    public static final Map<String, Integer> WITCH_ENERGY = witchEnergy();

    public static final List<String> EFFECTS = List.of("binding", "sleep", "steadfast", "concealment", "dazzled", "thorns",
            "undead_repellent", "arthropod_repellent");
    public static final List<String> ENTITY_TAGS = List.of("undead", "arthropods", "hard_control_immune", "hard_control_allowed",
            "protected_entities");

    private AdvancedManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private static Map<String, String> bottled() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String drink : List.of("night_vision", "water_breathing", "resistance", "healing")) {
            map.put("utility/" + drink, "advanced/drinks/" + drink);
        }
        for (String vial : List.of("poison", "concealment", "slowing", "binding", "weakening", "sleep")) {
            map.put("combat/" + vial, "advanced/throwables/" + vial);
        }
        for (String tonic : List.of("guardian", "swiftpaw", "nightwatch", "moonveil", "spiritbond", "hearthkeeper", "shadowstep")) {
            map.put("familiar/" + tonic, "familiar/tonics/" + tonic);
        }
        return map;
    }

    private static Map<String, Integer> witchEnergy() {
        Map<String, Integer> map = new LinkedHashMap<>();
        Object[] cauldron = {"utility/night_vision", 5, "utility/water_breathing", 5, "utility/resistance", 10, "utility/healing", 10,
                "combat/poison", 10, "combat/concealment", 10, "combat/slowing", 10, "combat/binding", 15, "combat/weakening", 10,
                "combat/sleep", 15, "familiar/guardian", 10, "familiar/swiftpaw", 10, "familiar/nightwatch", 10,
                "familiar/moonveil", 10, "familiar/spiritbond", 15, "familiar/hearthkeeper", 10, "familiar/shadowstep", 15};
        for (int i = 0; i < cauldron.length; i += 2) {
            map.put("cauldron/" + cauldron[i], (Integer) cauldron[i + 1]);
        }
        map.put("distillation/moon", 10);
        map.put("distillation/warding", 15);
        map.put("distillation/venomleaf", 15);
        map.put("grinding/blinding_dust", 5);
        map.put("infusion/oils/venom", 10);
        map.put("infusion/oils/thorn", 10);
        map.put("infusion/oils/undead_repellent", 8);
        map.put("infusion/oils/arthropod_repellent", 8);
        map.put("infusion/oils/emberguard", 10);
        return map;
    }

    /** Every Milestone 7 preparation item path (under preparations/): 16 + 10 + 17 = 43. */
    public static Set<String> items() {
        Set<String> items = new TreeSet<>(ADVANCED.keySet());
        items.addAll(FAMILIAR.keySet());
        items.addAll(MIXTURES.keySet());
        return items;
    }

    /** Ordinary bottling recipes (path under recipes/processing/): a plain and a labelled one per mixture. */
    public static Set<String> bottlingRecipes() {
        Set<String> recipes = new TreeSet<>();
        for (String mixture : BOTTLED.keySet()) {
            recipes.add("bottling/" + mixture + "/plain");
            recipes.add("bottling/" + mixture + "/labelled");
        }
        return recipes;
    }

    /** Every Milestone 7 processing recipe of the preparations (witch recipes and bottling), under recipes/processing/. */
    public static Set<String> processingRecipes() {
        Set<String> recipes = new TreeSet<>(WITCH_ENERGY.keySet());
        recipes.addAll(bottlingRecipes());
        return recipes;
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        items().forEach(i -> files.add("assets/herbalwitch/models/item/preparations/" + i + ".json"));
        ENTITY_TAGS.forEach(t -> files.add("data/herbalwitch/tags/entity_types/" + t + ".json"));
        files.add("data/herbalwitch/tags/items/coatable_weapons.json");
        return files;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (Map<String, String> group : List.of(ADVANCED, FAMILIAR, MIXTURES)) {
            group.forEach((path, name) -> {
                String key = "item.herbalwitch.preparations." + path.replace('/', '.');
                lang.put(key, name);
                lang.put(key + ".tooltip", null);
            });
        }
        EFFECTS.forEach(e -> lang.put("effect.herbalwitch.brewing.effects." + e, null));
        for (String key : List.of("label", "oil.no_weapon", "throw.limit", "familiar.unavailable")) {
            lang.put("item.herbalwitch.preparations." + key, null);
        }
        for (String page : List.of("preparations.title", "preparations.0", "preparations.drinks", "preparations.throwing",
                "preparations.control", "preparations.sight", "preparations.oils", "preparations.consent",
                "familiar.title", "familiar.0", "familiar.deferred")) {
            lang.put("grimoire.herbalwitch.witchcraft." + page, null);
        }
        lang.put("showcase.herbalwitch.section.advanced_preparations", "Witch's Preparations");
        for (String topic : List.of("player", "familiar", "mixtures")) {
            lang.put("showcase.herbalwitch.label.advanced_preparations." + topic, null);
            lang.put("showcase.herbalwitch.label.advanced_preparations." + topic + ".detail", null);
        }
        return lang;
    }
}
