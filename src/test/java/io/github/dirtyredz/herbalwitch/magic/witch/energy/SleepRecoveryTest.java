package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The once-per-day sleep reward: day arithmetic, replay and backward time, the restful preparation and capacity overflow. */
class SleepRecoveryTest {
    private static final EnergyBalance B = EnergyBalance.DEFAULTS;
    private static final long DAY = SleepRecovery.TICKS_PER_DAY;

    /** A witch with no energy, so the whole grant is visible. */
    private static WitchState emptyWitch() {
        WitchState s = new WitchState();
        EnergyService.initiate(s, B, UUID.nameUUIDFromBytes("witch".getBytes()));
        s.setEnergyUnits(0);
        return s;
    }

    @Test
    void rewardsAWitchOncePerDay() {
        WitchState s = emptyWitch();
        SleepRecovery.Outcome first = SleepRecovery.completed(s, B, 5 * DAY, true);
        assertTrue(first.rewarded());
        assertEquals(3_000, first.grantedUnits());
        assertFalse(first.preparationUsed());
        assertEquals(3_000, s.energyUnits());
        assertEquals(5, s.lastSleepRewardDay());

        SleepRecovery.Outcome second = SleepRecovery.completed(s, B, 5 * DAY + 500, true);
        assertFalse(second.rewarded());
        assertEquals(0, second.grantedUnits());
        assertEquals(3_000, s.energyUnits());
    }

    @Test
    void aLaterDayRewardsAgain() {
        WitchState s = emptyWitch();
        SleepRecovery.completed(s, B, 5 * DAY, true);
        SleepRecovery.Outcome next = SleepRecovery.completed(s, B, 6 * DAY, true);
        assertTrue(next.rewarded());
        assertEquals(6_000, s.energyUnits());
        assertEquals(6, s.lastSleepRewardDay());
    }

    @Test
    void backwardTimeNeverReopensAnEarlierDay() {
        WitchState s = emptyWitch();
        SleepRecovery.completed(s, B, 6 * DAY, true);
        SleepRecovery.Outcome back = SleepRecovery.completed(s, B, 4 * DAY, true);
        assertFalse(back.rewarded());
        assertEquals(3_000, s.energyUnits());
        assertEquals(6, s.lastSleepRewardDay());
    }

    @Test
    void dayBoundaryFollowsTheCalendarDay() {
        assertEquals(0, SleepRecovery.day(0));
        assertEquals(0, SleepRecovery.day(DAY - 1));
        assertEquals(1, SleepRecovery.day(DAY));
        WitchState s = emptyWitch();
        assertTrue(SleepRecovery.completed(s, B, 3 * DAY - 1, true).rewarded());
        assertEquals(2, s.lastSleepRewardDay());
        assertTrue(SleepRecovery.completed(s, B, 3 * DAY, true).rewarded());
    }

    @Test
    void aShortSleepRewardsNothingAndChangesNothing() {
        WitchState s = emptyWitch();
        WitchState before = s.copy();
        SleepRecovery.Outcome out = SleepRecovery.completed(s, B, 5 * DAY, false);
        assertFalse(out.rewarded());
        assertTrue(s.sameAs(before));
        assertEquals(WitchState.NEVER, s.lastSleepRewardDay());
    }

    @Test
    void nonWitchesGetNothing() {
        WitchState plain = new WitchState();
        SleepRecovery.Outcome out = SleepRecovery.completed(plain, B, 5 * DAY, true);
        assertFalse(out.rewarded());
        assertTrue(plain.sameAs(new WitchState()));
    }

    @Test
    void restfulPreparationAddsItsBonusAndIsCleared() {
        WitchState s = emptyWitch();
        s.setRestfulTicks(500);
        SleepRecovery.Outcome out = SleepRecovery.completed(s, B, 5 * DAY, true);
        assertTrue(out.rewarded());
        assertTrue(out.preparationUsed());
        assertEquals(6_000, out.grantedUnits());
        assertEquals(6_000, s.energyUnits());
        assertEquals(0, s.restfulTicks());
    }

    @Test
    void preparationIsKeptWhenTheSleepIsNotRewarded() {
        WitchState s = emptyWitch();
        SleepRecovery.completed(s, B, 5 * DAY, true);
        s.setRestfulTicks(500);
        SleepRecovery.Outcome sameDay = SleepRecovery.completed(s, B, 5 * DAY + 10, true);
        assertFalse(sameDay.rewarded());
        assertEquals(500, s.restfulTicks());
        SleepRecovery.Outcome tooShort = SleepRecovery.completed(s, B, 7 * DAY, false);
        assertFalse(tooShort.rewarded());
        assertEquals(500, s.restfulTicks());
        assertTrue(SleepRecovery.completed(s, B, 7 * DAY, true).preparationUsed());
    }

    @Test
    void overflowBeyondCapacityClampsButStillConsumesTheDayAndPreparation() {
        WitchState s = emptyWitch();
        s.setEnergyUnits(9_900);
        s.setRestfulTicks(500);
        SleepRecovery.Outcome out = SleepRecovery.completed(s, B, 5 * DAY, true);
        assertTrue(out.rewarded());
        assertTrue(out.preparationUsed());
        assertEquals(100, out.grantedUnits());
        assertEquals(10_000, s.energyUnits());
        assertEquals(5, s.lastSleepRewardDay());
        assertEquals(0, s.restfulTicks());
        assertFalse(SleepRecovery.completed(s, B, 5 * DAY, true).rewarded());
    }
}
