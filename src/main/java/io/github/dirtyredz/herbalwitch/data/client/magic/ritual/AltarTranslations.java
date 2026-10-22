package io.github.dirtyredz.herbalwitch.data.client.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The altar's English: its name and screen, every result and interruption notice, the grimoire's
 * altar and action pages (numbers are arguments from the active rules) and the showcase signs.
 */
public final class AltarTranslations {
    private AltarTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(RitualBlocks.ALTAR.get(), AltarCatalog.NAME);
        lang.add(AltarMenu.TITLE, AltarCatalog.NAME);
        notices().forEach((suffix, text) -> lang.add(AltarSessionService.KEY + suffix, text));
        ui().forEach((suffix, text) -> lang.add(AltarSessionService.KEY + "ui." + suffix, text));
        pages().forEach((suffix, text) -> lang.add(AltarEntries.PREFIX + suffix, text));
        lang.add(AltarShowcaseSection.TITLE, AltarCatalog.NAME);
        lang.add(AltarShowcaseSection.USE, "Use the altar");
        lang.add(AltarShowcaseSection.USE + ".detail", "Any part opens it");
        lang.add(AltarShowcaseSection.SUPPLIES, "Supplies");
        Map.of("founding", new String[] {"Founding", "The first witch"}, "initiation", new String[] {"Initiation", "With consent"},
                "recovery", new String[] {"Recovery", "Rest, sleep, tea"}, "practice", new String[] {"Practice", "Attune with sage"})
                .forEach((topic, text) -> {
                    lang.add(AltarShowcaseSection.label(topic), text[0]);
                    lang.add(AltarShowcaseSection.label(topic) + ".detail", text[1]);
                });
    }

    /** Results and interruptions ({@code ritual.herbalwitch.<suffix>}). */
    public static Map<String, String> notices() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("busy", "The altar or you are already busy with a ritual.");
        m.put("idle", "Nothing to stop.");
        m.put("missing_offering", "The offering is not in your inventory.");
        m.put("not_enough_energy", "Not enough energy.");
        m.put("rules_changed", "The server's rules changed; look again.");
        m.put("founding.disabled", "Founding is disabled on this server.");
        m.put("founding.already_witch", "You are already a witch.");
        m.put("founding.closed", "The first lineage is already founded.");
        m.put("founding.begun", "The founding ritual begins. Stay at the altar.");
        m.put("founding.done", "You founded the first lineage: you are a witch.");
        m.put("initiation.not_near", "They must be able to use this altar too.");
        m.put("initiation.not_witch", "Only a witch can initiate.");
        m.put("initiation.other_altar", "Accept at the altar where you were invited.");
        m.put("initiation.initiator_away", "Your initiator must be at this altar with it open.");
        m.put("initiation.begun", "The initiation begins. Both of you stay at the altar.");
        m.put("initiation.done", "The initiation is complete: a new witch.");
        for (String result : new String[] {"sent", "designated", "declined", "withdrawn", "self", "not_witch", "already_witch", "disabled",
                "initiator_busy", "recipient_busy", "cooldown", "no_invitation", "expired", "ready"}) {
            m.put("consent." + result, CONSENT.getOrDefault(result, "Invitation: " + result.replace('_', ' ') + "."));
        }
        m.put("meditation.not_witch", "Only a witch can meditate here.");
        m.put("meditation.full", "Your energy is full.");
        m.put("meditation.begun", "You settle into meditation.");
        m.put("attunement.not_witch", "Only a witch can attune.");
        m.put("attunement.begun", "You begin to attune.");
        m.put("attunement.done", "Attuned: your practice grows.");
        m.put("attunement.done_max", "Attuned. Your capacity is already at its fullest.");
        Map<String, String> cancel = new LinkedHashMap<>();
        cancel.put("altar_gone", "the altar was broken or unloaded");
        cancel.put("left", "a participant left");
        cancel.put("closed", "the altar was closed");
        cancel.put("access", "a participant moved out of reach");
        cancel.put("hurt", "someone was hurt");
        cancel.put("reloaded", "data was reloaded");
        cancel.put("rules_changed", "the server's rules changed");
        cancel.put("stopped", "stopped");
        cancel.put("inputs_changed", "the offering was moved");
        cancel.put("claim_lost", "founding closed meanwhile");
        cancel.put("not_enough_energy", "not enough energy");
        cancel.put("moved", "you moved");
        cancel.put("already_witch", "they are already a witch");
        cancel.put("no_room", "no room for the grimoire");
        cancel.forEach((k, v) -> m.put("cancel." + k, "Ritual interrupted: " + v + ". Nothing was used."));
        m.put("result.converted", "A new witch.");
        m.put("result.already_witch", "They are already a witch.");
        m.put("result.inputs_changed", "The offering was moved; nothing was used.");
        m.put("result.not_enough_energy", "Not enough energy; nothing was used.");
        m.put("result.no_room", "Make room for the grimoire; nothing was used.");
        m.put("result.claim_lost", "Founding closed meanwhile; nothing was used.");
        m.put("result.not_persisted", "The founding could not be recorded; nothing was used.");
        m.put("result.duplicate", "That ritual already ended.");
        m.put("result.failed", "The ritual failed and was undone; nothing was used.");
        return m;
    }

    private static final Map<String, String> CONSENT = Map.of("sent", "Invitation sent.", "declined", "The invitation was declined.",
            "withdrawn", "The invitation was withdrawn.", "expired", "The invitation expired.", "no_invitation", "There is no such invitation.",
            "cooldown", "Wait a moment before inviting again.", "recipient_busy", "They already hold an invitation.",
            "initiator_busy", "You already have an invitation open.", "already_witch", "They are already a witch.",
            "disabled", "Initiation is disabled on this server.");

    /** The screen ({@code ritual.herbalwitch.ui.<suffix>}). */
    public static Map<String, String> ui() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("status.witch", "Witch: %s / %s energy");
        m.put("status.not_witch", "You are not a witch.");
        m.put("reading", "Reading the altar...");
        m.put("running", "%s in progress.");
        m.put("running_with", "%s in progress with %s.");
        m.put("stop", "Stop");
        m.put("no_invitation", "No invitation. A witch can invite you here.");
        m.put("founding_closed", "This world's first lineage is founded. Ask a witch to initiate you.");
        m.put("founding_intro", "No witch exists yet. You may found the first lineage here, alone.");
        m.put("found", "Found a lineage");
        m.put("founding_review", "The founding takes %s seconds at this altar and consumes:");
        m.put("permanent", "This permanently makes you a witch.");
        m.put("confirm", "Confirm");
        m.put("cancel", "Cancel");
        m.put("meditation_full", "Your energy is full.");
        m.put("meditation_intro", "Meditate here, still, to restore energy.");
        m.put("meditate", "Meditate");
        m.put("attunement_intro", "Attune for practice: %s energy and:");
        m.put("attunement_max", "No further capacity to gain.");
        m.put("attune", "Attune");
        m.put("offering_line", "%s: %s/%s");
        m.put("scope", "Taken from your main inventory and offhand.");
        m.put("designated", "An operator offers to make you a witch, at no cost to you.");
        m.put("invited_by", "%s invites you to become a witch. They supply the offering and %s energy.");
        m.put("expires", "Expires in %s s.");
        m.put("other_altar", "Accept at the altar you were invited at.");
        m.put("accept", "Accept and begin");
        m.put("decline", "Decline");
        m.put("inviting", "Invited %s (%s s left).");
        m.put("withdraw", "Withdraw");
        m.put("initiation_disabled", "Initiation is disabled on this server.");
        m.put("initiation_intro", "Initiate someone nearby: you supply the offering and %s energy.");
        m.put("nobody", "Nobody nearby can be invited.");
        m.put("invite", "Invite");
        return m;
    }

    /** The grimoire's altar pages ({@code grimoire.herbalwitch.ritual.<suffix>}). */
    public static Map<String, String> pages() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("altar.0", "A dark-wood shrine with a working table, where witches are made and energy is worked.");
        m.put("altar.size", "Two blocks wide, one deep and two high, on a sturdy floor.");
        m.put("altar.scope", "It holds nothing: offerings come from your main inventory and offhand when a ritual completes; "
                + "an interrupted ritual uses nothing.");
        m.put("altar.crafted", "Crafted from two smooth stone slabs, an amethyst shard, four dark oak logs and a gold ingot.");
        m.put("offering", "Offering: %s.");
        m.put("founding.title", "Founding a Lineage");
        m.put("founding.0", "When no witch exists, anyone may become the first one, alone, at any altar and any hour.");
        m.put("founding.terms", "Confirm at the altar and stay %s seconds. You receive a grimoire (keep a slot free).");
        m.put("founding.once", "It happens once per world.");
        m.put("founding.disabled", "This server has disabled founding.");
        m.put("initiation.title", "Initiation");
        m.put("initiation.0", "A witch may make another witch, but only with consent.");
        m.put("initiation.terms", "The witch pays %s energy and the offering; both stay %s seconds. An invitation lasts %s seconds.");
        m.put("initiation.consent", "The one invited opens the same altar and accepts or declines; they pay nothing.");
        m.put("initiation.designation", "An operator may designate someone; they still accept at an altar.");
        m.put("meditation.title", "Meditation");
        m.put("meditation.0", "A witch may sit with an idle altar to recover energy.");
        m.put("meditation.terms", "After %s seconds of settling it restores %s energy every %s seconds; it ends when you are full.");
        m.put("attunement.title", "Attunement");
        m.put("attunement.0", "Practice alone: a small spend that strengthens a witch.");
        m.put("attunement.terms", "It costs %s energy and takes %s seconds.");
        m.put("attunement.capacity", "It counts towards capacity until every threshold is reached.");
        return m;
    }
}
