package io.github.dirtyredz.herbalwitch.stations.process;

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
        assertArrayEquals(new int[] {1, 0}, ProcessJob.assign(matches, occupied));
    }

    @Test
    void everyOccupiedSlotMustBeUsed() {
        boolean[][] one = {{true, false, false}};
        assertNull(ProcessJob.assign(one, new boolean[] {true, true, false}), "an unrelated item must block the recipe");
        assertNotNull(ProcessJob.assign(one, new boolean[] {true, false, false}));
    }

    @Test
    void anIngredientWithoutAMatchingSlotFails() {
        boolean[][] matches = {{true, false}, {true, false}};
        assertNull(ProcessJob.assign(matches, new boolean[] {true, true}), "two ingredients cannot share one slot");
    }

    @Test
    void emptyStationMatchesNothing() {
        assertNull(ProcessJob.assign(new boolean[][] {{false}}, new boolean[] {false}));
    }
}
