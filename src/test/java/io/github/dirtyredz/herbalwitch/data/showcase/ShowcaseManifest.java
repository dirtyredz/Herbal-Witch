package io.github.dirtyredz.herbalwitch.data.showcase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The showcase command's language keys (any non-empty text). It generates no other files. */
public final class ShowcaseManifest {
    public static final List<String> SECTIONS = List.of("decor", "herbs", "garden");

    private ShowcaseManifest() {
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (String reason : List.of("success", "unknown_section", "too_large", "outside_world", "unloaded", "denied",
                "block_entity", "unbreakable", "entity")) {
            lang.put("commands.herbalwitch.showcase." + reason, null);
        }
        SECTIONS.forEach(section -> lang.put("showcase.herbalwitch.section." + section, null));
        for (String label : List.of("ages", "samples", "bed_dry", "bed_moist", "farmland")) {
            lang.put("showcase.herbalwitch.label." + label, null);
        }
        return lang;
    }
}
