package io.github.dirtyredz.herbalwitch.decor.showcase;

import java.util.ArrayList;
import java.util.List;

/**
 * Footprint-aware packing of the decor samples into rows (pure arithmetic, no registries). Each
 * sample takes a slot: the piece's footprint, an extra column or row for the wall it hangs on, and
 * a sign two cells east of that footprint. Slots are separated by a two-block aisle, which holds
 * their sign, and rows by one open block, so every
 * seat, bed and cabinet can be reached. Rows fill left to right up to {@link #WIDTH}, then start
 * behind the deepest slot of the row.
 */
public final class DecorShowcaseLayout {
    /** Packing width for pieces and supports; the final sign extends two cells beyond it. */
    public static final int WIDTH = 41;
    /** First piece column, keeping the existing west entrance margin. */
    public static final int FIRST_COLUMN = 1;
    /** Blocks between footprints: an open block, then the preceding sample's sign. */
    public static final int AISLE = 2;
    /** Open rows between one row of slots and the next. */
    public static final int ROW_AISLE = 1;

    /** What a sample needs: footprint (width x depth in cells) and the support columns or rows around it. */
    public record Footprint(int width, int depth, boolean wallBehind, boolean wallLeft) {
        /** Columns the slot spans (the piece, plus the left wall of a corner piece). */
        public int columns() {
            return width + (wallLeft ? 1 : 0);
        }

        /** Rows the slot spans: the piece and the wall behind it. */
        public int rows() {
            return depth + (wallBehind ? 1 : 0);
        }
    }

    /** Where a sample landed: its slot's minimum corner (x, z) in section coordinates; its sign follows its eastern footprint edge. */
    public record Slot(int x, int z) {
    }

    private DecorShowcaseLayout() {
    }

    public static List<Slot> pack(List<Footprint> footprints) {
        List<Slot> slots = new ArrayList<>();
        int x = FIRST_COLUMN;
        int z = 0;
        int rowDepth = 0;
        for (Footprint f : footprints) {
            if (FIRST_COLUMN + f.columns() > WIDTH) {
                throw new IllegalArgumentException("a sample " + f.columns() + " wide does not fit the " + WIDTH + "-wide decor section");
            }
            if (x > FIRST_COLUMN && x + f.columns() > WIDTH) {
                z += rowDepth + ROW_AISLE;
                x = FIRST_COLUMN;
                rowDepth = 0;
            }
            slots.add(new Slot(x, z));
            x += f.columns() + AISLE;
            rowDepth = Math.max(rowDepth, f.rows());
        }
        return slots;
    }
}
