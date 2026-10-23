package io.github.dirtyredz.herbalwitch.data.familiars;

import org.junit.jupiter.api.Test;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarHomeResourcesTest {
    @Test void homesHaveExactRecipesFourFacingsAndEmptySingleItemLoot() {
        var ingredients = Map.of("bed", Map.of("dark_oak_planks", 3, "green_wool", 2, "string", 2), "nest", Map.of("stick", 4, "wheat", 2, "string", 2), "perch", Map.of("dark_oak_slab", 2, "stick", 2, "iron_nugget", 1));
        for (var key : List.of("bed", "nest", "perch")) {
            String path = "familiars/home/" + key;
            var recipe = json(GENERATED.resolve("data/herbalwitch/recipes/" + path + ".json"));
            Map<String, Integer> actual = new TreeMap<>(); recipe.getAsJsonArray("ingredients").forEach(e -> actual.merge(e.getAsJsonObject().get("item").getAsString().substring(10), 1, Integer::sum));
            assertEquals(ingredients.get(key), actual);
            var variants = json(GENERATED.resolve("assets/herbalwitch/blockstates/" + path + ".json")).getAsJsonObject("variants");
            assertEquals(Set.of("facing=north", "facing=east", "facing=south", "facing=west"), variants.keySet());
            var loot = json(GENERATED.resolve("data/herbalwitch/loot_tables/blocks/" + path + ".json")).toString();
            assertFalse(loot.contains("copy_nbt")); assertFalse(loot.contains("set_nbt")); assertTrue(loot.contains("herbalwitch:" + path));
        }
    }
}
