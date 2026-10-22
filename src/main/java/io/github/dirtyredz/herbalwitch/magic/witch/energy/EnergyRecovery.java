package io.github.dirtyredz.herbalwitch.magic.witch.energy;

/**
 * The pure arithmetic of recovery: the environmental bonus (fixed-point percent, capped), passive
 * rest's stillness tracking, and meditation's settle-then-interval schedule. Rest and meditation are
 * mutually exclusive (a meditating player's rest tracker is held reset). No world access, so every
 * rule is unit-tested; {@link RecoveryConditions} reads the world and {@link EnergyEvents} drives it.
 */
public final class EnergyRecovery {
    /** How far a resting player may drift from where the rest began, in blocks. */
    public static final double REST_RADIUS = 0.1;

    private EnergyRecovery() {
    }

    /** The bonus percent: nature and a visible full moon, combined, never above the cap. */
    public static int bonusPercent(EnergyBalance balance, boolean nature, boolean fullMoonSky) {
        int pct = (nature ? balance.natureBonus() : 0) + (fullMoonSky ? balance.moonBonus() : 0);
        return Math.min(pct, balance.bonusCap());
    }

    /** {@code units} raised by {@code percent}, rounded down (fixed-point). */
    public static int withBonus(int units, int percent) {
        return (int) ((long) units * (100 + Math.max(0, percent)) / 100);
    }

    /** One player's passive-rest progress (transient, never saved). */
    public static final class Rest {
        private double x;
        private double y;
        private double z;
        private boolean anchored;
        private int ticks;

        /**
         * Advances one server tick at position (x, y, z). {@code disturbed} is any activity that breaks
         * rest (sprinting, swimming, flying, riding, being hurt, using an item, spending energy).
         * Returns true when a full interval of undisturbed rest just completed (the counter restarts).
         */
        public boolean tick(double px, double py, double pz, boolean disturbed, int interval) {
            if (disturbed || !anchored || moved(px, py, pz)) {
                anchor(px, py, pz);
                return false;
            }
            ticks++;
            if (ticks >= interval) {
                ticks = 0;
                return true;
            }
            return false;
        }

        private boolean moved(double px, double py, double pz) {
            double dx = px - x;
            double dy = py - y;
            double dz = pz - z;
            return dx * dx + dy * dy + dz * dz > REST_RADIUS * REST_RADIUS;
        }

        private void anchor(double px, double py, double pz) {
            x = px;
            y = py;
            z = pz;
            anchored = true;
            ticks = 0;
        }

        /** Forget the anchor (death, dimension change, meditation). */
        public void reset() {
            anchored = false;
            ticks = 0;
        }

        public int ticks() {
            return ticks;
        }

        public boolean resting() {
            return anchored && ticks > 0;
        }
    }

    /**
     * Whether meditation grants on this tick: after {@code settle} ticks, then every {@code interval}.
     *
     * @param elapsed ticks since the meditation began (1 on its first tick)
     */
    public static boolean meditationGrant(int elapsed, int settle, int interval) {
        return elapsed > settle && (elapsed - settle) % interval == 0;
    }
}
