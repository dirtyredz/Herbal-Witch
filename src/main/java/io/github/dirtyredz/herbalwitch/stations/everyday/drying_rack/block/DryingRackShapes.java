package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * The rack's cells, authored facing north (cell 0 is the east half; cell 1 is to the viewer's right,
 * the west half; cells 2 and 3 above them): the A-frame legs and feet at each outer end, the beam
 * across the top.
 */
public final class DryingRackShapes {
    public static final List<Map<Direction, VoxelShape>> CELLS = List.of(
            ShapeRotation.horizontal(new double[] {11, 0, 2, 16, 3, 14}, new double[] {12, 0, 5, 16, 16, 11}),
            ShapeRotation.horizontal(new double[] {0, 0, 2, 5, 3, 14}, new double[] {0, 0, 5, 4, 16, 11}),
            ShapeRotation.horizontal(new double[] {12, 0, 5, 16, 10, 11}, new double[] {0, 7, 6, 16, 12, 10}),
            ShapeRotation.horizontal(new double[] {0, 0, 5, 4, 10, 11}, new double[] {0, 7, 6, 16, 12, 10}));

    private DryingRackShapes() {
    }
}
