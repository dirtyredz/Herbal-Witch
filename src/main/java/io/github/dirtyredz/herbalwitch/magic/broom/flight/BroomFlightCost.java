package io.github.dirtyredz.herbalwitch.magic.broom.flight;

/** Exact hundredths-per-second arithmetic. Caller commits the remainder only after a paid tick. */
public final class BroomFlightCost {
    public record Tick(int units, int remainder) { }

    private BroomFlightCost() { }

    public static Tick next(int unitsPerSecond, int remainder) {
        if (unitsPerSecond < 0 || unitsPerSecond > 2000 || remainder < 0 || remainder >= 20) {
            throw new IllegalArgumentException("Invalid flight cost");
        }
        int total = unitsPerSecond + remainder;
        return new Tick(total / 20, total % 20);
    }
}
