package io.github.dirtyredz.herbalwitch.data.animals;

import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

/** Independent resource expectations, deliberately not derived from production profiles. */
public final class AnimalManifest {
    public static final Set<String> SPECIES = Set.of("cow", "sheep", "goat", "pig", "rabbit", "chicken", "horse", "donkey",
            "mule", "llama", "cat", "wolf", "fox", "parrot", "axolotl", "bat");
    public static final Set<String> BAITS = Set.of("grain", "root", "seed", "fish", "meat", "berry");
    public static final List<String> PROJECTS = List.of("animals/bait/grain", "animals/bait/root", "animals/bait/seed",
            "animals/bait/fish", "animals/bait/meat", "animals/bait/berry", "animals/care/calming_powder", "animals/capture/charm", "animals/capture/filled_charm");
    private AnimalManifest() {}
    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        for (String tag : Set.of("eligible", "capturable", "excluded", "ai/attractable", "ai/calmable"))
            files.add("data/herbalwitch/tags/entity_types/animals/" + tag + ".json");
        for (String bait : BAITS) {
            files.add("data/herbalwitch/tags/entity_types/animals/bait/" + bait + ".json");
            files.add("data/herbalwitch/tags/items/animals/food/" + bait + ".json");
            files.add("assets/herbalwitch/models/item/animals/bait/" + bait + ".json");
            files.add("data/herbalwitch/recipes/processing/animals/grinding/" + bait + ".json");
        }
        files.add("assets/herbalwitch/models/item/animals/care/calming_powder.json");
        files.add("data/herbalwitch/recipes/processing/animals/grinding/calming_powder.json");
        files.add("assets/herbalwitch/models/item/animals/capture/charm.json");
        files.add("assets/herbalwitch/models/item/animals/capture/filled_charm.json");
        files.add("data/herbalwitch/recipes/processing/animals/charm_work/capture_charm.json");
        return files;
    }
    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (String bait : BAITS) lang.put("item.herbalwitch.animals.bait." + bait, Character.toUpperCase(bait.charAt(0)) + bait.substring(1) + " Herbal Bait");
        lang.put("item.herbalwitch.animals.care.calming_powder", "Calming Powder");
        lang.put("item.herbalwitch.animals.capture.charm", "Animal Capture Charm");
        lang.put("item.herbalwitch.animals.capture.filled_charm", "Filled Animal Capture Charm");
        lang.put("commands.herbalwitch.showcase.samples", null);
        lang.put("commands.herbalwitch.showcase.samples_failed", null);
        lang.put("animal.herbalwitch.showcase.supplies", "Animal care supplies");
        for (String key : List.of("success", "actor", "disabled", "item", "permission", "unsupported", "owner", "trust", "danger", "settle", "persistence", "capacity", "generation", "payload", "busy", "quarantine", "token", "destination", "inventory", "issued"))
            lang.put("animal.herbalwitch.capture." + key, null);
        for (String key : List.of("transport", "recovery", "actions", "enabled", "disabled")) lang.put("animal.herbalwitch.guide." + key, null);
        for (String key : List.of("title", "adopt", "tame", "loading", "trust", "cooldown", "effects", "requirements", "supported", "owner.0", "owner.1", "owner.2", "owner.3", "trust_only", "brain",
                "guide.feeding", "guide.adoption", "guide.damage", "guide.care", "guide.limits", "guide.species", "guide.native", "guide.keeper", "guide.bat"))
            lang.put("animal.herbalwitch." + key, null);
        for (String key : List.of("ready", "disabled", "unsupported", "range", "permission", "foreign_owner", "read_only", "hurt", "sleeping", "busy", "wrong_bait", "cooldown",
                "max_trust", "capacity", "health_full", "already_calm", "danger", "trust", "owned", "no_bait", "canceled")) lang.put("animal.herbalwitch.reason." + key, null);
        return lang;
    }
}
