package io.github.dirtyredz.herbalwitch.data.stations;

import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The stations' expected generated files, recipe matrix and names, written out independently of
 * StationCatalog and the recipe providers.
 */
public final class StationManifest {
    /** station directory -> English name, in catalogue order. */
    public static final Map<String, String> STATIONS = linked("drying_rack", "Herb Drying Rack", "mortar", "Mortar and Pestle",
            "kettle", "Tea Kettle", "hearth", "Hearth", "infusion_pot", "Infusion Pot");
    public static final List<String> TYPES = List.of("drying", "grinding", "steeping", "infusion");
    public static final List<String> GRINDING_EXTRAS = List.of("plant_oil", "fertilizer", "flower_dust", "mushroom_dust",
            "purple_dye", "green_dye", "yellow_dye", "black_dye", "bone_meal");
    public static final List<String> DRINKS = List.of("recovery_tea", "energy_tea", "restful_infusion", "warmth_remedy",
            "cooling_remedy", "mining_tonic", "travel_tonic");
    public static final List<String> INFUSIONS = List.of("healing_salve", "lantern_oil", "fishing_blend", "growth_infusion",
            "pest_repellent", "soil_tonic");

    private StationManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    public static String station(String directory) {
        return "stations/" + directory + "/station";
    }

    /** Every processing recipe path under {@code recipes/}. */
    public static Set<String> processingRecipes() {
        Set<String> recipes = new TreeSet<>();
        for (BotanyManifest.Herb herb : BotanyManifest.HERBS) {
            recipes.add("processing/drying/" + herb.group() + "/" + herb.id());
            recipes.add("processing/grinding/herbs/" + herb.group() + "/" + herb.id());
        }
        GRINDING_EXTRAS.forEach(r -> recipes.add("processing/grinding/" + r));
        DRINKS.forEach(r -> recipes.add("processing/steeping/" + r));
        INFUSIONS.forEach(r -> recipes.add("processing/infusion/" + r));
        return recipes;
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        for (String directory : STATIONS.keySet()) {
            String id = station(directory);
            files.add("assets/herbalwitch/blockstates/" + id + ".json");
            files.add("assets/herbalwitch/models/item/" + id + ".json");
            files.add("data/herbalwitch/loot_tables/blocks/" + id + ".json");
            files.add("data/herbalwitch/recipes/" + id + ".json");
            files.add("data/herbalwitch/advancements/recipes/decorations/" + id + ".json");
        }
        processingRecipes().forEach(r -> files.add("data/herbalwitch/recipes/" + r + ".json"));
        files.add("assets/herbalwitch/lang/en_us.json");
        files.add("data/herbalwitch/tags/blocks/heat_sources.json");
        files.add("data/herbalwitch/tags/blocks/heat_conductors.json");
        files.add("data/minecraft/tags/blocks/mineable/axe.json");
        files.add("data/minecraft/tags/blocks/mineable/pickaxe.json");
        files.add("data/farmersdelight/tags/blocks/heat_sources.json");
        return files;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("itemGroup.herbalwitch.apothecary", "The Herbal Witch: Apothecary");
        STATIONS.forEach((directory, name) -> {
            lang.put("block.herbalwitch." + station(directory).replace('/', '.'), name);
            lang.put("showcase.herbalwitch.recipe." + directory, null);
        });
        for (String status : List.of("missing_input", "needs_heat", "working", "output_blocked")) {
            lang.put("status.herbalwitch.station." + status, null);
        }
        for (String type : TYPES) {
            lang.put("category.herbalwitch.processing." + type, null);
            lang.put("emi.category.herbalwitch.processing." + type, null);
        }
        lang.put("category.herbalwitch.processing.time", null);
        lang.put("category.herbalwitch.processing.heated", null);
        return lang;
    }
}
