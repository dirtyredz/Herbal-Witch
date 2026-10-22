package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.PracticeProgress;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;

import java.util.UUID;

/**
 * Energy arithmetic over one {@link WitchState}, pure and fixed-point (units are hundredths). Only
 * trusted server code calls it, with costs it derived itself; a client only ever sends intent.
 * Separate operations for affordability, debit, recovery and practice: a failed check mutates
 * nothing, negative amounts and overflow are refused, recovery clamps to capacity, and practice is
 * awarded only for a completed, successful spend (the caller decides that; this service never
 * implies a transaction across features).
 */
public final class EnergyService {
    /** What an operation did. */
    public enum Result {
        OK,
        NOT_WITCH,
        INSUFFICIENT,
        INVALID
    }

    private EnergyService() {
    }

    public static int capacityUnits(WitchState state, EnergyBalance balance) {
        return state.initiated() ? PracticeProgress.capacityUnits(balance, state.practiceSpent()) : 0;
    }

    /** Whether a witch holds at least {@code units} now. */
    public static Result check(WitchState state, int units) {
        if (units < 0) {
            return Result.INVALID;
        }
        if (!state.initiated()) {
            return Result.NOT_WITCH;
        }
        return state.energyUnits() >= units ? Result.OK : Result.INSUFFICIENT;
    }

    /** Spends {@code units}; nothing changes unless the result is {@link Result#OK}. */
    static Result debit(WitchState state, int units) {
        Result check = check(state, units);
        if (check == Result.OK) {
            state.setEnergyUnits(state.energyUnits() - units);
        }
        return check;
    }

    /**
     * Restores up to {@code units}, clamped to capacity; returns how many were actually added (0 for a
     * non-witch, a negative amount or a full witch).
     */
    static int recover(WitchState state, EnergyBalance balance, int units) {
        if (units <= 0 || !state.initiated()) {
            return 0;
        }
        int capacity = capacityUnits(state, balance);
        int room = Math.max(0, capacity - state.energyUnits());
        int added = Math.min(room, units);
        state.setEnergyUnits(state.energyUnits() + added);
        return added;
    }

    /**
     * Records {@code units} of successful qualifying expenditure; returns the number of thresholds
     * newly crossed. Capacity grows with them but the new room is not filled. Practice stops counting
     * at the final threshold.
     */
    static int awardPractice(WitchState state, EnergyBalance balance, int units) {
        if (units <= 0 || !state.initiated()) {
            return 0;
        }
        int before = PracticeProgress.reached(balance, state.practiceSpent());
        long next = Math.min((long) state.practiceSpent() + units, balance.practiceCeilingUnits());
        state.setPracticeSpent((int) Math.max(state.practiceSpent(), next));
        return PracticeProgress.reached(balance, state.practiceSpent()) - before;
    }

    /** Conversion: a witch with this identity and a full initial capacity. */
    static void initiate(WitchState state, EnergyBalance balance, UUID initiationId) {
        state.initiate(initiationId);
        state.setEnergyUnits(capacityUnits(state, balance));
    }

    /** A balance change: energy above the recomputed capacity is clamped; nothing is refilled. */
    static boolean clamp(WitchState state, EnergyBalance balance) {
        int capacity = capacityUnits(state, balance);
        if (state.energyUnits() > capacity) {
            state.setEnergyUnits(capacity);
            return true;
        }
        if (state.practiceSpent() > balance.practiceCeilingUnits()) {
            state.setPracticeSpent(balance.practiceCeilingUnits());
            return true;
        }
        return false;
    }

    /** Whole energy units to state units, refusing overflow. */
    public static int units(int energy) {
        if (energy < 0 || energy > EnergyBalance.MAX_ENERGY) {
            throw new IllegalArgumentException("energy " + energy);
        }
        return energy * EnergyBalance.UNITS;
    }
}
