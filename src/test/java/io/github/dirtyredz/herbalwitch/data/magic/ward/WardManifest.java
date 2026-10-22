package io.github.dirtyredz.herbalwitch.data.magic.ward;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The ward stone's expected generated files and language keys (Milestone 7), written out
 * independently of the ward catalogue and providers. The shared axe/pickaxe tag is listed by the
 * station manifest, the lang file by the feature manifests.
 */
public final class WardManifest {
    public static final String ID = "magic/ward/stone";

    private WardManifest() {
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

    /** The compiled models under models/, sorted: one inactive base and the lit active form. */
    public static List<String> models() {
        return List.of("block/ward/ward_stone/active.json", "block/ward/ward_stone/base.json");
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("block.herbalwitch.magic.ward.stone", "Ward Stone");
        lang.put("container.herbalwitch.magic.ward.stone", "Ward Stone");
        for (String m : List.of("activated", "already_charged", "chunk_limit", "deactivated", "dimension_limit", "failed", "no_stone",
                "not_charged", "not_enough_energy", "not_owner", "not_witch", "overlap", "owner_limit", "profile", "protected",
                "radius", "radius_limit", "released", "too_far", "unbound", "unsupported", "world_limit")) {
            lang.put("ward.herbalwitch." + m, null);
        }
        for (String u : List.of("activate", "charge", "cost", "deactivate", "owner.0", "owner.1", "owner.2", "profile",
                "profile.arthropods", "profile.both", "profile.undead", "profile_is", "radius", "radius_down", "radius_up",
                "release", "state.0", "state.1", "state.2", "state.3")) {
            lang.put("ward.herbalwitch.ui." + u, null);
        }
        for (String p : List.of("title", "0", "activation", "boundary", "loaded", "never", "owner", "profiles")) {
            lang.put("grimoire.herbalwitch.witchcraft.wards." + p, null);
        }
        lang.put("showcase.herbalwitch.section.ward_stone", null);
        for (String t : List.of("activate", "boundary", "protection")) {
            lang.put("showcase.herbalwitch.label.ward." + t, null);
            lang.put("showcase.herbalwitch.label.ward." + t + ".detail", null);
        }
        return lang;
    }
}
