package io.github.dirtyredz.herbalwitch.botany;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbTrait;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The herb catalogue against the independent test manifest. Pure JUnit. */
class HerbCatalogTest {
    @Test
    void fourteenHerbsWithPermanentGroupedIds() {
        assertEquals(BotanyManifest.HERBS.size(), HerbCatalog.ALL.size());
        for (int i = 0; i < HerbCatalog.ALL.size(); i++) {
            HerbSpec herb = HerbCatalog.ALL.get(i);
            BotanyManifest.Herb expected = BotanyManifest.HERBS.get(i);
            assertEquals(expected.id(), herb.id());
            assertEquals(expected.group(), herb.habitat().directory(), herb.id());
            assertEquals(expected.name(), herb.displayName());
            assertEquals(expected.bush(), herb.form() == HerbSpec.Form.BUSH, herb.id());
            assertEquals(expected.dir() + "/plant", herb.plantPath());
            assertEquals(expected.dir() + "/seeds", herb.seedsPath());
            assertEquals(expected.dir() + "/fresh", herb.freshPath());
        }
    }

    @Test
    void startersAreLavenderSageMintChamomile() {
        assertEquals(BotanyManifest.STARTERS, HerbCatalog.STARTERS.stream().map(HerbSpec::id).toList());
        HerbCatalog.ALL.forEach(h -> assertEquals(BotanyManifest.STARTERS.contains(h.id()), h.starter(), h.id()));
    }

    @Test
    void traitsFollowThePlan() {
        Map<String, List<HerbTrait>> expected = Map.ofEntries(
                Map.entry("lavender", List.of(HerbTrait.CALMING, HerbTrait.ATTRACTION)),
                Map.entry("sage", List.of(HerbTrait.PROTECTIVE, HerbTrait.RESTORATIVE)),
                Map.entry("mint", List.of(HerbTrait.RESTORATIVE, HerbTrait.SPEED)),
                Map.entry("chamomile", List.of(HerbTrait.CALMING, HerbTrait.RESTORATIVE)),
                Map.entry("rosemary", List.of(HerbTrait.PROTECTIVE, HerbTrait.SIGHT)),
                Map.entry("thyme", List.of(HerbTrait.PROTECTIVE, HerbTrait.GROWTH)),
                Map.entry("nettle", List.of(HerbTrait.GROWTH, HerbTrait.RESTORATIVE)),
                Map.entry("yarrow", List.of(HerbTrait.RESTORATIVE, HerbTrait.PROTECTIVE)),
                Map.entry("valerian", List.of(HerbTrait.CALMING, HerbTrait.ATTRACTION)),
                Map.entry("mugwort", List.of(HerbTrait.SIGHT, HerbTrait.BINDING)),
                Map.entry("belladonna", List.of(HerbTrait.TOXIC, HerbTrait.SIGHT)),
                Map.entry("nightshade", List.of(HerbTrait.CONCEALMENT, HerbTrait.BINDING)),
                Map.entry("wolfsbane", List.of(HerbTrait.TOXIC, HerbTrait.PROTECTIVE)),
                Map.entry("mandrake", List.of(HerbTrait.BINDING, HerbTrait.GROWTH)));
        HerbCatalog.ALL.forEach(h -> assertEquals(expected.get(h.id()), h.traits(), h.id()));
    }

    @Test
    void harvestResetsCropsToSproutsAndBushesToAgeOne() {
        assertEquals(0, HerbSpec.Form.CROP.harvestResetAge());
        assertEquals(1, HerbSpec.Form.BUSH.harvestResetAge());
        assertTrue(HerbSpec.MAX_AGE == 3);
    }
}
