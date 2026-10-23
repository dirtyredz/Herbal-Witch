package io.github.dirtyredz.herbalwitch.data.familiars;

import org.junit.jupiter.api.Test;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarAcquisitionResourcesTest {
    @Test void wildSpeciesUseExactPassiveWeightsAndAuthoredCreativeEggs() {
        for (var expected : Map.of("raven", 6, "owl", 4, "snake", 6).entrySet()) {
            String species = expected.getKey(); var modifier = generated("data/herbalwitch/forge/biome_modifier/familiars/spawns/" + species + ".json");
            assertEquals("forge:add_spawns", modifier.get("type").getAsString());
            assertEquals("#herbalwitch:familiars/spawns/" + (species.equals("snake") ? "warm_dry" : "wooded"), modifier.get("biomes").getAsString());
            var spawners = modifier.get("spawners"); var spawn = spawners.isJsonArray() ? spawners.getAsJsonArray().get(0).getAsJsonObject() : spawners.getAsJsonObject();
            assertEquals("herbalwitch:familiars/entity/" + species, spawn.get("type").getAsString());
            assertEquals(expected.getValue(), spawn.get("weight").getAsInt());
            assertEquals(1, spawn.get("minCount").getAsInt()); assertEquals(2, spawn.get("maxCount").getAsInt());
            assertEquals("herbalwitch:block/familiars/spawn_egg/" + species + "/base",
                    generated("assets/herbalwitch/models/item/familiars/spawn_egg/" + species + ".json").get("parent").getAsString());
        }
    }
}
