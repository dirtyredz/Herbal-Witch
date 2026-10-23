package io.github.dirtyredz.herbalwitch.data.animals;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.*;
import static org.junit.jupiter.api.Assertions.*;

class AnimalResourcesTest {
    @Test void sixOrdinaryBaitsHaveExactDurationIngredientsAndYield() {
        for (String bait : AnimalManifest.BAITS) {
            var recipe = json(GENERATED.resolve("data/herbalwitch/recipes/processing/animals/grinding/" + bait + ".json"));
            assertEquals(100, recipe.get("duration").getAsInt()); assertFalse(recipe.has("requirements"));
            assertEquals(4, recipe.getAsJsonObject("result").get("count").getAsInt());
            assertEquals("herbalwitch:animals/bait/" + bait, recipe.getAsJsonObject("result").get("item").getAsString());
            var inputs = recipe.getAsJsonArray("ingredients"); assertEquals(2, inputs.size());
            assertEquals(2, inputs.get(0).getAsJsonObject().get("count").getAsInt());
            assertEquals("herbalwitch:animals/food/" + bait, inputs.get(0).getAsJsonObject().getAsJsonObject("ingredient").get("tag").getAsString());
            assertEquals("herbalwitch:preparations/herbs/shade/valerian/powder", inputs.get(1).getAsJsonObject().getAsJsonObject("ingredient").get("item").getAsString());
        }
    }
    @Test void captureCharmUsesExistingPaidWitchProcessingAndNeverCraftsFilledTokens() {
        var recipe = json(GENERATED.resolve("data/herbalwitch/recipes/processing/animals/charm_work/capture_charm.json"));
        assertEquals("herbalwitch:processing/charm_work", recipe.get("type").getAsString()); assertEquals(200, recipe.get("duration").getAsInt());
        assertTrue(recipe.getAsJsonObject("requirements").get("witch").getAsBoolean()); assertEquals(10, recipe.getAsJsonObject("requirements").get("energy").getAsInt());
        assertEquals("herbalwitch:animals/capture/charm", recipe.getAsJsonObject("result").get("item").getAsString());
        assertFalse(AnimalManifest.files().stream().anyMatch(s -> s.contains("recipes") && s.contains("filled")));
    }
    @Test void allNineItemModelsOnlyReferenceTheirBlockbenchParent() {
        for (String item : AnimalManifest.PROJECTS) {
            var model = json(GENERATED.resolve("assets/herbalwitch/models/item/" + item + ".json"));
            assertEquals(Set.of("parent"), model.keySet()); assertEquals("herbalwitch:block/" + item + "/base", model.get("parent").getAsString());
        }
    }
    private static Set<String> values(String path) {
        var tag = json(GENERATED.resolve("data/herbalwitch/tags/" + path + ".json"));
        Set<String> out = new TreeSet<>();
        for (var value : tag.getAsJsonArray("values")) out.add(value.getAsString().replace("minecraft:", ""));
        return out;
    }
    @Test void exactEntityMembershipAndSpecializedBrainExclusions() {
        var eligible = new TreeSet<>(AnimalManifest.SPECIES);
        io.github.dirtyredz.herbalwitch.data.familiars.FamiliarManifest.SPECIES.forEach(s -> eligible.add("herbalwitch:familiars/entity/" + s));
        assertEquals(eligible, values("entity_types/animals/eligible"));
        var capturable = new TreeSet<>(AnimalManifest.SPECIES); capturable.remove("bat");
        assertEquals(capturable, values("entity_types/animals/capturable"));
        var ai = new TreeSet<>(capturable); ai.removeAll(Set.of("axolotl", "goat"));
        for (String s : Set.of("raven", "owl", "snake")) ai.add("herbalwitch:familiars/entity/" + s);
        assertEquals(ai, values("entity_types/animals/ai/attractable"));
        assertEquals(ai, values("entity_types/animals/ai/calmable"));
        assertTrue(values("entity_types/animals/excluded").isEmpty());
    }
    @Test void allSixBaitSpeciesAndIngredientGroupsMatchTheBrief() {
        Map.of("grain", Set.of("cow", "sheep", "goat", "horse", "donkey", "mule", "llama"),
                "root", Set.of("pig", "rabbit"), "seed", Set.of("chicken", "parrot"), "fish", Set.of("cat", "axolotl"),
                "meat", Set.of("wolf"), "berry", Set.of("fox", "bat"))
                .forEach((bait, types) -> {
                    var expected = new TreeSet<>(types);
                    var additions = Map.of("root", Set.of("rabbit"), "seed", Set.of("raven"), "fish", Set.of("cat"), "meat", Set.of("wolf", "owl", "snake"), "berry", Set.of("fox", "bat"));
                    for (String s : additions.getOrDefault(bait, Set.of())) expected.add("herbalwitch:familiars/entity/" + s);
                    assertEquals(expected, values("entity_types/animals/bait/" + bait));
                });
        Map.of("grain", Set.of("wheat"), "root", Set.of("carrot", "beetroot"),
                "seed", Set.of("wheat_seeds", "melon_seeds", "pumpkin_seeds", "beetroot_seeds"),
                "fish", Set.of("cod", "salmon", "tropical_fish"), "meat", Set.of("beef", "porkchop", "chicken", "mutton", "rabbit"),
                "berry", Set.of("sweet_berries", "glow_berries", "apple"))
                .forEach((bait, foods) -> assertEquals(foods, values("items/animals/food/" + bait)));
    }
}
