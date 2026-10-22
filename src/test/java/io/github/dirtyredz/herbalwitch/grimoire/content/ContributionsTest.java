package io.github.dirtyredz.herbalwitch.grimoire.content;

import io.github.dirtyredz.herbalwitch.apothecary.botany.grimoire.HerbEntries;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.grimoire.AdvancedPreparationEntries;
import io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire.BrewingEntries;
import io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire.StationEntries;
import io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire.StationRecipeEntries;
import io.github.dirtyredz.herbalwitch.decor.grimoire.DecorEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;
import io.github.dirtyredz.herbalwitch.magic.charm.grimoire.CharmEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ward.grimoire.WardEntries;
import io.github.dirtyredz.herbalwitch.magic.witch.grimoire.WitchEntries;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContributionsTest {
    @Test
    void ordersByPriorityThenRegistration() {
        var c = new Contributions<String>();
        c.add(20, "b1");
        c.add(10, "a");
        c.add(20, "b2");
        c.add(5, "first");
        assertEquals(List.of("first", "a", "b1", "b2"), c.ordered());
    }

    @Test
    void concurrentRegistrationLosesNothing() throws Exception {
        var c = new Contributions<Integer>();
        var pool = Executors.newFixedThreadPool(8);
        var start = new CountDownLatch(1);
        for (int t = 0; t < 8; t++) {
            int base = t * 1000;
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < 500; i++) c.add(i % 7, base + i);
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(4000, c.ordered().size());
        assertEquals(4000, new java.util.HashSet<>(c.ordered()).size());
    }

    /** The grimoire index reads sources in this order (the former GrimoireContent.Order enum); do not reorder. */
    @Test
    void sourcePrioritiesKeepTheOriginalReadingOrder() {
        List<Integer> priorities = new ArrayList<>(List.of(GuideEntries.PRIORITY, WitchEntries.PRIORITY, BrewingEntries.PRIORITY,
                AdvancedPreparationEntries.PRIORITY, StationEntries.PRIORITY, DecorEntries.PRIORITY, HerbEntries.PRIORITY,
                StationRecipeEntries.PRIORITY, AltarEntries.PRIORITY, WardEntries.PRIORITY, CharmEntries.PRIORITY));
        assertEquals(List.of(0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000), priorities);
        assertEquals(priorities.stream().sorted().distinct().toList(), priorities, "strictly increasing and distinct");
    }
}
