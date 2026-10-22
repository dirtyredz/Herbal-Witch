package io.github.dirtyredz.herbalwitch.data.client.animals;

import net.minecraftforge.common.data.LanguageProvider;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnimalTranslations {
    private AnimalTranslations() {}
    public static void addTo(LanguageProvider lang) { all().forEach(lang::add); }
    public static Map<String, String> all() {
        Map<String, String> text = new LinkedHashMap<>();
        for (String bait : new String[] {"grain", "root", "seed", "fish", "meat", "berry"})
            text.put("item.herbalwitch.animals.bait." + bait, Character.toUpperCase(bait.charAt(0)) + bait.substring(1) + " Herbal Bait");
        text.put("item.herbalwitch.animals.care.calming_powder", "Calming Powder");
        text.put("item.herbalwitch.animals.capture.charm", "Animal Capture Charm");
        text.put("item.herbalwitch.animals.capture.filled_charm", "Filled Animal Capture Charm");
        text.put("commands.herbalwitch.showcase.samples", "Spawned %s adult animal specimens.");
        text.put("commands.herbalwitch.showcase.samples_failed", "Animal spawning failed. This invocation's samples were removed; %s constructed blocks remain.");
        String prefix = "animal.herbalwitch.";
        text.put(prefix + "showcase.supplies", "Animal care supplies");
        text.put(prefix + "requirements", "Needs %s trust, %s feeds and one extra matching bait.");
        text.put(prefix + "supported", "Calming %s; adoption %s; capture %s.");
        String[] transport = {"success", "Animal transport completed.", "actor", "Transport requires a living real player.",
                "disabled", "New captures are disabled; existing charms may still release.", "item", "Hold one empty animal capture charm.",
                "permission", "You cannot handle this animal here.", "unsupported", "No tested capture adapter supports this animal.",
                "owner", "Only the authorized owner or captor may do this.", "trust", "An unowned animal needs 80 personal trust and four successful feeds.",
                "danger", "Let the animal recover; remove passengers, vehicles and leashes, and move it out of danger.",
                "settle", "Settle this animal with calming powder or the existing sleep vial first.", "persistence", "Capture persistence is uncertain. Transport is locked; an operator must inspect the retained ledger.",
                "capacity", "Capture storage is full. Existing records are retained.", "generation", "The animal's transport generation conflicts with the ledger.",
                "payload", "Saved animal data or its adapter is unsupported. The animal or stored payload was preserved.", "busy", "Wait before another transport transaction.",
                "quarantine", "Transport was interrupted. Its payload is retained for operator recovery.", "token", "This charm has no current stored claim.",
                "destination", "The aimed destination is unsafe, obstructed, unloaded or protected. Axolotls need water.",
                "inventory", "The recipient needs a free inventory slot.", "issued", "A replacement token was durably issued; earlier copies are invalid."};
        for (int i = 0; i < transport.length; i += 2) text.put(prefix + "capture." + transport[i], transport[i + 1]);
        Map.ofEntries(Map.entry("title", "Ordinary Animals"), Map.entry("adopt", "Become keeper"), Map.entry("tame", "Tame"),
                Map.entry("loading", "Reading this animal..."), Map.entry("trust", "Your trust: %s / 100; successful feeds: %s"),
                Map.entry("cooldown", "Next rewarded feed: %s loaded seconds"), Map.entry("effects", "Settled: %s s; asleep: %s s"),
                Map.entry("owner.0", "No exclusive owner"), Map.entry("owner.1", "Your animal"), Map.entry("owner.2", "Another player's animal"),
                Map.entry("owner.3", "Conflicting ownership: care disabled"),
                Map.entry("trust_only", "Bats earn trust for later familiar work: 60 trust and three feeds. No ordinary adoption or transport."),
                Map.entry("brain", "Native Brain behavior is preserved. Use sleep for handling; generic calming and attraction do not apply."))
                .forEach((key, value) -> text.put(prefix + key, value));
        String[] reasons = {
                "ready", "Ready", "disabled", "This action is disabled by the server.", "unsupported", "This animal does not support that action.",
                "range", "Stay within reach and clear sight of the living animal.", "permission", "Entity protection denies this action.",
                "foreign_owner", "Only this animal's owner may care for it.", "read_only", "Unrecognized saved care data: preserved without changes.",
                "hurt", "Give this animal time after being hurt.", "sleeping", "Wait until the animal is awake and free of hard control.",
                "busy", "The animal has an attack target.", "wrong_bait", "Use this species' matching herbal bait.", "cooldown", "Wait for the target's loaded care cooldown.",
                "max_trust", "Your trust is already full.", "capacity", "The animal care capacity is full; nothing was consumed.",
                "health_full", "The animal is already at full health.", "already_calm", "This animal is already settled; calming cannot refresh.",
                "danger", "Let the animal escape danger or stop riding before settling it.", "trust", "Earn the required trust and feeding history first.",
                "owned", "This animal already has an owner or keeper.", "no_bait", "Carry one additional matching bait in your main inventory or offhand.",
                "canceled", "The action was canceled; no preparation was consumed."};
        for (int i = 0; i < reasons.length; i += 2) text.put(prefix + "reason." + reasons[i], reasons[i + 1]);
        text.put(prefix + "guide.feeding", "Use matching herbal bait on an awake animal to earn %s personal trust, at most once per %s loaded seconds. Holding bait attracts but never rewards trust. Vanilla food keeps its normal behavior.");
        text.put(prefix + "guide.adoption", "Open this grimoire on the animal. Keeper species require %s trust and %s successful feeds; native pets require %s trust and %s feeds. Then choose Become keeper or Tame with one additional matching bait. Vanilla taming remains available.");
        text.put(prefix + "guide.damage", "Your successful damage removes %s of your trust and blocks feeding for %s loaded seconds. Trust never decays passively. Healing grants no trust.");
        text.put(prefix + "guide.care", "Use the existing healing salve to restore up to %s health, once per %s loaded seconds, returning its bowl. Calming powder briefly settles supported goal-selector animals for %s loaded seconds. Panic can resume afterward; attacker memory is retained.");
        text.put(prefix + "guide.limits", "Foreign pets are owner-only. Ordinary keepers grant persistence, not follow, attack, sit, home, equipment, resurrection or familiar powers. Animals remain mortal. Goats, axolotls and bats retain native Brain or flight behavior. The existing sleep vial is temporary handling, never permission or earned trust. Familiar brews remain unavailable.");
        text.put(prefix + "guide.species", "%s: %s — %s");
        text.put(prefix + "guide.native", "native taming and ownership");
        text.put(prefix + "guide.keeper", "exclusive keeper, ordinary behavior");
        text.put(prefix + "guide.bat", "trust only, future familiar milestone");
        text.put(prefix + "guide.transport", "Capture supported animals only after earning 80 trust and four feeds, or when you already own them. Settle them with powder or sleep, let damage recover, dismount and unleash. Use an empty charm; release its single stored claim against a safe block face. Axolotls require a water habitat. The animal is paused while stored. Copied charms cannot duplicate it. Making a charm requires the witch workbench; using it costs no energy.");
        text.put(prefix + "guide.recovery", "If transport fails, keep the charm and ask an operator to inspect /herbalwitch capture inspect <animal-uuid>. Reissue rotates a stored token; recover handles retained interrupted payloads for the original captor. Neither command spawns an animal or revives a released animal. Unknown saved data is preserved.");
        text.put(prefix + "guide.actions", "Attraction: %s; calming: %s; ordinary adoption: %s; new capture: %s.");
        text.put(prefix + "guide.enabled", "available"); text.put(prefix + "guide.disabled", "unavailable");
        return text;
    }
}
