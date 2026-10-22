package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The validated energy economy, in whole energy unless named otherwise (state stores hundredths:
 * {@link #UNITS} per energy). Plain Java, built from server configuration and checked as a whole:
 * an invalid set is refused and the previous one stays (see {@code WitchRules}).
 *
 * @param initialCapacity      capacity of a new witch
 * @param thresholds           successfully spent energy at which capacity grows, strictly increasing
 * @param capacityPerThreshold capacity gained at each threshold
 * @param initiationCost       energy the initiating witch pays
 * @param attunementCost       energy an attunement spends
 * @param restInterval         ticks of undisturbed rest per passive grant
 * @param restAmount           energy per passive grant
 * @param meditationSettle     ticks of settling before meditation grants anything
 * @param meditationInterval   ticks per meditation grant
 * @param meditationAmount     energy per meditation grant
 * @param sleepAmount          energy for a successful night's sleep (once per day)
 * @param teaAmount            energy from a cup of energy tea
 * @param teaCooldown          ticks before the next cup works
 * @param restfulBonus         extra energy a prepared sleep grants
 * @param restfulDuration      online ticks a restful preparation lasts
 * @param natureBonus          percent added to rest and meditation in nature biomes
 * @param moonBonus            percent added under a visible full moon at night
 * @param bonusCap             the combined bonus ceiling, percent
 */
public record EnergyBalance(int initialCapacity, List<Integer> thresholds, int capacityPerThreshold, int initiationCost,
                            int attunementCost, int restInterval, int restAmount, int meditationSettle, int meditationInterval,
                            int meditationAmount, int sleepAmount, int teaAmount, int teaCooldown, int restfulBonus, int restfulDuration,
                            int natureBonus, int moonBonus, int bonusCap) {
    /** Hundredths: state, costs and grants are fixed-point in units. */
    public static final int UNITS = 100;
    /** Upper bounds that keep every unit sum inside an int. */
    public static final int MAX_ENERGY = 100_000;
    public static final int MAX_TICKS = 1_000_000;

    /** The milestone defaults (docs/catalogue/witchcraft.md). */
    public static final EnergyBalance DEFAULTS = new EnergyBalance(100, List.of(50, 100, 150, 200, 250, 300, 350, 400, 450, 500), 10,
            50, 10, 100, 1, 40, 20, 2, 30, 25, 1200, 30, 24_000, 10, 15, 25);

    public EnergyBalance {
        thresholds = List.copyOf(thresholds);
    }

    /** Why this balance is unusable; empty when it is valid. */
    public List<String> problems() {
        List<String> out = new ArrayList<>();
        range(out, "initialCapacity", initialCapacity, 1, MAX_ENERGY);
        range(out, "capacityPerThreshold", capacityPerThreshold, 0, MAX_ENERGY);
        range(out, "initiationCost", initiationCost, 0, MAX_ENERGY);
        range(out, "attunementCost", attunementCost, 0, MAX_ENERGY);
        range(out, "restInterval", restInterval, 1, MAX_TICKS);
        range(out, "restAmount", restAmount, 0, MAX_ENERGY);
        range(out, "meditationSettle", meditationSettle, 0, MAX_TICKS);
        range(out, "meditationInterval", meditationInterval, 1, MAX_TICKS);
        range(out, "meditationAmount", meditationAmount, 0, MAX_ENERGY);
        range(out, "sleepAmount", sleepAmount, 0, MAX_ENERGY);
        range(out, "teaAmount", teaAmount, 0, MAX_ENERGY);
        range(out, "teaCooldown", teaCooldown, 0, MAX_TICKS);
        range(out, "restfulBonus", restfulBonus, 0, MAX_ENERGY);
        range(out, "restfulDuration", restfulDuration, 1, MAX_TICKS * 10);
        range(out, "natureBonus", natureBonus, 0, 100);
        range(out, "moonBonus", moonBonus, 0, 100);
        range(out, "bonusCap", bonusCap, 0, 100);
        if (thresholds.isEmpty() || thresholds.size() > 32) {
            out.add("thresholds: between 1 and 32 entries");
        }
        int previous = 0;
        for (int t : thresholds) {
            if (t <= previous || t > MAX_ENERGY) {
                out.add("thresholds must be strictly increasing, positive and at most " + MAX_ENERGY + ": " + thresholds);
                break;
            }
            previous = t;
        }
        if ((long) initialCapacity + (long) capacityPerThreshold * thresholds.size() > MAX_ENERGY) {
            out.add("final capacity exceeds " + MAX_ENERGY);
        }
        return out;
    }

    private static void range(List<String> out, String name, int value, int min, int max) {
        if (value < min || value > max) {
            out.add(name + " " + value + " outside " + min + ".." + max);
        }
    }

    /** The final threshold in units: practice never counts past it. */
    public int practiceCeilingUnits() {
        return thresholds.get(thresholds.size() - 1) * UNITS;
    }

    /** The largest capacity, reached at the final threshold. */
    public int finalCapacity() {
        return initialCapacity + capacityPerThreshold * thresholds.size();
    }

    @Override
    public String toString() {
        return "EnergyBalance" + Arrays.asList(initialCapacity, thresholds, capacityPerThreshold, initiationCost, attunementCost);
    }
}
