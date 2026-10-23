package io.github.dirtyredz.herbalwitch.data.familiars;

import org.junit.jupiter.api.Test;
import java.util.List;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarEquipmentResourcesTest {
    @Test void everyAccessoryHasItsAuthoredModelAndExactPaidRecipe() {
        var ingredients = List.of("minecraft:gold_nugget", "minecraft:rabbit_hide", "minecraft:glass_pane", "minecraft:sweet_berries",
                "minecraft:iron_ingot", "minecraft:copper_ingot", "herbalwitch:preparations/herbs/temperate/chamomile/bundle", "minecraft:amethyst_shard");
        for (int i = 0; i < FamiliarManifest.EQUIPMENT.size(); i++) {
            String gear = FamiliarManifest.EQUIPMENT.get(i), path = "familiars/equipment/" + gear;
            var model = generated("assets/herbalwitch/models/item/" + path + ".json");
            assertEquals("herbalwitch:block/" + path + "/base", model.get("parent").getAsString());
            var recipe = generated("data/herbalwitch/recipes/processing/charm_work/" + path + ".json");
            assertEquals(200, recipe.get("duration").getAsInt());
            assertEquals(5, recipe.getAsJsonObject("requirements").get("energy").getAsInt());
            assertTrue(recipe.toString().contains(ingredients.get(i)), gear);
            assertTrue(recipe.toString().contains(gear.equals("bat_chime") ? "minecraft:iron_nugget" : "minecraft:leather"), gear);
            assertTrue(recipe.toString().contains("minecraft:string"), gear);
        }
    }
}
