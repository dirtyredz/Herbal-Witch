package io.github.dirtyredz.herbalwitch.data.familiars;

import java.util.*;

/** Independent M9 inventory; never computed from production catalogues. */
public final class FamiliarManifest {
    public static final List<String> SPECIES = List.of("cat", "raven", "owl", "fox", "wolf", "snake", "rabbit", "bat");
    public static final List<String> EQUIPMENT = List.of("cat_bell", "raven_satchel", "owl_lens", "fox_pouch", "wolf_harness", "snake_ring", "rabbit_sachet", "bat_chime");
    private FamiliarManifest() {}
    public static Set<String> files() {
        Set<String> out = new TreeSet<>();
        for (String species : SPECIES) out.add("data/herbalwitch/tags/items/familiars/equipment/" + species + ".json");
        out.add("data/herbalwitch/tags/items/familiars/charms.json");
        for (String key : List.of("bed", "nest", "perch")) {
            String path = "familiars/home/" + key + ".json";
            out.add("assets/herbalwitch/blockstates/" + path); out.add("assets/herbalwitch/models/item/" + path);
            out.add("data/herbalwitch/loot_tables/blocks/" + path); out.add("data/herbalwitch/recipes/" + path);
            out.add("data/herbalwitch/advancements/recipes/decorations/" + path);
        }
        for (String gear : EQUIPMENT) {
            out.add("assets/herbalwitch/models/item/familiars/equipment/" + gear + ".json");
            out.add("data/herbalwitch/recipes/processing/charm_work/familiars/equipment/" + gear + ".json");
        }
        return out;
    }
    public static Map<String, String> lang() {
        Map<String, String> out = new LinkedHashMap<>();
        for (String gear : EQUIPMENT) out.put("item.herbalwitch.familiars.equipment." + gear, null);
        for (String key : List.of("collect_cargo", "equipment_hint", "charm_slot", "gear_slot", "cargo_empty", "inventory_full", "cargo_collected")) out.put("familiar.herbalwitch." + key, null);
        for (String key : List.of("bed", "nest", "perch")) out.put("block.herbalwitch.familiars.home." + key, null);
        out.put("showcase.herbalwitch.section.familiar_homes", null); out.put("showcase.herbalwitch.label.familiar_home", null);
        for (String key : List.of("choose_home", "assign_home", "home_assigned", "home_incompatible", "home_occupied")) out.put("familiar.herbalwitch." + key, null);
        out.put("familiar.herbalwitch.offline_limit", null);
        for (String key : List.of("roster", "detail", "first_page", "next_page", "altar_target", "loading", "health", "loaded", "last_observed", "home", "empty", "select", "stale", "nearby_only")) out.put("familiar.herbalwitch." + key, null);
        for (String key : List.of("previous_command", "next_command", "recall", "command_set", "mobile_cap", "mobile_reduced", "no_home", "home_unloaded", "home_missing", "other_dimension", "unloaded_route", "blocked", "path_pending", "retreating", "owner_offline", "owner_dead", "unavailable", "not_loaded", "controlled", "cooldown", "permission", "busy", "interrupted", "recalled", "threat")) out.put("familiar.herbalwitch." + key, null);
        for (String key : List.of("follow", "stay", "guard_witch", "protect_home", "scout", "passive", "return_home")) out.put("familiar.herbalwitch.command." + key, null);
        for (String key : List.of("binding_prepared", "active", "incapacitation_prepared", "incapacitated", "revival_prepared", "transfer_prepared", "quarantined")) out.put("familiar.herbalwitch.phase." + key, null);
        for (String s : SPECIES) out.put("entity.herbalwitch.familiars.entity." + s, "Familiar " + Character.toUpperCase(s.charAt(0)) + s.substring(1));
        out.put("ritual.herbalwitch.ui.view_contributed", "Familiar rituals"); out.put("ritual.herbalwitch.ui.contributed_terms", null);
        for (String action : List.of("binding", "revival", "heartwood", "spirit_well", "bond_harmony")) {
            out.put("grimoire.herbalwitch.ritual.familiar." + action + ".title", null);
            out.put("grimoire.herbalwitch.ritual.familiar." + action + ".offering", null);
            out.put("category.herbalwitch.ritual.result.familiar." + action, null);
        }
        for (String key : List.of("not_witch", "disabled", "no_target", "indexing", "already_bound", "species", "range", "danger", "owner", "trust", "permission", "care_read_only", "capture_read_only", "capture_lineage", "begun", "bound", "revived", "interrupted")) {
            out.put("ritual.herbalwitch.familiar." + key, null); out.put("ritual.herbalwitch.cancel.familiar_" + key, null);
        }
        return out;
    }
}
