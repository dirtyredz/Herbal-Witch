package io.github.dirtyredz.herbalwitch.decor.showcase;

import java.util.ArrayList;
import java.util.List;

/**
 * Footprint-aware packing of the decor samples into rows (pure arithmetic, no registries). Each
 * sample takes a slot: its sign row in front, the piece's footprint behind it, and an extra column
 * or row for the wall it hangs on; slots are separated by one-block aisles so every seat, bed and
 * cabinet can be reached. Rows fill left to right up to {@link #WIDTH}, then start behind the
 * deepest slot of the row.
 */
public final class DecorShowcaseLayout {
    /** Section width in blocks. */
    public static final int WIDTH = 40;
    public static final int AISLE = 1;

    /** What a sample needs: footprint (width x depth in cells) and the support columns or rows around it. */
    public record Footprint(int width, int depth, boolean wallBehind, boolean wallLeft) {
        /** Columns the slot spans (the piece, plus the left wall of a corner piece). */
        public int columns() {
            return width + (wallLeft ? 1 : 0);
        }

        /** Rows the slot spans: the sign, the piece, and the wall behind it. */
        public int rows() {
            return 1 + depth + (wallBehind ? 1 : 0);
        }
    }

    /** Where a sample landed: its slot's minimum corner (x, z) in section coordinates. */
    public record Slot(int x, int z) {
    }

    private DecorShowcaseLayout() {
    }

    public static List<Slot> pack(List<Footprint> footprints) {
        List<Slot> slots = new ArrayList<>();
        int x = 0;
        int z = 0;
        int rowDepth = 0;
        for (Footprint f : footprints) {
            if (f.columns() > WIDTH) {
                throw new IllegalArgumentException("a sample " + f.columns() + " wide does not fit the " + WIDTH + "-wide decor section");
            }
            if (x > 0 && x + f.columns() > WIDTH) {
                z += rowDepth + AISLE;
                x = 0;
                rowDepth = 0;
            }
            slots.add(new Slot(x, z));
            x += f.columns() + AISLE;
            rowDepth = Math.max(rowDepth, f.rows());
        }
        return slots;
    }
}
