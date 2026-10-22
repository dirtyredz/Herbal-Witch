package io.github.dirtyredz.herbalwitch.companions.animals.species;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class AnimalEligibilityTest {
    @Test void exactVanillaMatrixHasSixteenEntriesAndFifteenTransportAdapters() {
        assertEquals(Set.of("cow", "sheep", "goat", "pig", "rabbit", "chicken", "horse", "donkey", "mule", "llama",
                "cat", "wolf", "fox", "parrot", "axolotl", "bat"), AnimalProfiles.ALL.stream().map(p -> p.id().substring(10)).collect(java.util.stream.Collectors.toSet()));
        assertEquals(15, AnimalProfiles.ALL.stream().filter(AnimalProfile::capturable).count());
        assertEquals(13, AnimalProfiles.ALL.stream().filter(AnimalProfile::goalAi).count());
        assertFalse(AnimalProfiles.find("minecraft:bat").orElseThrow().adoptable());
    }

    @Test void exclusionsAndAmbiguousTagsWinAndReloadIsReadLive() {
        assertTrue(AnimalEligibility.resolve("minecraft:cow", true, true, false, false, Set.of(Bait.GRAIN)).isPresent());
        assertTrue(AnimalEligibility.resolve("minecraft:cow", true, true, true, false, Set.of(Bait.GRAIN)).isEmpty());
        assertTrue(AnimalEligibility.resolve("minecraft:cow", true, true, false, false, Set.of(Bait.GRAIN, Bait.ROOT)).isEmpty());
        assertTrue(AnimalEligibility.resolve("minecraft:cow", true, false, false, false, Set.of(Bait.GRAIN)).isEmpty());
    }

    @Test void inheritanceDoesNotAdmitUnlistedVanillaAndModdedTagsNeverGrantAdapters() {
        for (String id : Set.of("minecraft:mooshroom", "minecraft:trader_llama", "minecraft:ocelot", "doggytalents:dog"))
            assertTrue(AnimalEligibility.resolve(id, true, true, false, true, Set.of(Bait.MEAT)).isEmpty());
        assertTrue(AnimalEligibility.resolve("example:animal", true, true, false, false, Set.of(Bait.GRAIN)).isEmpty());
        var opted = AnimalEligibility.resolve("example:animal", true, true, false, true, Set.of(Bait.GRAIN)).orElseThrow();
        assertFalse(opted.capturable()); assertFalse(opted.goalAi()); assertFalse(opted.adoptable());
        assertTrue(AnimalEligibility.resolve("example:citizen", false, true, false, true, Set.of(Bait.GRAIN)).isEmpty());
    }

    @Test void defaultsPreserveSpecifiedLimits() {
        var d = AnimalRules.DEFAULTS;
        assertEquals(8, d.relationshipsPerAnimal()); assertEquals(4096, d.lifetime());
        assertEquals(32768, d.payloadBytes()); assertEquals(20, d.transactionInterval());
    }
}
