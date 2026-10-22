package io.github.dirtyredz.herbalwitch.magic.ritual.altar;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.platform.geometry.CellShapes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * The altar's collision and selection boxes, authored once in the whole structure's north-facing
 * assembly pixels (derived from the first-draft Blockbench project, src/art/blockbench/ritual/altar)
 * and cut per cell and facing by {@link CellShapes}: the working table across the front, its legs and
 * back shelf, the two shrine posts, the rune panel and the gabled header.
 */
public final class AltarShapes {
    /** {minX, minY, minZ, maxX, maxY, maxZ} in assembly pixels. */
    public static final double[][] BOXES = {
            {-16, 13, 1, 16, 14.5, 12},       // table top
            {-15, 0, 1.5, -12.5, 13, 4.5},    // front leg, right
            {12.5, 0, 1.5, 15, 13, 4.5},      // front leg, left
            {-11.5, 3, 3.5, 11.5, 13, 13.5},  // shelf and back plate
            {-16, 0, 9.5, -11, 28, 14.5},     // shrine post, right
            {11, 0, 9.5, 16, 28, 14.5},       // shrine post, left
            {-11.5, 14.5, 10.5, 11.5, 27, 14}, // ledge and rune panel
            {-15.5, 27, 10.5, 15.5, 29.5, 14.5}, // header
            {-12, 29.5, 11, 12, 31.5, 14},    // gable
    };
    private static final List<Map<Direction, VoxelShape>> CELLS = CellShapes.cells(BOXES, AltarCatalog.LAYOUT.cells());

    private AltarShapes() {
    }

    public static VoxelShape shape(int cell, Direction facing) {
        return CELLS.get(cell).get(facing);
    }
}
