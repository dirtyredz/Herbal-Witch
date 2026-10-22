package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.matching.ProcessMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Shapeless, exact slot assignment for reservations. Pure JUnit. */
class ProcessReservationTest {
    @Test
    void eachIngredientTakesItsOwnMatchingSlot() {
        // ingredient 0 fits slots 0 and 1, ingredient 1 only slot 0: the search must back-track.
        boolean[][] matches = {{true, true, false}, {true, false, false}};
        boolean[] occupied = {true, true, false};
        assertArrayEquals(new int[] {1, 0}, ProcessMatcher.assign(matches, occupied));
    }

    @Test
    void everyOccupiedSlotMustBeUsed() {
        boolean[][] one = {{true, false, false}};
        assertNull(ProcessMatcher.assign(one, new boolean[] {true, true, false}), "an unrelated item must block the recipe");
        assertNotNull(ProcessMatcher.assign(one, new boolean[] {true, false, false}));
    }

    @Test
    void anIngredientWithoutAMatchingSlotFails() {
        boolean[][] matches = {{true, false}, {true, false}};
        assertNull(ProcessMatcher.assign(matches, new boolean[] {true, true}), "two ingredients cannot share one slot");
    }

    @Test
    void emptyStationMatchesNothing() {
        assertNull(ProcessMatcher.assign(new boolean[][] {{false}}, new boolean[] {false}));
    }
}
