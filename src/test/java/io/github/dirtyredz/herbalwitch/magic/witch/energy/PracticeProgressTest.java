package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Capacity progression at every default threshold, and the validation of an energy balance. */
class PracticeProgressTest {
    private static final EnergyBalance B = EnergyBalance.DEFAULTS;

    private static EnergyBalance variant(List<Integer> thresholds, int initiationCost, int attunementCost, int bonusCap) {
        return new EnergyBalance(100, thresholds, 10, initiationCost, attunementCost, 100, 1, 40, 20, 2, 30, 25, 1200, 30, 24_000, 10, 15,
                bonusCap);
    }

    @Test
    void defaultThresholdsAreFiftyToFiveHundredInSteps() {
        assertEquals(List.of(50, 100, 150, 200, 250, 300, 350, 400, 450, 500), B.thresholds());
    }

    @Test
    void capacityGrowsByTenAtEachBoundary() {
        assertEquals(100, PracticeProgress.capacity(B, 0));
        for (int i = 0; i < B.thresholds().size(); i++) {
            int units = B.thresholds().get(i) * EnergyBalance.UNITS;
            assertEquals(100 + 10 * i, PracticeProgress.capacity(B, units - 1), "just below threshold " + (i + 1));
            assertEquals(100 + 10 * (i + 1), PracticeProgress.capacity(B, units), "at threshold " + (i + 1));
            assertEquals(i + 1, PracticeProgress.reached(B, units));
            assertEquals(PracticeProgress.capacity(B, units) * EnergyBalance.UNITS, PracticeProgress.capacityUnits(B, units));
        }
    }

    @Test
    void finalCapacityIsTwoHundred() {
        assertEquals(200, PracticeProgress.capacity(B, 50_000));
        assertEquals(200, PracticeProgress.capacity(B, 5_000_000), "nothing past the last threshold");
        assertEquals(200, B.finalCapacity());
        assertEquals(50_000, B.practiceCeilingUnits());
    }

    @Test
    void nextIsTheFollowingThresholdOrMinusOne() {
        assertEquals(50, PracticeProgress.next(B, 0));
        assertEquals(50, PracticeProgress.next(B, 4_999));
        assertEquals(100, PracticeProgress.next(B, 5_000));
        assertEquals(500, PracticeProgress.next(B, 45_000));
        assertEquals(500, PracticeProgress.next(B, 49_999));
        assertEquals(-1, PracticeProgress.next(B, 50_000));
        assertEquals(-1, PracticeProgress.next(B, 99_999));
    }

    @Test
    void completeOnlyAtTheFinalThreshold() {
        assertFalse(PracticeProgress.complete(B, 0));
        assertFalse(PracticeProgress.complete(B, 49_999));
        assertTrue(PracticeProgress.complete(B, 50_000));
        assertTrue(PracticeProgress.complete(B, 60_000));
    }

    @Test
    void defaultsAreValid() {
        assertEquals(List.of(), B.problems());
    }

    @Test
    void nonIncreasingThresholdsAreInvalid() {
        assertFalse(variant(List.of(50, 50, 100), 50, 10, 25).problems().isEmpty());
        assertFalse(variant(List.of(100, 50), 50, 10, 25).problems().isEmpty());
        assertFalse(variant(List.of(0, 50), 50, 10, 25).problems().isEmpty());
    }

    @Test
    void emptyThresholdsAreInvalid() {
        assertFalse(variant(List.of(), 50, 10, 25).problems().isEmpty());
    }

    @Test
    void negativeCostsAreInvalid() {
        assertFalse(variant(List.of(50), -1, 10, 25).problems().isEmpty());
        assertFalse(variant(List.of(50), 50, -1, 25).problems().isEmpty());
    }

    @Test
    void bonusCapAboveOneHundredIsInvalid() {
        assertFalse(variant(List.of(50), 50, 10, 101).problems().isEmpty());
        assertTrue(variant(List.of(50), 50, 10, 100).problems().isEmpty());
    }
}
