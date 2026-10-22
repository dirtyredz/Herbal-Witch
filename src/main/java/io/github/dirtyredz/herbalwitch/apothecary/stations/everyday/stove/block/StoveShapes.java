package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.stove.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/** The stove's two cells, facing north: a full-height iron body whose top carries the vessels. */
public final class StoveShapes {
    private static final double[] BODY = {0, 0, 0, 16, 16, 16};
    public static final List<Map<Direction, VoxelShape>> CELLS = List.of(ShapeRotation.horizontal(BODY), ShapeRotation.horizontal(BODY));

    private StoveShapes() {
    }
}
