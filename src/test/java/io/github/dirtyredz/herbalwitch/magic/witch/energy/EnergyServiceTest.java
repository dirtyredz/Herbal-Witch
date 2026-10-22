package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService.Result;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fixed-point energy arithmetic under the default balance: checked debits, clamped recovery, practice and rule changes. */
class EnergyServiceTest {
    private static final EnergyBalance B = EnergyBalance.DEFAULTS;
    private static final UUID ID = UUID.nameUUIDFromBytes("witch".getBytes());

    private static WitchState witch() {
        WitchState s = new WitchState();
        EnergyService.initiate(s, B, ID);
        return s;
    }

    @Test
    void initiateFillsToTheInitialCapacity() {
        WitchState s = witch();
        assertTrue(s.initiated());
        assertEquals(ID, s.initiationId().orElseThrow());
        assertEquals(10_000, s.energyUnits());
        assertEquals(10_000, EnergyService.capacityUnits(s, B));
    }

    @Test
    void nonWitchHasNoCapacity() {
        assertEquals(0, EnergyService.capacityUnits(new WitchState(), B));
    }

    @Test
    void debitSpendsExactlyTheAmount() {
        WitchState s = witch();
        assertEquals(Result.OK, EnergyService.debit(s, 5_000));
        assertEquals(5_000, s.energyUnits());
        assertEquals(Result.OK, EnergyService.debit(s, 5_000));
        assertEquals(0, s.energyUnits());
        assertEquals(Result.OK, EnergyService.debit(s, 0));
        assertEquals(0, s.energyUnits());
    }

    @Test
    void failedDebitsLeaveTheStateUnchanged() {
        WitchState s = witch();
        s.setEnergyUnits(100);
        WitchState before = s.copy();
        assertEquals(Result.INSUFFICIENT, EnergyService.debit(s, 101));
        assertEquals(Result.INVALID, EnergyService.debit(s, -1));
        assertTrue(s.sameAs(before));

        WitchState plain = new WitchState();
        assertEquals(Result.NOT_WITCH, EnergyService.debit(plain, 1));
        assertTrue(plain.sameAs(new WitchState()));
    }

    @Test
    void checkReportsWithoutChangingAnything() {
        WitchState s = witch();
        assertEquals(Result.OK, EnergyService.check(s, 10_000));
        assertEquals(Result.INSUFFICIENT, EnergyService.check(s, 10_001));
        assertEquals(Result.INVALID, EnergyService.check(s, -1));
        assertEquals(10_000, s.energyUnits());
    }

    @Test
    void recoverClampsToCapacityAndReturnsWhatWasAdded() {
        WitchState s = witch();
        s.setEnergyUnits(9_000);
        assertEquals(500, EnergyService.recover(s, B, 500));
        assertEquals(9_500, s.energyUnits());
        assertEquals(500, EnergyService.recover(s, B, 5_000));
        assertEquals(10_000, s.energyUnits());
        assertEquals(0, EnergyService.recover(s, B, 100));
        assertEquals(10_000, s.energyUnits());
    }

    @Test
    void recoverRefusesNonWitchesAndNegativeAmounts() {
        WitchState plain = new WitchState();
        assertEquals(0, EnergyService.recover(plain, B, 100));
        assertEquals(0, plain.energyUnits());
        WitchState s = witch();
        s.setEnergyUnits(100);
        assertEquals(0, EnergyService.recover(s, B, -50));
        assertEquals(0, EnergyService.recover(s, B, 0));
        assertEquals(100, s.energyUnits());
    }

    @Test
    void awardPracticeReportsThresholdsCrossedAndGrowsCapacityWithoutRefilling() {
        WitchState s = witch();
        assertEquals(0, EnergyService.awardPractice(s, B, 4_999));
        assertEquals(10_000, EnergyService.capacityUnits(s, B));
        assertEquals(1, EnergyService.awardPractice(s, B, 1));
        assertEquals(11_000, EnergyService.capacityUnits(s, B));
        assertEquals(10_000, s.energyUnits(), "new room is not filled");
        assertEquals(3, EnergyService.awardPractice(s, B, 15_000));
        assertEquals(14_000, EnergyService.capacityUnits(s, B));
        assertEquals(20_000, s.practiceSpent());
    }

    @Test
    void practiceIsBoundedAtTheFinalThreshold() {
        WitchState s = witch();
        assertEquals(10, EnergyService.awardPractice(s, B, 1_000_000));
        assertEquals(500 * 100, s.practiceSpent());
        assertEquals(20_000, EnergyService.capacityUnits(s, B));
        assertEquals(0, EnergyService.awardPractice(s, B, 1_000));
        assertEquals(50_000, s.practiceSpent());
    }

    @Test
    void awardPracticeIgnoresNonWitchesAndNonPositiveAmounts() {
        WitchState plain = new WitchState();
        assertEquals(0, EnergyService.awardPractice(plain, B, 5_000));
        assertEquals(0, plain.practiceSpent());
        WitchState s = witch();
        assertEquals(0, EnergyService.awardPractice(s, B, 0));
        assertEquals(0, EnergyService.awardPractice(s, B, -5_000));
        assertEquals(0, s.practiceSpent());
    }

    private static EnergyBalance withInitialCapacity(int capacity, List<Integer> thresholds) {
        return new EnergyBalance(capacity, thresholds, 10, 50, 10, 100, 1, 40, 20, 2, 30, 25, 1200, 30, 24_000, 10, 15, 25);
    }

    @Test
    void clampLowersEnergyToASmallerCapacityAndReportsIt() {
        WitchState s = witch();
        EnergyBalance smaller = withInitialCapacity(50, List.of(50, 100));
        assertTrue(EnergyService.clamp(s, smaller));
        assertEquals(5_000, s.energyUnits());
        assertFalse(EnergyService.clamp(s, smaller), "a second clamp changes nothing");
    }

    @Test
    void clampNeverRaisesEnergy() {
        WitchState s = witch();
        s.setEnergyUnits(2_000);
        EnergyBalance larger = withInitialCapacity(200, List.of(50, 100));
        assertFalse(EnergyService.clamp(s, larger));
        assertEquals(2_000, s.energyUnits());
    }

    @Test
    void clampPullsPracticeBackToANewFinalThreshold() {
        WitchState s = witch();
        s.setPracticeSpent(5_000);
        EnergyBalance shorter = withInitialCapacity(100, List.of(10));
        assertTrue(EnergyService.clamp(s, shorter));
        assertEquals(1_000, s.practiceSpent());
    }

    @Test
    void unitsConvertsWholeEnergyAndRefusesNegativeOrOverflow() {
        assertEquals(0, EnergyService.units(0));
        assertEquals(500, EnergyService.units(5));
        assertEquals(10_000_000, EnergyService.units(EnergyBalance.MAX_ENERGY));
        assertThrows(IllegalArgumentException.class, () -> EnergyService.units(-1));
        assertThrows(IllegalArgumentException.class, () -> EnergyService.units(EnergyBalance.MAX_ENERGY + 1));
        assertThrows(IllegalArgumentException.class, () -> EnergyService.units(Integer.MAX_VALUE));
    }
}
