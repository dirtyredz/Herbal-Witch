package io.github.dirtyredz.herbalwitch.platform.geometry;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Collision and selection geometry of a multi-cell structure authored whole: boxes in the
 * structure's north-facing assembly coordinates (model pixels: cell 0 at x 0..16, a step to the
 * viewer's right at x - 16, up at y + 16, back at z + 16, as the Blockbench projects are drawn), cut
 * into each cell and turned to each horizontal facing. A cell no box reaches is empty, which
 * collision and selection both honour. Shared by furniture and the altar.
 */
public final class CellShapes {
    private CellShapes() {
    }

    /** One map per cell (in the given order): facing -> the cell's shape in its own block. */
    public static List<Map<Direction, VoxelShape>> cells(double[][] boxes, List<Cell> cells) {
        List<Map<Direction, VoxelShape>> out = new ArrayList<>();
        for (Cell cell : cells) {
            List<double[]> local = cut(boxes, cell);
            Map<Direction, VoxelShape> byFacing = new EnumMap<>(Direction.class);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                VoxelShape shape = Shapes.empty();
                for (double[] b : local) {
                    shape = Shapes.or(shape, ShapeRotation.shape(ShapeRotation.rotate(b, ShapeRotation.quarterTurns(facing))));
                }
                byFacing.put(facing, shape.optimize());
            }
            out.add(byFacing);
        }
        return out;
    }

    /** The parts of assembly boxes inside one cell, in that cell's own pixels (0..16). */
    public static List<double[]> cut(double[][] boxes, Cell cell) {
        double[] lo = {-16.0 * cell.right(), 16.0 * cell.up(), 16.0 * cell.depth()};
        List<double[]> out = new ArrayList<>();
        for (double[] b : boxes) {
            double[] c = new double[6];
            boolean empty = false;
            for (int i = 0; i < 3; i++) {
                c[i] = Math.max(b[i], lo[i]) - lo[i];
                c[i + 3] = Math.min(b[i + 3], lo[i] + 16) - lo[i];
                empty |= c[i + 3] - c[i] <= 1e-6;
            }
            if (!empty) {
                out.add(c);
            }
        }
        return out;
    }
}
