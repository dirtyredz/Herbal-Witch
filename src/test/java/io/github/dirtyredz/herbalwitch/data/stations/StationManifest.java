package io.github.dirtyredz.herbalwitch.data.stations;

import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import io.github.dirtyredz.herbalwitch.data.magic.charm.CharmManifest;
import io.github.dirtyredz.herbalwitch.data.preparations.AdvancedManifest;

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
            "kettle", "Tea Kettle", "hearth", "Hearth", "stove", "Witch's Stove", "infusion_pot", "Infusion Pot",
            "cauldron", "Witch's Cauldron", "still", "Copper Still", "bottling_bench", "Bottling Bench",
            "charm_workbench", "Charm Workbench");
    /** Multiblock stations: directory -> number of cells. */
    public static final Map<String, Integer> MULTIBLOCKS = Map.of("drying_rack", 4, "hearth", 4, "stove", 2,
            "still", 2, "bottling_bench", 2, "charm_workbench", 2);
    /** The overlay state a station shows and the overlay model prefix of its cells (fire while lit, bundles while drying, else active). */
    public static final Map<String, String> SHOWN = Map.of("drying_rack", "active", "hearth", "lit", "stove", "lit",
            "still", "active", "bottling_bench", "active", "charm_workbench", "active");
    public static final Map<String, String> OVERLAYS = Map.of("drying_rack", "bundles_", "hearth", "fire_", "stove", "fire_",
            "still", "active_", "bottling_bench", "active_", "charm_workbench", "active_");
    public static final List<String> TYPES = List.of("drying", "grinding", "steeping", "infusion",
            "cauldron", "distillation", "bottling", "charm_work");
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
        AdvancedManifest.processingRecipes().forEach(r -> recipes.add("processing/" + r));
        CharmManifest.processingRecipes().forEach(r -> recipes.add("processing/" + r));
        for (String bait : List.of("grain", "root", "seed", "fish", "meat", "berry", "calming_powder")) recipes.add("processing/animals/grinding/" + bait);
        recipes.add("processing/animals/charm_work/capture_charm");
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
        lang.put("category.herbalwitch.processing.witch", null);
        for (String status : List.of("already", "busy", "cancelled", "needs_energy", "needs_operator", "needs_witch", "not_enough_energy",
                "not_witch", "not_yours", "nothing", "ordinary", "resumed", "started", "too_far")) {
            lang.put("status.herbalwitch.station." + status, null);
        }
        for (String ui : List.of("cancel", "cost", "resume", "start", "theirs", "title", "unstarted", "yours")) {
            lang.put("screen.herbalwitch.station.magic." + ui, null);
        }
        for (String page : List.of("title", "0", "start", "payer", "cost", "bottling")) {
            lang.put("grimoire.herbalwitch.witchcraft.brewing." + page, null);
        }
        return lang;
    }
}
