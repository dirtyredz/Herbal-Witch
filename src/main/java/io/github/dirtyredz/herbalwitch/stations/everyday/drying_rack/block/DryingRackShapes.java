package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** The rack's frame: two legs and the top bar, authored facing north. */
public final class DryingRackShapes {
    public static final Map<Direction, VoxelShape> FRAME = ShapeRotation.horizontal(
            new double[] {1, 0, 6, 3, 16, 10},
            new double[] {13, 0, 6, 15, 16, 10},
            new double[] {1, 13, 6, 15, 16, 10},
            new double[] {1, 4, 6, 15, 6, 10});

    private DryingRackShapes() {
    }
}
