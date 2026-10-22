package io.github.dirtyredz.herbalwitch.data.client.magic.witch;

import io.github.dirtyredz.herbalwitch.grimoire.content.reference.WitchEntries;
import io.github.dirtyredz.herbalwitch.magic.witch.command.WitchCommand;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationConsent;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Witch status and energy in English: consent messages, the operator commands, the HUD number and
 * the grimoire's witchcraft pages (their numbers are arguments filled from the active rules).
 */
public final class WitchTranslations {
    private WitchTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        consent().forEach((result, text) -> lang.add(result.messageKey(), text));
        lang.add("witch.herbalwitch.initiation.invited", "%s invites you to become a witch. Open the altar there to accept or decline.");
        lang.add("witch.herbalwitch.initiation.designated_notice",
                "An operator offers to make you a witch. Open any Witch's Altar to accept or decline.");
        commands(lang);
        lang.add("hud.herbalwitch.energy", "%s / %s");
        pages().forEach((suffix, text) -> lang.add(WitchEntries.PREFIX + suffix, text));
    }

    static Map<InitiationConsent.Result, String> consent() {
        Map<InitiationConsent.Result, String> m = new LinkedHashMap<>();
        m.put(InitiationConsent.Result.SENT, "Invitation sent.");
        m.put(InitiationConsent.Result.DESIGNATED, "Designation sent; they must accept at an altar.");
        m.put(InitiationConsent.Result.DECLINED, "The invitation was declined.");
        m.put(InitiationConsent.Result.WITHDRAWN, "The invitation was withdrawn.");
        m.put(InitiationConsent.Result.SELF, "You cannot invite yourself.");
        m.put(InitiationConsent.Result.NOT_WITCH, "Only a witch can invite.");
        m.put(InitiationConsent.Result.ALREADY_WITCH, "They are already a witch.");
        m.put(InitiationConsent.Result.DISABLED, "Initiation is disabled on this server.");
        m.put(InitiationConsent.Result.INITIATOR_BUSY, "You already have an invitation open.");
        m.put(InitiationConsent.Result.RECIPIENT_BUSY, "They already hold an invitation.");
        m.put(InitiationConsent.Result.COOLDOWN, "Wait a moment before inviting again.");
        m.put(InitiationConsent.Result.NO_INVITATION, "There is no such invitation.");
        m.put(InitiationConsent.Result.EXPIRED, "The invitation expired.");
        m.put(InitiationConsent.Result.READY, "The invitation is ready to accept.");
        return m;
    }

    private static void commands(LanguageProvider lang) {
        String k = WitchCommand.KEY;
        lang.add(k + "no_status", "%s has no witch status record.");
        lang.add(k + "status.witch", "a witch");
        lang.add(k + "status.not_witch", "not a witch");
        lang.add(k + "inspect", "%s is %s: energy %s/%s, practice %s (next threshold %s)");
        lang.add(k + "inspect.gates", "Tea cooldown %s ticks, restful preparation %s ticks, last rewarded sleep day %s");
        lang.add(k + "inspect.unreadable", "Saved data could not be read and is kept unchanged: %s");
        lang.add(k + "designated", "%s may now accept witch status at an altar.");
        lang.add(k + "founding.inspect", "Founding: %s; claim %s; candidate %s; altar %s");
        lang.add(k + "founding.closed", "Founding claim %s closed; nothing was replayed.");
        lang.add(k + "founding.not_review", "Claim %s is not an interrupted claim awaiting review.");
    }

    static Map<String, String> pages() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("status.title", "Witch Status");
        m.put("status.0", "A witch is made, never found in a book: carrying a grimoire teaches you herbs, not witchcraft.");
        m.put("status.routes", "The first witch of a world founds a lineage alone at a Witch's Altar. Everyone after is initiated by a witch "
                + "and must consent; an operator may also designate someone, who still has to accept.");
        m.put("status.permanent", "Status is permanent: death does not remove it or refill energy.");
        m.put("energy.title", "Energy and Practice");
        m.put("energy.0", "Witches carry energy, spent by rituals and later magic and restored by rest.");
        m.put("energy.capacity", "A new witch holds %s energy. Each practice threshold adds %s, up to %s.");
        m.put("energy.thresholds", "Practice thresholds (energy successfully spent): %s.");
        m.put("energy.practice", "Only completed paid work counts: recovery, refunds, cancelled work and founding do not.");
        m.put("energy.exhaustion", "At zero energy nothing hurts you; paid work simply waits until you recover.");
        m.put("recovery.title", "Recovering Energy");
        m.put("recovery.0", "Energy returns through stillness, meditation, sleep and two kettle drinks.");
        m.put("recovery.rest", "Standing still restores %s energy every %s seconds.");
        m.put("recovery.meditation", "Meditating at an altar settles for %s seconds, then restores %s energy every %s seconds.");
        m.put("recovery.sleep", "A full night's sleep restores %s energy, once a day.");
        m.put("recovery.tea", "Energy tea restores %s energy; one cup every %s seconds.");
        m.put("recovery.restful", "A restful infusion adds %s to your next rewarded sleep, if taken within %s minutes.");
        m.put("recovery.bonus", "Nature biomes add %s%% and a visible full moon %s%% to rest and meditation (at most %s%%).");
        return m;
    }
}
