package io.github.dirtyredz.herbalwitch.platform.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The cells of a horizontal-facing multiblock: cell 0 is the origin (it holds the block entity and
 * the loot); every other cell is offset from it to the viewer's right, upward and toward the back.
 * "Right" is seen by someone facing the block's front, so for a block facing north it is west, and
 * the back is south. Layouts may be sparse (an archway leaves its passage empty) but are always
 * face-connected. Plain geometry over a cell-index property; no world access.
 *
 * <p>The tools' Blockbench compiler cuts authored geometry into the same cells in the same order
 * (tools/blockbench/geometry/cells.mjs): x - 16 per step right, y + 16 up, z + 16 back.
 */
public final class MultiblockLayout {
    /** One cell, offset from the origin: steps to the viewer's right, upward and toward the back. */
    public record Cell(int right, int up, int depth) {
        public Cell(int right, int up) {
            this(right, up, 0);
        }
    }

    private final IntegerProperty part;
    private final List<Cell> cells;

    private MultiblockLayout(IntegerProperty part, List<Cell> cells) {
        List<String> problems = problems(cells);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", problems));
        }
        if (part.getPossibleValues().size() != cells.size()) {
            throw new IllegalArgumentException("part property must have one value per cell");
        }
        this.part = part;
        this.cells = List.copyOf(cells);
    }

    /** A {@code width} x {@code height} rectangle, cells numbered row by row from the bottom. */
    public static MultiblockLayout rectangle(int width, int height) {
        return box(width, 1, height);
    }

    /**
     * A {@code width} x {@code depth} x {@code height} box in canonical order: by height, then depth,
     * then to the right (a one-deep box numbers exactly like {@link #rectangle}).
     */
    public static MultiblockLayout box(int width, int depth, int height) {
        return of("part", boxCells(width, depth, height));
    }

    /** The cells of a box, in canonical order. */
    public static List<Cell> boxCells(int width, int depth, int height) {
        List<Cell> cells = new ArrayList<>();
        for (int up = 0; up < height; up++) {
            for (int back = 0; back < depth; back++) {
                for (int right = 0; right < width; right++) {
                    cells.add(new Cell(right, up, back));
                }
            }
        }
        return cells;
    }

    /** An explicit, possibly sparse, layout over a property named {@code property}. */
    public static MultiblockLayout of(String property, List<Cell> cells) {
        return new MultiblockLayout(IntegerProperty.create(property, 0, Math.max(1, cells.size() - 1)), cells);
    }

    /**
     * Why a cell list is not a layout (empty when it is): fewer than two cells, origin not first,
     * duplicate cells, or cells that are not face-connected.
     */
    public static List<String> problems(List<Cell> cells) {
        List<String> out = new ArrayList<>();
        if (cells.size() < 2) {
            out.add("a multiblock needs at least two cells");
            return out;
        }
        if (!cells.get(0).equals(new Cell(0, 0, 0))) {
            out.add("cell 0 must be the origin");
        }
        Set<Cell> all = new HashSet<>(cells);
        if (all.size() != cells.size()) {
            out.add("a cell appears twice");
        }
        Set<Cell> reached = new HashSet<>();
        Deque<Cell> queue = new ArrayDeque<>();
        queue.add(cells.get(0));
        reached.add(cells.get(0));
        while (!queue.isEmpty()) {
            Cell c = queue.poll();
            for (int[] d : new int[][] {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
                Cell n = new Cell(c.right() + d[0], c.up() + d[1], c.depth() + d[2]);
                if (all.contains(n) && reached.add(n)) {
                    queue.add(n);
                }
            }
        }
        if (reached.size() != all.size()) {
            out.add("cells are not face-connected");
        }
        return out;
    }

    public IntegerProperty part() {
        return part;
    }

    public int size() {
        return cells.size();
    }

    public Cell cell(int index) {
        return cells.get(index);
    }

    public List<Cell> cells() {
        return cells;
    }

    /** The index of a cell, or -1. */
    public int indexOf(Cell cell) {
        return cells.indexOf(cell);
    }

    /** The viewer's right for a block whose front faces {@code facing}. */
    public static Direction right(Direction facing) {
        return facing.getCounterClockWise();
    }

    /** The back for a block whose front faces {@code facing}. */
    public static Direction back(Direction facing) {
        return facing.getOpposite();
    }

    /** Where cell {@code index} lies relative to the origin, in world axes, for a structure facing {@code facing}. */
    public BlockPos offset(Direction facing, int index) {
        Cell c = cells.get(index);
        return BlockPos.ZERO.relative(right(facing), c.right()).above(c.up()).relative(back(facing), c.depth());
    }

    public BlockPos cellPos(BlockPos origin, Direction facing, int index) {
        return origin.offset(offset(facing, index));
    }

    /** The origin of the structure a cell belongs to. */
    public BlockPos origin(BlockPos pos, Direction facing, int index) {
        return pos.subtract(offset(facing, index));
    }

    public boolean isOrigin(BlockState state) {
        return state.getValue(part) == 0;
    }

    /** Whether a cell stands on the ground (bottom row). */
    public boolean grounded(int index) {
        return cells.get(index).up() == 0;
    }

    /** Whether nothing of the structure lies above the cell. */
    public boolean top(int index) {
        Cell c = cells.get(index);
        return indexOf(new Cell(c.right(), c.up() + 1, c.depth())) < 0;
    }

    /** Whether nothing of the structure lies behind the cell. */
    public boolean rear(int index) {
        Cell c = cells.get(index);
        return indexOf(new Cell(c.right(), c.up(), c.depth() + 1)) < 0;
    }

    /** The structure's own cells next to {@code index}: direction -> the part expected there. */
    public Map<Direction, Integer> neighbours(Direction facing, int index) {
        Map<Direction, Integer> out = new EnumMap<>(Direction.class);
        Cell c = cells.get(index);
        for (int i = 0; i < cells.size(); i++) {
            Cell o = cells.get(i);
            int dr = o.right() - c.right();
            int du = o.up() - c.up();
            int dd = o.depth() - c.depth();
            if (Math.abs(dr) + Math.abs(du) + Math.abs(dd) != 1) {
                continue;
            }
            Direction d;
            if (du != 0) {
                d = du > 0 ? Direction.UP : Direction.DOWN;
            } else if (dr != 0) {
                d = dr > 0 ? right(facing) : right(facing).getOpposite();
            } else {
                d = dd > 0 ? back(facing) : back(facing).getOpposite();
            }
            out.put(d, i);
        }
        return out;
    }
}
