package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.wand_bench;

import io.github.dirtyredz.herbalwitch.platform.geometry.CellShapes;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * Collision and selection, authored once in the north-facing assembly pixels of the first-draft
 * Blockbench project (src/art/blockbench/stations/wand_bench) and cut per cell and facing.
 */
public final class WandBenchShapes {
    /** {minX, minY, minZ, maxX, maxY, maxZ} in assembly pixels. */
    public static final double[][] BOXES = {
            {-16, 10, 1, 16, 11.5, 13},
            {-15.5, 0, 1.5, -13, 10, 12.5},
            {13, 0, 1.5, 15.5, 10, 12.5},
            {-13, 3, 4, 13, 4.3, 9},
            {-15.5, 11.5, 13, 15.5, 15.9, 14}};
    public static final List<Map<Direction, VoxelShape>> CELLS = CellShapes.cells(BOXES, StationCatalog.WAND_BENCH.layout().cells());

    private WandBenchShapes() {
    }
}
