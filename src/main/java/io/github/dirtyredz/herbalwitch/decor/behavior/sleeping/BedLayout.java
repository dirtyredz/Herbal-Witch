package io.github.dirtyredz.herbalwitch.decor.behavior.sleeping;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * Which cells of a bed are the vanilla sleeping pair and which are frame. The foot is the origin
 * (cell 0, at the front, where the player placed it) and the head the cell straight behind it; every
 * other cell (a canopy's posts, rails and drapes) is auxiliary: it routes use and removal to the pair
 * and is never a place to sleep. A bed's vanilla {@code facing} points from foot to head, so the
 * layout's front is the opposite way. Pure: unit-tested without a world.
 */
public final class BedLayout {
    private BedLayout() {
    }

    /** The head cell: straight behind the foot. */
    public static int head(MultiblockLayout layout) {
        int head = layout.indexOf(new Cell(0, 0, 1));
        if (head < 0) {
            throw new IllegalArgumentException("a bed needs a head cell behind its foot");
        }
        return head;
    }

    /** Whether a cell is the foot or the head. */
    public static boolean sleepingPair(MultiblockLayout layout, int cell) {
        return cell == 0 || cell == head(layout);
    }

    /** The vanilla bed part a cell carries (auxiliary cells count as foot; they are never beds). */
    public static BedPart part(MultiblockLayout layout, int cell) {
        return cell == head(layout) ? BedPart.HEAD : BedPart.FOOT;
    }

    /** The layout front of a bed whose vanilla facing (foot to head) is {@code bedFacing}. */
    public static Direction front(Direction bedFacing) {
        return bedFacing.getOpposite();
    }
}
