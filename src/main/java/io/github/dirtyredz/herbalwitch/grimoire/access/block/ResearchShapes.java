package io.github.dirtyredz.herbalwitch.grimoire.access.block;

import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * Collision and selection geometry of the research blocks, simplified from their Blockbench models
 * (src/art/blockbench/grimoire/). Authored facing north: the reader stands at z = 0.
 */
public final class ResearchShapes {
    /** Foot, pedestal, and the sloped book board with the open book on it. */
    public static final Map<Direction, VoxelShape> STAND = ShapeRotation.horizontal(
            new double[] {3, 0, 3, 13, 2, 13},
            new double[] {6, 2, 6, 10, 10, 10},
            new double[] {2, 10, 2, 14, 15, 14});

    /** The carcass with its cornice and plinth; the open front between the side panels is shelves. */
    public static final Map<Direction, VoxelShape> BOOKSHELF = ShapeRotation.horizontal(
            new double[] {0, 0, 3, 16, 16, 16});

    private ResearchShapes() {
    }
}
