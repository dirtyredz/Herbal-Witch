package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;

/** Synchronous durable-state transaction. World and inventory rollback belongs to the participant. */
final class WitchSpend {
    private WitchSpend() { }

    static boolean run(WitchState state, boolean writable, EnergyBalance balance, int units,
                       WitchStatusService.Change change, Runnable debited, Runnable restored, Runnable committed) {
        if (!writable || EnergyService.check(state, units) != EnergyService.Result.OK) { return false; }
        WitchState before = state.copy();
        RuntimeException failure = null;
        try {
            if (change.prepare() && EnergyService.debit(state, units) == EnergyService.Result.OK) {
                if (units > 0) { debited.run(); }
                if (change.apply()) {
                    if (change.awardsPractice()) { EnergyService.awardPractice(state, balance, units); }
                    change.afterPractice();
                    committed.run();
                    return true;
                }
            }
        } catch (RuntimeException thrown) {
            failure = thrown;
        }
        try {
            change.rollback();
        } catch (RuntimeException rollbackFailure) {
            if (failure == null) { failure = rollbackFailure; }
            else if (failure != rollbackFailure) { failure.addSuppressed(rollbackFailure); }
        } finally {
            // Even a throwing participant rollback cannot prevent durable restoration. Transient
            // disturbance is deliberately retained, as it was in the individual spend callers.
            state.restoreFrom(before);
        }
        try {
            restored.run();
        } catch (RuntimeException notificationFailure) {
            if (failure == null) { failure = notificationFailure; }
            else if (failure != notificationFailure) { failure.addSuppressed(notificationFailure); }
        }
        if (failure != null) { throw failure; }
        return false;
    }
}
