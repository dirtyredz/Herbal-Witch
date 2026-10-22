package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** Collision and selection shape, simplified from the authored model, facing north. */
public final class KettleShapes {
    public static final Map<Direction, VoxelShape> BODY = ShapeRotation.horizontal(
            new double[] {4, 0, 4, 12, 10, 12});

    private KettleShapes() {
    }
}
