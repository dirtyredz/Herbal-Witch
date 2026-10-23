package io.github.dirtyredz.herbalwitch.data.client.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import net.minecraftforge.common.data.LanguageProvider;
import java.util.*;

public final class FamiliarTranslations {
    private FamiliarTranslations() {}
    public static void addTo(LanguageProvider out) {
        Map.ofEntries(Map.entry("roster", "Familiars"), Map.entry("detail", "Nearby detail"), Map.entry("first_page", "First page"),
                Map.entry("next_page", "Next page"), Map.entry("altar_target", "Select for altar"), Map.entry("loading", "Reading your familiar bonds…"),
                Map.entry("health", "Bond %s · %s / %s health · %s"), Map.entry("loaded", "Loaded"), Map.entry("last_observed", "Last observed"),
                Map.entry("home", "Home: %s"), Map.entry("empty", "No familiar bonds yet."), Map.entry("select", "Select a familiar. Scroll to read this page."),
                Map.entry("stale", "The roster changed. Review the refreshed page."), Map.entry("nearby_only", "Approach your familiar with a clear line of sight."))
                .forEach((key, value) -> out.add("familiar.herbalwitch." + key, value));
        for (var species : FamiliarCatalog.ALL) out.add("entity.herbalwitch.familiars.entity." + species.key(), "Familiar " + title(species.key()));
        out.add("ritual.herbalwitch.ui.view_contributed", "Familiar rituals");
        out.add("ritual.herbalwitch.ui.contributed_terms", "%s energy, %s seconds");
        String[] titles = {"Familiar Binding", "Familiar Revival", "Heartwood", "Spirit Well", "Bond Harmony"};
        String[] offerings = {"Matching bait, fresh sage, amethyst shard", "Moon essence, fresh yarrow, amethyst shard", "Fresh yarrow, fresh sage, gold ingot", "Amethyst shard, fresh mugwort, moon essence", "Matching bait, fresh lavender"};
        for (int i = 0; i < FamiliarActionCatalog.ALL.size(); i++) {
            var action = FamiliarActionCatalog.ALL.get(i);
            out.add("grimoire.herbalwitch.ritual." + action.key() + ".title", titles[i]);
            out.add("grimoire.herbalwitch.ritual." + action.key() + ".offering", offerings[i]);
            out.add("category.herbalwitch.ritual.result." + action.key(), titles[i]);
        }
        Map<String, String> notices = Map.ofEntries(
                Map.entry("not_witch", "Only a witch can bind a familiar."), Map.entry("no_target", "Choose a nearby trusted animal."),
                Map.entry("indexing", "Familiar records are still being verified."), Map.entry("already_bound", "This creature already has a bond."),
                Map.entry("species", "This creature cannot become a familiar."), Map.entry("range", "Remain within four blocks of the altar with the animal in sight."),
                Map.entry("danger", "The animal must be an adult, safe, awake and free to move."), Map.entry("owner", "You must own this animal."),
                Map.entry("trust", "More successful care is needed before binding."), Map.entry("permission", "You cannot bind this animal here."),
                Map.entry("care_read_only", "This animal's care data needs inspection."), Map.entry("capture_read_only", "Transport records need inspection."),
                Map.entry("capture_lineage", "This animal's transport identity is unresolved."), Map.entry("begun", "The familiar binding has begun."),
                Map.entry("bound", "Your familiar is bound and waiting here."), Map.entry("interrupted", "Binding was interrupted; the bond is retained for operator inspection."));
        notices.forEach((key, text) -> { out.add("ritual.herbalwitch.familiar." + key, text); out.add("ritual.herbalwitch.cancel.familiar_" + key, text); });
    }
    private static String title(String key) { return Character.toUpperCase(key.charAt(0)) + key.substring(1); }
}
