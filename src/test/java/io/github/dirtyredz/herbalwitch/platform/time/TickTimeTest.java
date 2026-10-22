package io.github.dirtyredz.herbalwitch.platform.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Characterizes page rounding before moving it into the shared tick conversion contract. */
class TickTimeTest {
    @Test
    void wholeSecondsRoundUpIncludingFractionalDurations() {
        assertEquals(0, TickTime.ceilingSeconds(0));
        assertEquals(1, TickTime.ceilingSeconds(1));
        assertEquals(1, TickTime.ceilingSeconds(20));
        assertEquals(2, TickTime.ceilingSeconds(21));
        assertEquals(3, TickTime.ceilingSeconds(50));
    }

    @Test
    void wholeMinutesRoundUpAtTheBoundary() {
        assertEquals(0, TickTime.ceilingMinutes(0));
        assertEquals(1, TickTime.ceilingMinutes(1));
        assertEquals(1, TickTime.ceilingMinutes(1200));
        assertEquals(2, TickTime.ceilingMinutes(1201));
    }
    @Test
    void recipeSecondsKeepFractionsInsteadOfCeiling() {
        assertEquals("0", TickTime.fractionalSeconds(0));
        assertEquals("0.05", TickTime.fractionalSeconds(1));
        assertEquals("1", TickTime.fractionalSeconds(20));
        assertEquals("2.5", TickTime.fractionalSeconds(50));
        assertEquals("60", TickTime.fractionalSeconds(1200));
    }
}
