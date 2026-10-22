package io.github.dirtyredz.herbalwitch.data.magic.ritual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The ritual circle's expected generated files, models and language keys, and the rituals it opens
 * (garden blessing, home protection), written out independently of the ritual catalogue and
 * providers (Milestone 7). The shared pickaxe tag is listed by the station manifest; the lang file
 * by the feature manifests.
 */
public final class CircleManifest {
    public static final String ID = "magic/ritual/circle";
    /** A five by five ring, hollow in the middle: sixteen cells, each with an active overlay. */
    public static final int CELLS = 16;
    public static final List<String> RITUALS = List.of("garden_blessing", "home_protection");

    private CircleManifest() {
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        files.add("assets/herbalwitch/blockstates/" + ID + ".json");
        files.add("assets/herbalwitch/models/item/" + ID + ".json");
        files.add("data/herbalwitch/recipes/" + ID + ".json");
        files.add("data/herbalwitch/advancements/recipes/decorations/" + ID + ".json");
        files.add("data/herbalwitch/loot_tables/blocks/" + ID + ".json");
        return files;
    }

    /** The compiled models under models/, sorted: a part and an active overlay per cell, and no assembly. */
    public static List<String> models() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < CELLS; i++) {
            out.add("block/ritual/circle/part_" + i + ".json");
            out.add("block/ritual/circle/active_" + i + ".json");
        }
        out.sort(null);
        return out;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("block.herbalwitch.magic.ritual.circle", "Ritual Circle");
        for (String p : List.of("0", "altar", "crafted", "needed", "size")) {
            lang.put("grimoire.herbalwitch.ritual.circle." + p, null);
        }
        for (String p : List.of("0", "area", "terms", "title")) {
            lang.put("grimoire.herbalwitch.ritual.garden_blessing." + p, null);
        }
        for (String p : List.of("0", "terms", "title", "ward")) {
            lang.put("grimoire.herbalwitch.ritual.home_protection." + p, null);
        }
        for (String c : List.of("circle_broken", "garden_changed", "ward_changed")) {
            lang.put("ritual.herbalwitch.cancel." + c, null);
        }
        for (String c : List.of("incomplete", "no_altar", "unloaded")) {
            lang.put("ritual.herbalwitch.circle." + c, null);
        }
        for (String g : List.of("begun", "done", "no_bed", "not_allowed", "not_witch", "nothing", "unloaded")) {
            lang.put("ritual.herbalwitch.garden_blessing." + g, null);
        }
        for (String h : List.of("already_charged", "begun", "chunk_limit", "dimension_limit", "done", "no_stone", "no_ward",
                "not_owner", "not_witch", "overlap", "owner_limit", "protected", "unsupported", "world_limit")) {
            lang.put("ritual.herbalwitch.home_protection." + h, null);
        }
        for (String u : List.of("bless", "bless_intro", "bless_no_target", "bless_target", "circle_complete", "circle_state",
                "protect", "protect_intro", "protect_no_target", "protect_target", "view_circle", "view_initiation")) {
            lang.put("ritual.herbalwitch.ui." + u, null);
        }
        for (String c : List.of("", ".needs_altar", ".needs_circle", ".open", ".terms", ".time", ".result.attunement", ".result.founding",
                ".result.garden_blessing", ".result.home_protection", ".result.initiation", ".result.meditation")) {
            lang.put("category.herbalwitch.ritual" + c, null);
        }
        lang.put("emi.category.herbalwitch.ritual", null);
        lang.put("showcase.herbalwitch.section.ritual_circle", null);
        lang.put("showcase.herbalwitch.label.circle.supplies", null);
        for (String t : List.of("arrangement", "garden_blessing", "home_protection")) {
            lang.put("showcase.herbalwitch.label.circle." + t, null);
            lang.put("showcase.herbalwitch.label.circle." + t + ".detail", null);
        }
        return lang;
    }
}
