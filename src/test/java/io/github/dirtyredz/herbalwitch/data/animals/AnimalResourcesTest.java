package io.github.dirtyredz.herbalwitch.data.animals;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.*;
import static org.junit.jupiter.api.Assertions.*;

class AnimalResourcesTest {
    private static Set<String> values(String path) {
        var tag = json(GENERATED.resolve("data/herbalwitch/tags/" + path + ".json"));
        Set<String> out = new TreeSet<>();
        for (var value : tag.getAsJsonArray("values")) out.add(value.getAsString().replace("minecraft:", ""));
        return out;
    }
    @Test void exactEntityMembershipAndSpecializedBrainExclusions() {
        assertEquals(AnimalManifest.SPECIES, values("entity_types/animals/eligible"));
        var capturable = new TreeSet<>(AnimalManifest.SPECIES); capturable.remove("bat");
        assertEquals(capturable, values("entity_types/animals/capturable"));
        var ai = new TreeSet<>(capturable); ai.removeAll(Set.of("axolotl", "goat"));
        assertEquals(ai, values("entity_types/animals/ai/attractable"));
        assertEquals(ai, values("entity_types/animals/ai/calmable"));
        assertTrue(values("entity_types/animals/excluded").isEmpty());
    }
    @Test void allSixBaitSpeciesAndIngredientGroupsMatchTheBrief() {
        Map.of("grain", Set.of("cow", "sheep", "goat", "horse", "donkey", "mule", "llama"),
                "root", Set.of("pig", "rabbit"), "seed", Set.of("chicken", "parrot"), "fish", Set.of("cat", "axolotl"),
                "meat", Set.of("wolf"), "berry", Set.of("fox", "bat"))
                .forEach((bait, types) -> assertEquals(types, values("entity_types/animals/bait/" + bait)));
        Map.of("grain", Set.of("wheat"), "root", Set.of("carrot", "beetroot"),
                "seed", Set.of("wheat_seeds", "melon_seeds", "pumpkin_seeds", "beetroot_seeds"),
                "fish", Set.of("cod", "salmon", "tropical_fish"), "meat", Set.of("beef", "porkchop", "chicken", "mutton", "rabbit"),
                "berry", Set.of("sweet_berries", "glow_berries", "apple"))
                .forEach((bait, foods) -> assertEquals(foods, values("items/animals/food/" + bait)));
    }
}
