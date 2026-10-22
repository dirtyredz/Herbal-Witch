package io.github.dirtyredz.herbalwitch.data.magic.witch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Witch status and energy's expected generated files and language keys (Milestone 6), independent
 * of the providers: the nature biome tag, consent messages, operator commands, the HUD and the
 * grimoire's witchcraft pages.
 */
public final class WitchManifest {
    public static final String NATURE_TAG = "data/herbalwitch/tags/worldgen/biome/witch/energy/nature.json";
    /** The habitats whose biomes count as nature, independently of the provider. */
    public static final List<String> NATURE_HABITATS = List.of("temperate", "wet", "shade", "woodland");

    private WitchManifest() {
    }

    public static Set<String> files() {
        return Set.of(NATURE_TAG);
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (String r : List.of("sent", "designated", "declined", "withdrawn", "self", "not_witch", "already_witch", "disabled",
                "initiator_busy", "recipient_busy", "cooldown", "no_invitation", "expired", "ready", "invited", "designated_notice")) {
            lang.put("witch.herbalwitch.initiation." + r, null);
        }
        for (String c : List.of("no_status", "status.witch", "status.not_witch", "inspect", "inspect.gates", "inspect.unreadable",
                "designated", "founding.inspect", "founding.closed", "founding.not_review")) {
            lang.put("commands.herbalwitch.witch." + c, null);
        }
        lang.put("hud.herbalwitch.energy", "%s / %s");
        for (String p : List.of("status.title", "status.0", "status.routes", "status.permanent", "energy.title", "energy.0",
                "energy.capacity", "energy.thresholds", "energy.practice", "energy.exhaustion", "recovery.title", "recovery.0",
                "recovery.rest", "recovery.meditation", "recovery.sleep", "recovery.tea", "recovery.restful", "recovery.bonus")) {
            lang.put("grimoire.herbalwitch.witchcraft." + p, null);
        }
        return lang;
    }
}
