package io.github.dirtyredz.herbalwitch.data.preparations;

import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Preparations' expected generated files and names, independent of the product catalogue. */
public final class PreparationManifest {
    /** {@code <category>/<product>} -> English name, in catalogue order. */
    public static final Map<String, String> PRODUCTS = linked(
            "drinks/recovery_tea", "Recovery Tea", "drinks/energy_tea", "Energy Tea", "drinks/restful_infusion", "Restful Infusion",
            "drinks/warmth_remedy", "Warmth Remedy", "drinks/cooling_remedy", "Cooling Remedy", "drinks/mining_tonic", "Mining Tonic",
            "drinks/travel_tonic", "Travel Tonic", "salves/healing_salve", "Healing Salve", "oils/lantern_oil", "Lantern Oil",
            "oils/fishing_blend", "Fishing Blend", "garden/fertilizer", "Garden Fertilizer", "garden/flower_dust", "Flower Growth Dust",
            "garden/mushroom_dust", "Mushroom Growth Dust", "garden/growth_infusion", "Growth Infusion",
            "garden/pest_repellent", "Pest Repellent", "garden/soil_tonic", "Soil Tonic", "ingredients/plant_oil", "Bottled Plant Oil",
            "ingredients/resin", "Resin");
    public static final String LANTERN = "preparations/household/oil_lantern";

    private PreparationManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    public static String herbDir(BotanyManifest.Herb herb) {
        return "preparations/herbs/" + herb.group() + "/" + herb.id();
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        for (BotanyManifest.Herb herb : BotanyManifest.HERBS) {
            String dir = herbDir(herb);
            files.add("assets/herbalwitch/blockstates/" + dir + "/bundle.json");
            files.add("assets/herbalwitch/models/item/" + dir + "/bundle.json");
            files.add("assets/herbalwitch/models/item/" + dir + "/powder.json");
            files.add("data/herbalwitch/loot_tables/blocks/" + dir + "/bundle.json");
        }
        PRODUCTS.keySet().forEach(p -> files.add("assets/herbalwitch/models/item/preparations/" + p + ".json"));
        files.add("assets/herbalwitch/blockstates/" + LANTERN + ".json");
        files.add("assets/herbalwitch/models/item/" + LANTERN + ".json");
        files.add("data/herbalwitch/loot_tables/blocks/" + LANTERN + ".json");
        files.add("data/herbalwitch/recipes/" + LANTERN + ".json");
        files.add("data/herbalwitch/advancements/recipes/decorations/" + LANTERN + ".json");
        files.add("assets/herbalwitch/lang/en_us.json");
        files.add("data/herbalwitch/tags/blocks/preparations/bundles.json");
        files.add("data/herbalwitch/tags/blocks/preparations/resin_sources.json");
        files.add("data/herbalwitch/tags/items/preparations/bundles.json");
        files.add("data/herbalwitch/tags/items/preparations/powders.json");
        files.add("data/herbalwitch/tags/entity_types/repelled_pests.json");
        files.add("data/minecraft/tags/blocks/mineable/pickaxe.json");
        files.add("data/herbalwitch/loot_modifiers/preparations/resin.json");
        files.add("data/forge/loot_modifiers/global_loot_modifiers.json");
        return files;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (BotanyManifest.Herb herb : BotanyManifest.HERBS) {
            String key = herbDir(herb).replace('/', '.');
            lang.put("block.herbalwitch." + key + ".bundle", "Dried " + herb.name() + " Bundle");
            lang.put("item.herbalwitch." + key + ".powder", "Ground " + herb.name());
        }
        PRODUCTS.forEach((path, name) -> {
            String key = "item.herbalwitch.preparations." + path.replace('/', '.');
            lang.put(key, name);
            lang.put(key + ".tooltip", null);
        });
        lang.put("block.herbalwitch." + LANTERN.replace('/', '.'), "Oil Lantern");
        lang.put("effect.herbalwitch.warmth", "Warmth");
        lang.put("effect.herbalwitch.pest_repellent", "Pest Repellent");
        for (String key : List.of("section.preparations", "label.powders", "label.products", "label.oil_lantern")) {
            lang.put("showcase.herbalwitch." + key, null);
        }
        return lang;
    }
}
