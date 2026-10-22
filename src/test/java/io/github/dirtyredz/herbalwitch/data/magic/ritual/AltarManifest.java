package io.github.dirtyredz.herbalwitch.data.magic.ritual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The functional altar's expected generated files and language keys, written out independently of
 * the ritual catalogue and providers (Milestone 6). The shared axe tag is listed by the decor
 * manifest; the lang file by the feature manifests.
 */
public final class AltarManifest {
    public static final String ID = "magic/ritual/altar";
    /** Cells with an active overlay (the glowing runes, flames and moon reach only the upper row). */
    public static final List<Integer> ACTIVE_CELLS = List.of(2, 3);
    public static final List<String> ACTIONS = List.of("founding", "initiation", "meditation", "attunement");
    public static final List<String> TOPICS = List.of("founding", "initiation", "recovery", "practice");

    private AltarManifest() {
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

    /** The compiled models under models/, sorted. */
    public static List<String> models() {
        List<String> out = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            out.add("block/ritual/altar/part_" + i + ".json");
        }
        ACTIVE_CELLS.forEach(i -> out.add("block/ritual/altar/active_" + i + ".json"));
        out.add("block/ritual/altar/assembly.json");
        out.sort(null);
        return out;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("block.herbalwitch.magic.ritual.altar", "Witch's Altar");
        lang.put("container.herbalwitch.magic.ritual.altar", "Witch's Altar");
        for (String n : List.of("busy", "idle", "missing_offering", "not_enough_energy", "rules_changed",
                "founding.disabled", "founding.already_witch", "founding.closed", "founding.begun", "founding.done",
                "initiation.not_near", "initiation.not_witch", "initiation.other_altar", "initiation.initiator_away", "initiation.begun",
                "initiation.done", "meditation.not_witch", "meditation.full", "meditation.begun",
                "attunement.not_witch", "attunement.begun", "attunement.done", "attunement.done_max")) {
            lang.put("ritual.herbalwitch." + n, null);
        }
        for (String c : List.of("sent", "designated", "declined", "withdrawn", "self", "not_witch", "already_witch", "disabled",
                "initiator_busy", "recipient_busy", "cooldown", "no_invitation", "expired", "ready")) {
            lang.put("ritual.herbalwitch.consent." + c, null);
        }
        for (String c : List.of("altar_gone", "left", "closed", "access", "hurt", "reloaded", "rules_changed", "stopped",
                "inputs_changed", "claim_lost", "not_enough_energy", "moved", "already_witch", "no_room")) {
            lang.put("ritual.herbalwitch.cancel." + c, null);
        }
        for (String r : List.of("converted", "already_witch", "inputs_changed", "not_enough_energy", "no_room", "claim_lost",
                "not_persisted", "duplicate", "failed")) {
            lang.put("ritual.herbalwitch.result." + r, null);
        }
        for (String u : List.of("status.witch", "status.not_witch", "reading", "running", "running_with", "stop", "no_invitation",
                "founding_closed", "founding_intro", "found", "founding_review", "permanent", "confirm", "cancel", "meditation_full",
                "meditation_intro", "meditate", "attunement_intro", "attunement_max", "attune", "offering_line", "scope", "designated",
                "invited_by", "expires", "other_altar", "accept", "decline", "inviting", "withdraw", "initiation_disabled",
                "initiation_intro", "nobody", "invite")) {
            lang.put("ritual.herbalwitch.ui." + u, null);
        }
        for (String p : List.of("altar.0", "altar.size", "altar.scope", "altar.crafted", "offering", "founding.terms", "founding.once",
                "founding.disabled", "initiation.terms", "initiation.consent", "initiation.designation", "meditation.terms",
                "attunement.terms", "attunement.capacity")) {
            lang.put("grimoire.herbalwitch.ritual." + p, null);
        }
        ACTIONS.forEach(a -> {
            lang.put("grimoire.herbalwitch.ritual." + a + ".title", null);
            lang.put("grimoire.herbalwitch.ritual." + a + ".0", null);
        });
        lang.put("showcase.herbalwitch.section.altar", "Witch's Altar");
        for (String l : List.of("use", "use.detail", "supplies")) {
            lang.put("showcase.herbalwitch.label.altar." + l, null);
        }
        TOPICS.forEach(t -> {
            lang.put("showcase.herbalwitch.label.altar." + t, null);
            lang.put("showcase.herbalwitch.label.altar." + t + ".detail", null);
        });
        return lang;
    }
}
