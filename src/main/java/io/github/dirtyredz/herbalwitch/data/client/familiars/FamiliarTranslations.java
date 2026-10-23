package io.github.dirtyredz.herbalwitch.data.client.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import net.minecraftforge.common.data.LanguageProvider;
import java.util.*;

public final class FamiliarTranslations {
    private FamiliarTranslations() {}
    public static void addTo(LanguageProvider out) {
        for (var gear : FamiliarEquipmentCatalog.ALL) out.add("item.herbalwitch." + gear.path().replace('/', '.'), gear.name());
        Map.of("collect_cargo", "Collect cargo", "equipment_hint", "Equip one charm and one species accessory.", "charm_slot", "Charm", "gear_slot", "Gear", "cargo_empty", "No cargo to collect.", "inventory_full", "Make space in your inventory.", "cargo_collected", "Cargo collected.").forEach((key, value) -> out.add("familiar.herbalwitch." + key, value));
        for (var home : FamiliarHomeKind.values()) out.add("block.herbalwitch." + home.path().replace('/', '.'), "Familiar " + title(home.key()));
        out.add("showcase.herbalwitch.section.familiar_homes", "Familiar homes");
        out.add("showcase.herbalwitch.label.familiar_home", "Use with grimoire");
        Map.of("choose_home", "Choose a familiar for this home", "assign_home", "Assign this home", "home_assigned", "Home assigned.",
                "home_incompatible", "This species needs a different home.", "home_occupied", "This home already has a familiar.")
                .forEach((key, value) -> out.add("familiar.herbalwitch." + key, value));
        Map.ofEntries(Map.entry("roster", "Familiars"), Map.entry("detail", "Nearby detail"), Map.entry("first_page", "First page"),
                Map.entry("next_page", "Next page"), Map.entry("altar_target", "Select for altar"), Map.entry("loading", "Reading your familiar bonds…"),
                Map.entry("health", "Bond %s · %s / %s health · %s"), Map.entry("loaded", "Loaded"), Map.entry("last_observed", "Last observed"),
                Map.entry("home", "Home: %s"), Map.entry("empty", "No familiar bonds yet."), Map.entry("select", "Select a familiar. Scroll to read this page."),
                Map.entry("stale", "The roster changed. Review the refreshed page."), Map.entry("nearby_only", "Approach your familiar with a clear line of sight."))
                .forEach((key, value) -> out.add("familiar.herbalwitch." + key, value));
        Map.ofEntries(Map.entry("previous_command", "<"), Map.entry("next_command", ">"), Map.entry("recall", "Recall"),
                Map.entry("command_set", "Command saved."), Map.entry("mobile_cap", "Your mobile familiar slots are occupied."),
                Map.entry("mobile_reduced", "The mobile limit is now %s. Newer assignments have changed to Stay."),
                Map.entry("no_home", "Assign a compatible home first."), Map.entry("home_unloaded", "Home is not loaded; waiting."),
                Map.entry("home_missing", "The assigned home no longer exists."), Map.entry("other_dimension", "Waiting in another dimension."),
                Map.entry("unloaded_route", "The route crosses unloaded terrain."), Map.entry("blocked", "The route or destination is blocked."),
                Map.entry("path_pending", "Looking for a safe route."), Map.entry("retreating", "Retreating to safety."),
                Map.entry("owner_offline", "Owner offline; staying here."), Map.entry("owner_dead", "Owner lost; staying here."), Map.entry("offline_limit", "Resting while other home guards are active."),
                Map.entry("unavailable", "This bond cannot perform that action now."), Map.entry("not_loaded", "Last observed; the familiar is not loaded."),
                Map.entry("controlled", "Wait until your familiar is awake and free to move."), Map.entry("cooldown", "Recall is still recovering."),
                Map.entry("permission", "That action is not permitted here."), Map.entry("busy", "Another familiar transaction is completing."),
                Map.entry("interrupted", "The bond needs operator inspection before another transfer."), Map.entry("recalled", "Your familiar is waiting nearby."),
                Map.entry("threat", "%s spotted %s."))
                .forEach((key, value) -> out.add("familiar.herbalwitch." + key, value));
        Map.of("follow", "Follow", "stay", "Stay", "guard_witch", "Guard Witch", "protect_home", "Protect Home", "scout", "Scout", "passive", "Passive", "return_home", "Return Home")
                .forEach((key, value) -> out.add("familiar.herbalwitch.command." + key, value));
        Map.of("binding_prepared", "Binding pending", "active", "Active", "incapacitation_prepared", "Recovery pending", "incapacitated", "Incapacitated", "revival_prepared", "Revival pending", "transfer_prepared", "Transfer pending", "quarantined", "Needs inspection")
                .forEach((key, value) -> out.add("familiar.herbalwitch.phase." + key, value));
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
                Map.entry("not_witch", "Only a witch can bind a familiar."), Map.entry("disabled", "New bindings for this species are disabled."), Map.entry("no_target", "Choose a nearby trusted animal."),
                Map.entry("indexing", "Familiar records are still being verified."), Map.entry("already_bound", "This creature already has a bond."),
                Map.entry("species", "This creature cannot become a familiar."), Map.entry("range", "Remain within four blocks of the altar with the animal in sight."),
                Map.entry("danger", "The animal must be an adult, safe, awake and free to move."), Map.entry("owner", "You must own this animal."),
                Map.entry("trust", "More successful care is needed before binding."), Map.entry("permission", "You cannot bind this animal here."),
                Map.entry("care_read_only", "This animal's care data needs inspection."), Map.entry("capture_read_only", "Transport records need inspection."),
                Map.entry("capture_lineage", "This animal's transport identity is unresolved."), Map.entry("begun", "The familiar ritual has begun."),
                Map.entry("revived", "Your familiar has revived and is waiting beside the altar."), Map.entry("bound", "Your familiar is bound and waiting here."), Map.entry("interrupted", "Binding was interrupted; the bond is retained for operator inspection."));
        notices.forEach((key, text) -> { out.add("ritual.herbalwitch.familiar." + key, text); out.add("ritual.herbalwitch.cancel.familiar_" + key, text); });
    }
    private static String title(String key) { return Character.toUpperCase(key.charAt(0)) + key.substring(1); }
}
