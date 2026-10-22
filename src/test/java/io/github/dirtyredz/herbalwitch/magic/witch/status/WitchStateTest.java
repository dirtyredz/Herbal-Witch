package io.github.dirtyredz.herbalwitch.magic.witch.status;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The plain witch state: fresh defaults, independent copies, rollback restore, equality and non-negative setters. */
class WitchStateTest {
    private static final UUID ID = UUID.nameUUIDFromBytes("witch".getBytes());

    private static WitchState sample() {
        WitchState s = new WitchState();
        s.initiate(ID);
        s.setEnergyUnits(5000);
        s.setPracticeSpent(1200);
        s.setTeaCooldownTicks(60);
        s.setRestfulTicks(300);
        s.setLastSleepRewardDay(9);
        return s;
    }

    @Test
    void freshStateIsNotAWitchWithNothingStored() {
        WitchState s = new WitchState();
        assertFalse(s.initiated());
        assertTrue(s.initiationId().isEmpty());
        assertEquals(0, s.energyUnits());
        assertEquals(0, s.practiceSpent());
        assertEquals(0, s.teaCooldownTicks());
        assertEquals(0, s.restfulTicks());
        assertEquals(WitchState.NEVER, s.lastSleepRewardDay());
    }

    @Test
    void initiateMarksAWitchWithItsIdentity() {
        WitchState s = new WitchState();
        s.initiate(ID);
        assertTrue(s.initiated());
        assertEquals(ID, s.initiationId().orElseThrow());
    }

    @Test
    void copyIsDeepAndIndependent() {
        WitchState original = sample();
        WitchState copy = original.copy();
        assertTrue(original.sameAs(copy));
        copy.setEnergyUnits(1);
        copy.setPracticeSpent(2);
        copy.setTeaCooldownTicks(3);
        copy.setRestfulTicks(4);
        copy.setLastSleepRewardDay(5);
        assertEquals(5000, original.energyUnits());
        assertEquals(1200, original.practiceSpent());
        assertEquals(60, original.teaCooldownTicks());
        assertEquals(300, original.restfulTicks());
        assertEquals(9, original.lastSleepRewardDay());
        assertFalse(original.sameAs(copy));
    }

    @Test
    void restoreFromTakesEveryField() {
        WitchState snapshot = sample();
        WitchState live = new WitchState();
        live.restoreFrom(snapshot);
        assertTrue(live.sameAs(snapshot));
        assertEquals(ID, live.initiationId().orElseThrow());
        snapshot.setEnergyUnits(0);
        assertEquals(5000, live.energyUnits(), "the restore copies values, it does not alias the snapshot");
    }

    @Test
    void sameAsComparesEveryField() {
        WitchState a = sample();
        WitchState b = sample();
        assertTrue(a.sameAs(b));
        b.setLastSleepRewardDay(10);
        assertFalse(a.sameAs(b));
        b = sample();
        b.setRestfulTicks(0);
        assertFalse(a.sameAs(b));
        b = sample();
        b.initiate(UUID.nameUUIDFromBytes("other".getBytes()));
        assertFalse(a.sameAs(b));
        assertFalse(a.sameAs(new WitchState()));
    }

    @Test
    void negativeSettersClampToZero() {
        WitchState s = new WitchState();
        s.setEnergyUnits(-5);
        s.setPracticeSpent(-5);
        s.setTeaCooldownTicks(-5);
        s.setRestfulTicks(-5);
        assertEquals(0, s.energyUnits());
        assertEquals(0, s.practiceSpent());
        assertEquals(0, s.teaCooldownTicks());
        assertEquals(0, s.restfulTicks());
    }
}
