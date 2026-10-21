package io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** A full block: vessels stand on its sturdy top. */
public final class HearthShapes {
    public static final Map<Direction, VoxelShape> BODY = ShapeRotation.horizontal(new double[] {0, 0, 0, 16, 16, 16});

    private HearthShapes() {
    }
}
