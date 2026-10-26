package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Named positions of the first-preparation work area, shared by structure generation and the animation.
 * Structure coordinates: the floor is layer {@code y = 0} (Ponder's base plate) and the work stands on it.
 *
 * <p>The stations all face {@link #FACING}; multiblock cells are derived from each station's own layout
 * ({@code MultiblockLayout.offset}), never restated here. Seen from the stations' front, the work reads left
 * to right: lavender, drying rack, mortar, then the stove with the kettle on its origin burner.
 */
public final class FirstPreparationLayout {
    /** Structure size: x, y, z. */
    public static final Vec3i SIZE = new Vec3i(11, 4, 5);
    /** The facing of every station in the scene. */
    public static final Direction FACING = Direction.NORTH;
    /** The row the work stands in (z). */
    private static final int ROW = 2;

    /** Mature lavender, on {@link #SOIL}. */
    public static final BlockPos LAVENDER = new BlockPos(9, 1, ROW);
    /** Cultivated soil under the lavender, part of the floor layer. */
    public static final BlockPos SOIL = LAVENDER.below();
    /** Origin cell (part 0) of the four-cell drying rack; the rest extend to the viewer's right and upward. */
    public static final BlockPos RACK_ORIGIN = new BlockPos(7, 1, ROW);
    public static final BlockPos MORTAR = new BlockPos(4, 1, ROW);
    /** Origin burner (part 0) of the two-cell stove. */
    public static final BlockPos STOVE_ORIGIN = new BlockPos(2, 1, ROW);
    /** The kettle stands directly on the stove's origin burner. */
    public static final BlockPos KETTLE = STOVE_ORIGIN.above();

    private FirstPreparationLayout() {
    }

    /** Whether a position lies inside the structure. */
    public static boolean contains(BlockPos pos) {
        return pos.getX() >= 0 && pos.getY() >= 0 && pos.getZ() >= 0
                && pos.getX() < SIZE.getX() && pos.getY() < SIZE.getY() && pos.getZ() < SIZE.getZ();
    }
}
