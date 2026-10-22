package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.CellShapes;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * Collision and selection, authored once in the north-facing assembly pixels of the first-draft
 * Blockbench project (src/art/blockbench/stations/still) and cut per cell and facing.
 */
public final class StillShapes {
    /** {minX, minY, minZ, maxX, maxY, maxZ} in assembly pixels. */
    public static final double[][] BOXES = {
            {3, 0, 2, 13, 12, 12},
            {4.8, 12, 5.3, 8.2, 16.8, 8.7},
            {3.1, 16.8, 3.6, 9.9, 26, 10.4},
            {10.8, 17, 4.8, 15.2, 26.6, 9.2},
            {11.4, 10.6, 5.2, 15.2, 16.2, 8.8}};
    public static final List<Map<Direction, VoxelShape>> CELLS = CellShapes.cells(BOXES, StationCatalog.STILL.layout().cells());

    private StillShapes() {
    }
}
