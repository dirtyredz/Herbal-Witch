package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Recovery arithmetic: the capped environmental bonus, fixed-point scaling, passive rest stillness and meditation timing. */
class EnergyRecoveryTest {
    private static final EnergyBalance B = EnergyBalance.DEFAULTS;

    /** Ticks a still player until the tracker reports a grant; returns how many ticks that took (or -1). */
    private static int ticksUntilGrant(EnergyRecovery.Rest rest, int interval, int limit) {
        for (int i = 1; i <= limit; i++) {
            if (rest.tick(1, 64, 1, false, interval)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void bonusPercentAddsNatureAndMoon() {
        assertEquals(0, EnergyRecovery.bonusPercent(B, false, false));
        assertEquals(10, EnergyRecovery.bonusPercent(B, true, false));
        assertEquals(15, EnergyRecovery.bonusPercent(B, false, true));
        assertEquals(25, EnergyRecovery.bonusPercent(B, true, true));
    }

    @Test
    void bonusPercentNeverExceedsTheCap() {
        EnergyBalance low = new EnergyBalance(100, List.of(50), 10, 50, 10, 100, 1, 40, 20, 2, 30, 25, 1200, 30, 24_000, 10, 15, 20);
        assertEquals(20, EnergyRecovery.bonusPercent(low, true, true));
        assertEquals(10, EnergyRecovery.bonusPercent(low, true, false));
    }

    @Test
    void withBonusIsFixedPointRoundedDown() {
        assertEquals(100, EnergyRecovery.withBonus(100, 0));
        assertEquals(125, EnergyRecovery.withBonus(100, 25));
        assertEquals(170, EnergyRecovery.withBonus(155, 10), "170.5 rounds down");
        assertEquals(100, EnergyRecovery.withBonus(100, -5), "a negative percent is ignored");
    }

    @Test
    void restNeedsAFullIntervalOfStillnessAfterTheAnchorTick() {
        EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
        assertFalse(rest.tick(1, 64, 1, false, 3), "the first tick only anchors");
        assertFalse(rest.resting());
        assertFalse(rest.tick(1, 64, 1, false, 3));
        assertFalse(rest.tick(1, 64, 1, false, 3));
        assertTrue(rest.resting());
        assertTrue(rest.tick(1, 64, 1, false, 3), "the third still tick completes the interval");
        assertEquals(0, rest.ticks(), "the counter restarts after a grant");
        assertEquals(3, ticksUntilGrant(rest, 3, 10), "the next grant needs another full interval");
    }

    @Test
    void movementBeyondTheRadiusResetsRest() {
        EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
        rest.tick(0, 64, 0, false, 3);
        rest.tick(0, 64, 0, false, 3);
        rest.tick(0, 64, 0, false, 3);
        assertFalse(rest.tick(0.5, 64, 0, false, 3), "moving away re-anchors without a grant");
        assertEquals(0, rest.ticks());
        assertFalse(rest.resting());
        assertFalse(rest.tick(0.5, 64, 0, false, 3), "a fresh interval counts from the new spot");
        assertFalse(rest.tick(0.5, 64, 0, false, 3));
        assertTrue(rest.tick(0.5, 64, 0, false, 3));
    }

    @Test
    void tinyDriftWithinTheRadiusDoesNotReset() {
        EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
        rest.tick(0, 64, 0, false, 3);
        assertFalse(rest.tick(0.05, 64, 0, false, 3));
        assertFalse(rest.tick(0, 64, 0.05, false, 3));
        assertTrue(rest.tick(0.05, 64, 0.05, false, 3), "drift of 0.1 or less keeps counting");
    }

    @Test
    void movementJustBeyondTheRadiusResets() {
        EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
        rest.tick(0, 64, 0, false, 3);
        rest.tick(0, 64, 0, false, 3);
        assertFalse(rest.tick(0.11, 64, 0, false, 3));
        assertEquals(0, rest.ticks());
    }

    @Test
    void disturbanceResetsRest() {
        EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
        rest.tick(0, 64, 0, false, 3);
        rest.tick(0, 64, 0, false, 3);
        rest.tick(0, 64, 0, false, 3);
        assertFalse(rest.tick(0, 64, 0, true, 3));
        assertEquals(0, rest.ticks());
        assertFalse(rest.resting());
    }

    @Test
    void resetForgetsTheAnchor() {
        EnergyRecovery.Rest rest = new EnergyRecovery.Rest();
        rest.tick(0, 64, 0, false, 3);
        rest.tick(0, 64, 0, false, 3);
        assertTrue(rest.resting());
        rest.reset();
        assertFalse(rest.resting());
        assertEquals(0, rest.ticks());
        assertFalse(rest.tick(0, 64, 0, false, 3), "the next tick re-anchors rather than counting");
        assertEquals(0, rest.ticks());
    }

    @Test
    void meditationGrantsAfterSettlingThenEveryInterval() {
        for (int elapsed = 1; elapsed <= 40; elapsed++) {
            assertFalse(EnergyRecovery.meditationGrant(elapsed, 40, 20), "settling at " + elapsed);
        }
        assertFalse(EnergyRecovery.meditationGrant(41, 40, 20));
        assertFalse(EnergyRecovery.meditationGrant(59, 40, 20));
        assertTrue(EnergyRecovery.meditationGrant(60, 40, 20));
        assertFalse(EnergyRecovery.meditationGrant(61, 40, 20));
        assertTrue(EnergyRecovery.meditationGrant(80, 40, 20));
        assertTrue(EnergyRecovery.meditationGrant(100, 40, 20));
    }
}
