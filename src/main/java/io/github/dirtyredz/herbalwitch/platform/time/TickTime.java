package io.github.dirtyredz.herbalwitch.platform.time;

/** Tick durations with explicit display rounding: recipe fractions differ from page countdowns. */
public final class TickTime {
    public static final int SECOND = 20;
    public static final int MINUTE = 60 * SECOND;

    private TickTime() { }

    public static int ceilingSeconds(int ticks) { return (ticks + SECOND - 1) / SECOND; }
    public static int ceilingMinutes(int ticks) { return (ticks + MINUTE - 1) / MINUTE; }

    /** Retains fractional recipe durations and omits an integral trailing decimal. */
    public static String fractionalSeconds(int ticks) {
        return String.valueOf(ticks / (double) SECOND).replaceAll("\\.0$", "");
    }
}
