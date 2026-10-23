package io.github.dirtyredz.herbalwitch.decor.showcase;

import java.util.ArrayList;
import java.util.List;

/**
 * Footprint-aware packing of the decor samples into rows (pure arithmetic, no registries). Each
 * sample takes a slot: the piece's footprint, an extra column or row for the wall it hangs on, and
 * the sign column to its west (the sign stands beside the piece, never in front of it). Slots are
 * separated by a two-block aisle, which holds the next sign, and rows by one open block, so every
 * seat, bed and cabinet can be reached. Rows fill left to right up to {@link #WIDTH}, then start
 * behind the deepest slot of the row.
 */
public final class DecorShowcaseLayout {
    /** Section width in blocks, the sign column included. */
    public static final int WIDTH = 41;
    /** The column west of a slot that holds its sign. */
    public static final int SIGN_COLUMN = 1;
    /** Blocks between one slot and the next sign column: an open block, then the sign. */
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

    /** Where a sample landed: its slot's minimum corner (x, z) in section coordinates; its sign is at x - 1. */
    public record Slot(int x, int z) {
    }

    private DecorShowcaseLayout() {
    }

    public static List<Slot> pack(List<Footprint> footprints) {
        List<Slot> slots = new ArrayList<>();
        int x = SIGN_COLUMN;
        int z = 0;
        int rowDepth = 0;
        for (Footprint f : footprints) {
            if (SIGN_COLUMN + f.columns() > WIDTH) {
                throw new IllegalArgumentException("a sample " + f.columns() + " wide does not fit the " + WIDTH + "-wide decor section");
            }
            if (x > SIGN_COLUMN && x + f.columns() > WIDTH) {
                z += rowDepth + ROW_AISLE;
                x = SIGN_COLUMN;
                rowDepth = 0;
            }
            slots.add(new Slot(x, z));
            x += f.columns() + AISLE;
            rowDepth = Math.max(rowDepth, f.rows());
        }
        return slots;
    }
}
