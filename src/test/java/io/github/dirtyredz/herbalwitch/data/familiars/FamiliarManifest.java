package io.github.dirtyredz.herbalwitch.data.familiars;

import java.util.*;

/** Independent M9 inventory; never computed from production catalogues. */
public final class FamiliarManifest {
    public static final List<String> SPECIES = List.of("cat", "raven", "owl", "fox", "wolf", "snake", "rabbit", "bat");
    private FamiliarManifest() {}
    public static Map<String, String> lang() {
        Map<String, String> out = new LinkedHashMap<>();
        for (String s : SPECIES) out.put("entity.herbalwitch.familiars.entity." + s, "Familiar " + Character.toUpperCase(s.charAt(0)) + s.substring(1));
        out.put("ritual.herbalwitch.ui.view_contributed", "Familiar rituals"); out.put("ritual.herbalwitch.ui.contributed_terms", null);
        for (String action : List.of("binding", "revival", "heartwood", "spirit_well", "bond_harmony")) {
            out.put("grimoire.herbalwitch.ritual.familiar." + action + ".title", null);
            out.put("grimoire.herbalwitch.ritual.familiar." + action + ".offering", null);
            out.put("category.herbalwitch.ritual.result.familiar." + action, null);
        }
        for (String key : List.of("not_witch", "no_target", "indexing", "already_bound", "species", "range", "danger", "owner", "trust", "permission", "care_read_only", "capture_read_only", "capture_lineage", "begun", "bound", "interrupted")) {
            out.put("ritual.herbalwitch.familiar." + key, null); out.put("ritual.herbalwitch.cancel.familiar_" + key, null);
        }
        return out;
    }
}
