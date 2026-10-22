package io.github.dirtyredz.herbalwitch.magic.ritual.circle;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * The circle's geometry relative to the altar, pure: from either origin and their shared facing, the
 * other origin and every cell position. The placement preview, the placement, validation and the
 * showcase all use this one arithmetic.
 */
public final class CircleLayout {
    private CircleLayout() {
    }

    /** The altar origin's offset from the circle origin, in world axes, for a circle facing {@code facing}. */
    public static BlockPos altarOffset(Direction facing) {
        MultiblockLayout.Cell a = CircleCatalog.ALTAR;
        return BlockPos.ZERO.relative(MultiblockLayout.right(facing), a.right()).above(a.up()).relative(MultiblockLayout.back(facing), a.depth());
    }

    public static BlockPos altarOrigin(BlockPos circleOrigin, Direction facing) {
        return circleOrigin.offset(altarOffset(facing));
    }

    public static BlockPos circleOrigin(BlockPos altarOrigin, Direction facing) {
        return altarOrigin.subtract(altarOffset(facing));
    }

    /** Every cell's position, in cell order. */
    public static List<BlockPos> cells(BlockPos circleOrigin, Direction facing) {
        List<BlockPos> out = new ArrayList<>();
        for (int i = 0; i < CircleCatalog.LAYOUT.size(); i++) {
            out.add(CircleCatalog.LAYOUT.cellPos(circleOrigin, facing, i));
        }
        return out;
    }
}
