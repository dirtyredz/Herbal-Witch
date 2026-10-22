package io.github.dirtyredz.herbalwitch.stations.advanced.bottling_bench.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.CellShapes;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * Collision and selection, authored once in the north-facing assembly pixels of the first-draft
 * Blockbench project (src/art/blockbench/stations/bottling_bench) and cut per cell and facing.
 */
public final class BottlingBenchShapes {
    /** {minX, minY, minZ, maxX, maxY, maxZ} in assembly pixels. */
    public static final double[][] BOXES = {
            {-16, 10.5, 1, 16, 12, 13},
            {-15.5, 0, 1.5, -12.5, 10.5, 12.5},
            {12.5, 0, 1.5, 15.5, 10.5, 12.5},
            {-12.5, 3, 3.5, 12.5, 4, 11},
            {-12.5, 8, 1.5, 12.5, 10.5, 12.5}};
    public static final List<Map<Direction, VoxelShape>> CELLS = CellShapes.cells(BOXES, StationCatalog.BOTTLING_BENCH.layout().cells());

    private BottlingBenchShapes() {
    }
}
