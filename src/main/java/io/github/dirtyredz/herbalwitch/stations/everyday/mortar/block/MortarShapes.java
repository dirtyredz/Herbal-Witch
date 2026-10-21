package io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** Collision and selection shape, simplified from the authored model, facing north. */
public final class MortarShapes {
    public static final Map<Direction, VoxelShape> BODY = ShapeRotation.horizontal(
            new double[] {3, 0, 3, 13, 8, 13});

    private MortarShapes() {
    }
}
