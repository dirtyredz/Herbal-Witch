package io.github.dirtyredz.herbalwitch.data.grimoire;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The grimoire's expected generated files and language keys, written out independently of the
 * grimoire catalogues and providers. The shared lang file and axe tag are listed by the other
 * manifests, so only the grimoire's own files appear here.
 */
public final class GrimoireManifest {
    /** The two placed blocks, in catalogue order, with their English names. */
    public static final Map<String, String> BLOCKS = linked("stand", "Grimoire Stand", "bookshelf", "Witch's Bookshelf");
    /** Guide entry -> number of pages. */
    public static final Map<String, Integer> GUIDES = guides();
    /** Station entry directories (one page each). */
    public static final List<String> STATION_ENTRIES = List.of("drying_rack", "hearth", "infusion_pot", "kettle", "mortar", "stove",
            "cauldron", "still", "bottling_bench", "charm_workbench", "tools/broom_stand", "tools/wand_bench");
    /** The herbs the reader names. */
    public static final List<String> HERBS = List.of("belladonna", "chamomile", "lavender", "mandrake", "mint", "mugwort",
            "nettle", "nightshade", "rosemary", "sage", "thyme", "valerian", "wolfsbane", "yarrow");

    private GrimoireManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private static Map<String, Integer> guides() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("acquisition", 3);
        map.put("getting_started", 3);
        map.put("heat", 2);
        map.put("missing_ingredients", 4);
        map.put("preparation", 3);
        map.put("products", 2);
        map.put("research", 3);
        map.put("stations", 2);
        map.put("teaching", 3);
        return map;
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        files.add("assets/herbalwitch/models/item/grimoire/book.json");
        files.add("data/herbalwitch/recipes/grimoire/book.json");
        files.add("data/herbalwitch/advancements/recipes/misc/grimoire/book.json");
        for (String block : BLOCKS.keySet()) {
            files.add("assets/herbalwitch/blockstates/grimoire/" + block + ".json");
            files.add("assets/herbalwitch/models/item/grimoire/" + block + ".json");
            files.add("data/herbalwitch/recipes/grimoire/" + block + ".json");
            files.add("data/herbalwitch/advancements/recipes/decorations/grimoire/" + block + ".json");
            files.add("data/herbalwitch/loot_tables/blocks/grimoire/" + block + ".json");
        }
        GUIDES.keySet().forEach(guide -> files.add("data/herbalwitch/grimoire_entries/guide/" + guide + ".json"));
        STATION_ENTRIES.forEach(station -> files.add("data/herbalwitch/grimoire_entries/stations/" + station + ".json"));
        return files;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        BLOCKS.forEach((block, name) -> lang.put("block.herbalwitch.grimoire." + block, name));
        lang.put("container.herbalwitch.grimoire.reader", "Grimoire");
        lang.put("item.herbalwitch.grimoire.book", "Grimoire");
        lang.put("item.herbalwitch.grimoire.book.tooltip", null);
        GUIDES.forEach((guide, pages) -> {
            lang.put("grimoire.herbalwitch.guide." + guide + ".title", null);
            for (int i = 0; i < pages; i++) {
                lang.put("grimoire.herbalwitch.guide." + guide + "." + i, null);
            }
        });
        STATION_ENTRIES.forEach(station -> lang.put("grimoire.herbalwitch.stations." + station.replace('/','.') + ".0", null));
        HERBS.forEach(herb -> lang.put("grimoire.herbalwitch.herb." + herb, null));
        for (String note : List.of("bonus", "discovery", "discovery.none", "form.bush", "form.crop", "likes", "no_conditions")) {
            lang.put("grimoire.herbalwitch.herb." + note, null);
        }
        lang.put("grimoire.herbalwitch.hint.recipe", null);
        for (String preference : List.of("full_moon", "low_sky", "night", "rain")) {
            lang.put("grimoire.herbalwitch.preference." + preference, null);
        }
        for (String result : List.of("already_known", "needs_research_block", "no_familiar_ingredient", "not_a_recipe", "researched")) {
            lang.put("grimoire.herbalwitch.research." + result, null);
        }
        for (String note : List.of("heat_source", "multiblock", "needs_heat", "no_heat", "runs")) {
            lang.put("grimoire.herbalwitch.station." + note, null);
        }
        for (String message : List.of("accepted", "already_known", "cancelled", "cooldown", "declined", "expired", "invited",
                "no_invitation", "no_reader", "not_known", "not_live_recipe", "not_recorded", "out_of_range", "recipe_changed",
                "recipient_busy", "self", "sent", "teacher_busy", "teacher_left")) {
            lang.put("grimoire.herbalwitch.teaching." + message, null);
        }
        for (String ui : List.of("accept", "alternatives", "back", "category.all", "category.furniture", "category.guide", "category.herbs",
                "category.stations", "category.witchcraft", "choose", "counting", "decline", "empty", "filter.all", "filter.known", "filter.research",
                "have", "heat", "invite", "invited", "missing", "nobody", "offered", "open", "open.tooltip", "open_for_counts",
                "ready", "research", "returns", "role.base", "role.ingredient", "role.packaging", "scope", "search", "see_also",
                "station", "teach", "unknown", "withdraw")) {
            lang.put("grimoire.herbalwitch.ui." + ui, null);
        }
        lang.put("showcase.herbalwitch.section.grimoire", null);
        for (String label : List.of("discovery", "discovery.detail", "missing", "missing.detail", "research", "research.detail",
                "supplies", "teaching", "teaching.detail")) {
            lang.put("showcase.herbalwitch.label.grimoire." + label, null);
        }
        return lang;
    }
}
