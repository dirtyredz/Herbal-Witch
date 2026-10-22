package io.github.dirtyredz.herbalwitch.magic.witch.grimoire;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Characterizes page rounding before moving it into the shared tick conversion contract. */
class WitchTimingTest {
    @Test
    void wholeSecondsRoundUpIncludingFractionalDurations() {
        assertEquals(0, WitchEntries.seconds(0));
        assertEquals(1, WitchEntries.seconds(1));
        assertEquals(1, WitchEntries.seconds(20));
        assertEquals(2, WitchEntries.seconds(21));
        assertEquals(3, WitchEntries.seconds(50));
    }

    @Test
    void wholeMinutesRoundUpAtTheBoundary() {
        assertEquals(0, WitchEntries.minutes(0));
        assertEquals(1, WitchEntries.minutes(1));
        assertEquals(1, WitchEntries.minutes(1200));
        assertEquals(2, WitchEntries.minutes(1201));
    }
}
