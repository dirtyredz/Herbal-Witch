package io.github.dirtyredz.herbalwitch.decor.structure.shape;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.FixturesShapes;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.LightingShapes;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.OpeningsShapes;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.SeatingShapes;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.StorageShapes;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.SurfacesShapes;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.families.TextilesShapes;
import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collision and selection geometry of every piece. Each family authors its pieces' boxes once, in the
 * whole structure's north-facing coordinates (model pixels: cell 0 at x 0..16, a step to the
 * viewer's right at x - 16, up at y + 16, back at z + 16, as the Blockbench projects are drawn);
 * here they are cut into each cell and turned to each facing. A cell no box reaches is empty
 * (open space inside a frame), which collision and selection both honour.
 */
public final class FurnitureShapes {
    private static final Map<String, double[][]> BOXES = new HashMap<>();
    private static final Map<String, List<Map<Direction, VoxelShape>>> CACHE = new HashMap<>();

    static {
        for (Map<String, double[][]> family : List.of(SeatingShapes.BOXES, StorageShapes.BOXES, SurfacesShapes.BOXES, LightingShapes.BOXES,
                TextilesShapes.BOXES, FixturesShapes.BOXES, OpeningsShapes.BOXES)) {
            family.forEach((id, boxes) -> {
                if (BOXES.put(id, boxes) != null) {
                    throw new IllegalStateException("two families shape " + id);
                }
            });
        }
    }

    private FurnitureShapes() {
    }

    /** Whether a piece has authored boxes. */
    public static boolean authored(FurnitureSpec spec) {
        return BOXES.containsKey(spec.id());
    }

    /** The assembly-space boxes of a piece. */
    public static double[][] boxes(FurnitureSpec spec) {
        double[][] boxes = BOXES.get(spec.id());
        if (boxes == null) {
            throw new IllegalArgumentException("no shape authored for " + spec.path());
        }
        return boxes;
    }

    /** One map per cell (layout order): facing -> the cell's shape in its own block. */
    public static synchronized List<Map<Direction, VoxelShape>> cells(FurnitureSpec spec) {
        return CACHE.computeIfAbsent(spec.path(), k -> build(spec));
    }

    private static List<Map<Direction, VoxelShape>> build(FurnitureSpec spec) {
        double[][] boxes = boxes(spec);
        List<Map<Direction, VoxelShape>> out = new ArrayList<>();
        for (Cell cell : spec.mount().cells()) {
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
