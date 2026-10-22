package io.github.dirtyredz.herbalwitch.data.animals;

import java.util.Set;
import java.util.TreeSet;

/** Independent resource expectations, deliberately not derived from production profiles. */
public final class AnimalManifest {
    public static final Set<String> SPECIES = Set.of("cow", "sheep", "goat", "pig", "rabbit", "chicken", "horse", "donkey",
            "mule", "llama", "cat", "wolf", "fox", "parrot", "axolotl", "bat");
    public static final Set<String> BAITS = Set.of("grain", "root", "seed", "fish", "meat", "berry");
    private AnimalManifest() {}
    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        for (String tag : Set.of("eligible", "capturable", "excluded", "ai/attractable", "ai/calmable"))
            files.add("data/herbalwitch/tags/entity_types/animals/" + tag + ".json");
        for (String bait : BAITS) {
            files.add("data/herbalwitch/tags/entity_types/animals/bait/" + bait + ".json");
            files.add("data/herbalwitch/tags/items/animals/food/" + bait + ".json");
        }
        return files;
    }
}
