package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * Collision and selection, authored once in the north-facing assembly pixels of the first-draft
 * Blockbench project (src/art/blockbench/stations/cauldron) and cut per cell and facing.
 */
public final class CauldronShapes {
    /** {minX, minY, minZ, maxX, maxY, maxZ} in assembly pixels. */
    public static final double[][] BOXES = {
            {5.6, 0, 4.4, 10.4, 2.6, 9.6},
            {3.2, 2.6, 2.2, 12.8, 9.2, 11.8},
            {3.6, 9.2, 2.6, 12.4, 10.6, 11.4}};
    public static final List<Map<Direction, VoxelShape>> CELLS = List.of(ShapeRotation.horizontal(BOXES));

    private CauldronShapes() {
    }
}
