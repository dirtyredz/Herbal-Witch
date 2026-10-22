package io.github.dirtyredz.herbalwitch.magic.witch.energy;

/**
 * Capacity progression from successful practice: every threshold of successfully spent energy
 * reached adds {@link EnergyBalance#capacityPerThreshold()}. Capacity is always derived from the
 * saved practice and the active balance, never stored, so a configuration change recomputes it.
 * Plain arithmetic in units (hundredths).
 */
public final class PracticeProgress {
    private PracticeProgress() {
    }

    /** How many thresholds {@code practiceUnits} of spent energy has reached. */
    public static int reached(EnergyBalance balance, int practiceUnits) {
        int n = 0;
        for (int t : balance.thresholds()) {
            if ((long) t * EnergyBalance.UNITS <= practiceUnits) {
                n++;
            }
        }
        return n;
    }

    /** The capacity in whole energy for this much practice. */
    public static int capacity(EnergyBalance balance, int practiceUnits) {
        return balance.initialCapacity() + balance.capacityPerThreshold() * reached(balance, practiceUnits);
    }

    public static int capacityUnits(EnergyBalance balance, int practiceUnits) {
        return capacity(balance, practiceUnits) * EnergyBalance.UNITS;
    }

    /** Whether every threshold is reached (practice can no longer raise capacity). */
    public static boolean complete(EnergyBalance balance, int practiceUnits) {
        return reached(balance, practiceUnits) == balance.thresholds().size();
    }

    /** The next threshold in whole energy, or -1 when every one is reached. */
    public static int next(EnergyBalance balance, int practiceUnits) {
        for (int t : balance.thresholds()) {
            if ((long) t * EnergyBalance.UNITS > practiceUnits) {
                return t;
            }
        }
        return -1;
    }
}
