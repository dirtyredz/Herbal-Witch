package io.github.dirtyredz.herbalwitch.decor.structure;

import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

import java.util.EnumSet;
import java.util.Set;

/**
 * The support rules every furniture behaviour shares (floor, wall, ceiling, corner, carpet), per
 * cell: only the cells on the supported side need it (the bottom row stands, the back column hangs
 * on the wall, the top row hangs from the ceiling). A neighbour in an unloaded chunk is never read
 * and never counts as support, so nothing here loads a chunk.
 */
public final class FurnitureSupport {
    private FurnitureSupport() {
    }

    /**
     * Whether a cell has its support.
     *
     * @param layout the piece's cells, or null for a one-cell piece
     * @param index  the cell
     * @param facing the piece's front (ignored by floor, ceiling and carpet support; null for pieces without a facing)
     */
    public static boolean supported(Support support, MultiblockLayout layout, int index, Direction facing, LevelReader level, BlockPos pos) {
        for (Direction d : anchors(support, layout, index, facing)) {
            BlockPos at = pos.relative(d);
            if (!level.hasChunkAt(at)) {
                return false;
            }
            boolean ok = switch (support) {
                case FLOOR -> Block.canSupportCenter(level, at, Direction.UP);
                case CEILING -> Block.canSupportCenter(level, at, Direction.DOWN);
                case CARPET -> !level.isEmptyBlock(at);
                case WALL, CORNER -> level.getBlockState(at).isFaceSturdy(level, at, d.getOpposite());
                case FREE -> true;
            };
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /** The directions from a cell to the blocks it rests on (empty when the cell needs nothing). */
    public static Set<Direction> anchors(Support support, MultiblockLayout layout, int index, Direction facing) {
        Set<Direction> out = EnumSet.noneOf(Direction.class);
        boolean bottom = layout == null || layout.grounded(index);
        boolean top = layout == null || layout.top(index);
        boolean rear = layout == null || layout.rear(index);
        switch (support) {
            case FLOOR, CARPET -> {
                if (bottom) {
                    out.add(Direction.DOWN);
                }
            }
            case CEILING -> {
                if (top) {
                    out.add(Direction.UP);
                }
            }
            case WALL -> {
                if (rear) {
                    out.add(MultiblockLayout.back(facing));
                }
            }
            case CORNER -> {
                if (rear) {
                    out.add(MultiblockLayout.back(facing));
                    out.add(MultiblockLayout.right(facing).getOpposite()); // the viewer's left
                }
            }
            case FREE -> {
            }
        }
        return out;
    }
}
