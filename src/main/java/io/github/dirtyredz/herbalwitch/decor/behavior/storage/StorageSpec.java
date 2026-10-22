package io.github.dirtyredz.herbalwitch.decor.behavior.storage;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;

/**
 * How a piece stores: its slot count, the grid the menu lays them out in, and whether it is a
 * display (one item per slot, drawn where it stands, no automation) or a cabinet (normal stacks,
 * hoppers allowed). Cabinets hold 27 or 54, displays 3, 6 or 9. Plain data.
 *
 * @param slots   slot count
 * @param columns menu columns (9 for cabinets, 3 for displays)
 * @param display whether it is a display shelf
 */
public record StorageSpec(int slots, int columns, boolean display) {
    public StorageSpec {
        if (slots <= 0 || slots % columns != 0) {
            throw new IllegalArgumentException(slots + " slots do not fill " + columns + " columns");
        }
    }

    public static StorageSpec of(FurnitureSpec spec) {
        boolean display = spec.behavior() == FurnitureBehavior.DISPLAY;
        return new StorageSpec(spec.slots(), display ? 3 : 9, display);
    }

    public int rows() {
        return slots / columns;
    }

    /** Items one slot holds: one on a display, a full stack in a cabinet. */
    public int slotLimit() {
        return display ? 1 : 64;
    }
}
