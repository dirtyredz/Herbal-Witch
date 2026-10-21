package io.github.dirtyredz.herbalwitch.platform.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The cells of a horizontal-facing multiblock: cell 0 is the origin (it holds the block entity and
 * the loot); every other cell is offset from it to the viewer's right and upward. "Right" is seen by
 * someone facing the block's front, so for a block facing north it is west. Plain geometry over a
 * {@code part} property; no world access.
 */
public final class MultiblockLayout {
    /** One cell, offset from the origin. */
    public record Cell(int right, int up) {
    }

    private final IntegerProperty part;
    private final List<Cell> cells;

    private MultiblockLayout(IntegerProperty part, List<Cell> cells) {
        if (cells.isEmpty() || !cells.get(0).equals(new Cell(0, 0))) {
            throw new IllegalArgumentException("cell 0 must be the origin");
        }
        if (part.getPossibleValues().size() != cells.size()) {
            throw new IllegalArgumentException("part property must have one value per cell");
        }
        this.part = part;
        this.cells = List.copyOf(cells);
    }

    /** A {@code width} x {@code height} rectangle, cells numbered row by row from the bottom. */
    public static MultiblockLayout rectangle(int width, int height) {
        Cell[] cells = new Cell[width * height];
        for (int up = 0; up < height; up++) {
            for (int right = 0; right < width; right++) {
                cells[up * width + right] = new Cell(right, up);
            }
        }
        return new MultiblockLayout(IntegerProperty.create("part", 0, cells.length - 1), List.of(cells));
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

    /** The viewer's right for a block whose front faces {@code facing}. */
    public static Direction right(Direction facing) {
        return facing.getCounterClockWise();
    }

    public BlockPos cellPos(BlockPos origin, Direction facing, int index) {
        Cell c = cells.get(index);
        return origin.relative(right(facing), c.right()).above(c.up());
    }

    /** The origin of the structure a cell belongs to. */
    public BlockPos origin(BlockPos pos, Direction facing, int index) {
        Cell c = cells.get(index);
        return pos.relative(right(facing), -c.right()).below(c.up());
    }

    public boolean isOrigin(BlockState state) {
        return state.getValue(part) == 0;
    }

    /** Whether a cell stands on the ground (row 0). */
    public boolean grounded(int index) {
        return cells.get(index).up() == 0;
    }

    /** The structure's own cells next to {@code index}: direction -> the part expected there. */
    public Map<Direction, Integer> neighbours(Direction facing, int index) {
        Map<Direction, Integer> out = new EnumMap<>(Direction.class);
        Cell c = cells.get(index);
        Direction right = right(facing);
        for (int i = 0; i < cells.size(); i++) {
            Cell o = cells.get(i);
            int dr = o.right() - c.right();
            int du = o.up() - c.up();
            if (Math.abs(dr) + Math.abs(du) != 1) {
                continue;
            }
            Direction d = du == 1 ? Direction.UP : du == -1 ? Direction.DOWN : dr == 1 ? right : right.getOpposite();
            out.put(d, i);
        }
        return out;
    }
}
