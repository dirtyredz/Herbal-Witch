package io.github.dirtyredz.herbalwitch.magic.ritual.circle;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.platform.geometry.CellShapes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * The circle's collision and selection, authored once in its north-facing assembly pixels (from the
 * first-draft Blockbench project, src/art/blockbench/ritual/circle) and cut per cell and facing: a
 * low 2-pixel relief over every cell, the four rune stones and the four corner candle plinths.
 */
public final class RitualCircleShapes {
    /** {minX, minY, minZ, maxX, maxY, maxZ} in assembly pixels (x -64..16, z 0..80). */
    public static final double[][] BOXES = {
            {-64, 0, 0, 16, 2, 16},          // front row
            {-64, 0, 64, 16, 2, 80},         // back row
            {-64, 0, 16, -48, 2, 64},        // right column
            {0, 0, 16, 16, 2, 64},           // left column
            {8.5, 0, 36.5, 15.5, 6.4, 43.5}, // rune stones
            {-63.5, 0, 36.5, -56.5, 6.4, 43.5},
            {-27.5, 0, 0.5, -20.5, 6.4, 7.5},
            {-27.5, 0, 72.5, -20.5, 6.4, 79.5},
            {4.35, 0, 68.35, 10.65, 3, 74.65}, // candle plinths
            {4.35, 0, 5.35, 10.65, 3, 11.65},
            {-58.65, 0, 68.35, -52.35, 3, 74.65},
            {-58.65, 0, 5.35, -52.35, 3, 11.65},
    };
    private static final List<Map<Direction, VoxelShape>> CELLS = CellShapes.cells(BOXES, CircleCatalog.LAYOUT.cells());

    private RitualCircleShapes() {
    }

    public static VoxelShape shape(int cell, Direction facing) {
        return CELLS.get(cell).get(facing);
    }
}
