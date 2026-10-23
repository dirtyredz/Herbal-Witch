package io.github.dirtyredz.herbalwitch.data.familiars;

import org.junit.jupiter.api.Test;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarRetrievalResourcesTest {
    @Test void defaultPickupListContainsOnlyHerbsSeedsAndSmallOrdinaryMaterials() {
        var actual = new HashSet<String>();
        generated("data/herbalwitch/tags/items/familiars/retrievable.json").getAsJsonArray("values").forEach(v -> actual.add(v.getAsString()));
        assertEquals(Set.of("#herbalwitch:botany/fresh_herbs", "#herbalwitch:botany/seeds", "minecraft:stick", "minecraft:string", "minecraft:feather",
                "minecraft:flint", "minecraft:wheat_seeds", "minecraft:pumpkin_seeds", "minecraft:melon_seeds", "minecraft:beetroot_seeds",
                "minecraft:iron_nugget", "minecraft:copper_ingot", "minecraft:coal", "minecraft:clay_ball"), actual);
    }
}
