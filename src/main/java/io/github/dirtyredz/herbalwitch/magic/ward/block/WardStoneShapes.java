package io.github.dirtyredz.herbalwitch.magic.ward.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** The ward stone's collision and selection, from the first-draft model's main boxes (src/art/blockbench/ward/ward_stone), facing north. */
public final class WardStoneShapes {
    public static final Map<Direction, VoxelShape> SHAPES = ShapeRotation.horizontal(
            new double[] {3, 0, 3, 13, 1.5, 13},
            new double[] {3.8, 1.5, 3.8, 12.2, 3, 12.2},
            new double[] {4.5, 3, 4.5, 11.5, 8, 11.5},
            new double[] {5.2, 8, 5.2, 10.8, 13.2, 10.8});

    private WardStoneShapes() {
    }
}
