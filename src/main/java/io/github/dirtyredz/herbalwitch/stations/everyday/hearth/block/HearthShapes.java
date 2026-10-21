package io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * The fireplace's cells, facing north: a solid firebox and mantel below (cells 0 and 1), the chimney
 * breast above (cells 2 and 3), set back from the mantel, whose full top carries a vessel.
 */
public final class HearthShapes {
    private static final double[] LOWER = {0, 0, 0, 16, 16, 16};
    public static final List<Map<Direction, VoxelShape>> CELLS = List.of(
            ShapeRotation.horizontal(LOWER),
            ShapeRotation.horizontal(LOWER),
            ShapeRotation.horizontal(new double[] {0, 0, 4, 16, 16, 16}),
            ShapeRotation.horizontal(new double[] {0, 0, 4, 16, 16, 16}));

    private HearthShapes() {
    }
}
