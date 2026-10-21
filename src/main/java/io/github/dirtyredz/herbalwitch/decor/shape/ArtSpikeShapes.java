package io.github.dirtyredz.herbalwitch.decor.shape;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * Collision and selection geometry of the art-spike pieces, simplified from their template models
 * (models/block/decor/templates/). Authored facing north: the front is at z = 0, the back or the
 * wall at z = 16. The rug uses vanilla carpet's own shape.
 */
public final class ArtSpikeShapes {
    /** Legs and seat block, raised back, two arms. */
    public static final Map<Direction, VoxelShape> ARMCHAIR = ShapeRotation.horizontal(
            new double[] {1, 0, 1, 15, 8, 15},
            new double[] {1, 8, 12, 15, 16, 15},
            new double[] {1, 8, 1, 3, 12, 12},
            new double[] {13, 8, 1, 15, 12, 12});

    /** Body plus a full-width top, so the top face is sturdy enough to stand a lamp on. */
    public static final Map<Direction, VoxelShape> APOTHECARY_COUNTER = ShapeRotation.horizontal(
            new double[] {1, 0, 1, 15, 14, 15},
            new double[] {0, 14, 0, 16, 16, 16});

    /** The board, its brackets and the bottles standing on it. */
    public static final Map<Direction, VoxelShape> SMALL_WALL_SHELF = ShapeRotation.horizontal(
            new double[] {0, 7, 9, 16, 9, 16},
            new double[] {2, 3, 11, 3, 7, 16},
            new double[] {13, 3, 11, 14, 7, 16},
            new double[] {3, 9, 11, 14, 14, 14});

    /** Base, stem and shade; symmetric, so one shape serves every state. */
    public static final VoxelShape TABLE_LAMP = ShapeRotation.shape(
            new double[] {5, 0, 5, 11, 2, 11},
            new double[] {7, 2, 7, 9, 8, 9},
            new double[] {4, 8, 4, 12, 15, 12});

    private ArtSpikeShapes() {
    }
}
