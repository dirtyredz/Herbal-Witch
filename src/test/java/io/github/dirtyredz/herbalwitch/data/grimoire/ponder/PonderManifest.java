package io.github.dirtyredz.herbalwitch.data.grimoire.ponder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The animated lessons' expected generated files and language keys, written out independently of the lesson
 * catalogues and providers.
 */
public final class PonderManifest {
    public static final String FIRST_PREPARATION_STRUCTURE = "assets/herbalwitch/ponder/apothecary/first_preparation.nbt";
    /** The first-preparation captions, by shared-text name. */
    public static final List<String> FIRST_PREPARATION_CAPTIONS = List.of("intro", "harvest", "dry", "dry_wait", "grind",
            "valerian", "heat", "load", "container", "brew", "drink", "replay");

    private PonderManifest() {
    }

    public static Set<String> files() {
        return Set.of(FIRST_PREPARATION_STRUCTURE);
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("herbalwitch.guide.watch", "Watch the animated lesson");
        lang.put("herbalwitch.ponder.first_preparation.header", "First preparation: Restful Infusion");
        FIRST_PREPARATION_CAPTIONS.forEach(name -> lang.put("herbalwitch.ponder.shared.first_preparation." + name, null));
        return lang;
    }
}
