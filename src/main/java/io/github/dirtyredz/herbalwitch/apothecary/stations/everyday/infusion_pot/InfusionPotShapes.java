package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** Collision and selection shape, simplified from the authored model, facing north. */
public final class InfusionPotShapes {
    public static final Map<Direction, VoxelShape> BODY = ShapeRotation.horizontal(
            new double[] {3, 0, 3, 13, 12, 13});

    private InfusionPotShapes() {
    }
}
