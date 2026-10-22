package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;

/**
 * The daily sleep reward, pure. Only a genuine completed night's sleep counts (the server's
 * sleep-completion path with a long enough sleep, decided by the caller); entering a bed, setting a
 * spawn point, resetting the rest statistic or changing the time does not. Rewards go by the
 * resulting Overworld calendar day: only a day strictly newer than the last rewarded one grants, so
 * turning time backwards cannot reopen a day. A rewarded sleep also cashes in and clears a restful
 * preparation; a sleep that earns nothing leaves the preparation for a later rewarded night.
 */
public final class SleepRecovery {
    /** What a completed sleep did. */
    public record Outcome(boolean rewarded, int grantedUnits, boolean preparationUsed) {
        static final Outcome NONE = new Outcome(false, 0, false);
    }

    public static final long TICKS_PER_DAY = 24_000L;

    private SleepRecovery() {
    }

    /** The calendar day a day time falls in. */
    public static long day(long dayTime) {
        return Math.floorDiv(dayTime, TICKS_PER_DAY);
    }

    /**
     * @param newDayTime      the Overworld day time the sleep woke the world to
     * @param sleptLongEnough the player slept through the vanilla minimum (100 ticks) on the completion path
     */
    static Outcome completed(WitchState state, EnergyBalance balance, long newDayTime, boolean sleptLongEnough) {
        if (!state.initiated() || !sleptLongEnough) {
            return Outcome.NONE;
        }
        long day = day(newDayTime);
        if (day <= state.lastSleepRewardDay()) {
            return Outcome.NONE;
        }
        boolean prepared = state.restfulTicks() > 0;
        int units = EnergyService.units(balance.sleepAmount()) + (prepared ? EnergyService.units(balance.restfulBonus()) : 0);
        state.setLastSleepRewardDay(day);
        state.setRestfulTicks(0);
        int granted = EnergyService.recover(state, balance, units);
        return new Outcome(true, granted, prepared);
    }
}
