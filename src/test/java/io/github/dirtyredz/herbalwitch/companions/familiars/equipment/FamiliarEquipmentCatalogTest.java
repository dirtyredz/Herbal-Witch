package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarEquipmentCatalogTest {
    @Test void everySpeciesHasOnePermanentCompatiblePiece() {
        assertEquals(Set.of("cat_bell", "raven_satchel", "owl_lens", "fox_pouch", "wolf_harness", "snake_ring", "rabbit_sachet", "bat_chime"),
                FamiliarEquipmentCatalog.ALL.stream().map(FamiliarEquipmentCatalog.Gear::key).collect(Collectors.toSet()));
        for (var species : FamiliarCatalog.ALL) {
            var gear = FamiliarEquipmentCatalog.find(species.equipment()).orElseThrow();
            assertTrue(gear.accepts(species.key()));
            assertEquals(1, FamiliarEquipmentCatalog.ALL.stream().filter(g -> g.accepts(species.key())).count());
        }
        assertEquals(2, FamiliarEquipmentRules.SLOTS); assertEquals(16, FamiliarEquipmentRules.CARGO_LIMIT);
    }
}
