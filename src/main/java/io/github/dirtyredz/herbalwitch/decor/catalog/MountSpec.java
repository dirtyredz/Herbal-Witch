package io.github.dirtyredz.herbalwitch.decor.catalog;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;

import java.util.List;

/**
 * Where a piece stands and which cells it occupies: the support it needs and its cells, as
 * (right, up, depth) steps from the origin in canonical order (by height, then depth, then right;
 * {@link MultiblockLayout#boxCells}). A one-cell piece has the single origin cell.
 *
 * @param support what the piece needs to stand
 * @param cells   occupied cells, origin first
 */
public record MountSpec(Support support, List<Cell> cells) {
    /** What holds a piece up. Checked at placement and whenever a supporting neighbour changes. */
    public enum Support {
        /** Nothing: the piece may stand anywhere (the armchair and apothecary counter since 0.1.0, beds). */
        FREE,
        /** Every bottom cell stands on a block whose top can hold the centre of a post. */
        FLOOR,
        /** Every back cell is backed by a sturdy wall face; the piece faces away from it. */
        WALL,
        /** Every top cell hangs from a block whose underside can hold the centre of a chain. */
        CEILING,
        /** Backed by a sturdy wall behind and a second sturdy wall on the viewer's left. */
        CORNER,
        /** Carpet rule: anything but air below every bottom cell. */
        CARPET
    }

    public MountSpec {
        cells = List.copyOf(cells);
        if (cells.isEmpty() || !cells.get(0).equals(new Cell(0, 0, 0))) {
            throw new IllegalArgumentException("cell 0 must be the origin");
        }
        if (cells.size() > 1 && !MultiblockLayout.problems(cells).isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", MultiblockLayout.problems(cells)));
        }
    }

    /** A width x depth x height box of cells. */
    public static MountSpec box(Support support, int width, int depth, int height) {
        return new MountSpec(support, width * depth * height == 1 ? List.of(new Cell(0, 0, 0))
                : MultiblockLayout.boxCells(width, depth, height));
    }

    public boolean multiblock() {
        return cells.size() > 1;
    }

    /** Footprint extents in cells: {width, depth, height}. */
    public int[] size() {
        int w = 0;
        int d = 0;
        int h = 0;
        for (Cell c : cells) {
            w = Math.max(w, c.right() + 1);
            d = Math.max(d, c.depth() + 1);
            h = Math.max(h, c.up() + 1);
        }
        return new int[] {w, d, h};
    }
}
