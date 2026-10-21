package io.github.dirtyredz.herbalwitch.platform.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.function.BiPredicate;

/**
 * World-side multiblock lifecycle over a {@link MultiblockLayout}: room to place, placing the other
 * cells, recognising a broken structure, keeping a shared property (lit, active) the same on every
 * cell, and creative removal without drops. Blocks call these from their own hooks; removal of
 * one cell removes the others through ordinary neighbour shape updates, and only the origin drops.
 */
public final class MultiblockStructure {
    /** Quiet removal: neighbours update, nothing drops (vanilla's double-plant creative rule). */
    private static final int REMOVE_QUIETLY = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;

    private MultiblockStructure() {
    }

    /** Whether every cell from {@code origin} is replaceable and inside the world, and grounded cells pass {@code support}. */
    public static boolean canPlace(BlockPlaceContext context, MultiblockLayout layout, Direction facing,
                                   BiPredicate<Integer, BlockPos> support) {
        Level level = context.getLevel();
        for (int i = 0; i < layout.size(); i++) {
            BlockPos pos = layout.cellPos(context.getClickedPos(), facing, i);
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }
            if (i > 0 && !level.getBlockState(pos).canBeReplaced(context)) {
                return false;
            }
            if (layout.grounded(i) && !support.test(i, pos)) {
                return false;
            }
        }
        return true;
    }

    /** Places cells 1.. next to an origin already in the world, matching its state. */
    public static void placeRest(Level level, BlockPos origin, BlockState originState, MultiblockLayout layout, int flags) {
        Direction facing = originState.getValue(HorizontalDirectionalBlock.FACING);
        for (int i = 1; i < layout.size(); i++) {
            level.setBlock(layout.cellPos(origin, facing, i), originState.setValue(layout.part(), i), flags);
        }
    }

    /** Places the whole structure (origin first): for direct placement by commands, showcases and tests. */
    public static void placeAll(Level level, BlockPos origin, BlockState originState, MultiblockLayout layout, int flags) {
        level.setBlock(origin, originState.setValue(layout.part(), 0), flags);
        placeRest(level, origin, originState.setValue(layout.part(), 0), layout, flags);
    }

    /**
     * Whether the neighbour in {@code direction} still belongs to this cell's structure. Directions
     * outside the structure always pass.
     */
    public static boolean intact(BlockState state, MultiblockLayout layout, Direction direction, BlockState neighbour) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        Map<Direction, Integer> expected = layout.neighbours(facing, state.getValue(layout.part()));
        Integer part = expected.get(direction);
        return part == null || neighbour.is(state.getBlock())
                && neighbour.getValue(HorizontalDirectionalBlock.FACING) == facing && neighbour.getValue(layout.part()).equals(part);
    }

    /** The origin position of the structure {@code pos} belongs to. */
    public static BlockPos origin(BlockState state, BlockPos pos, MultiblockLayout layout) {
        return layout.origin(pos, state.getValue(HorizontalDirectionalBlock.FACING), state.getValue(layout.part()));
    }

    /** Sets {@code property} on every cell still standing (client update only, no neighbour churn). */
    public static <T extends Comparable<T>> void setOnAll(Level level, BlockPos origin, MultiblockLayout layout, Property<T> property,
                                                         T value, int flags) {
        BlockState originState = level.getBlockState(origin);
        Direction facing = originState.getValue(HorizontalDirectionalBlock.FACING);
        for (int i = 0; i < layout.size(); i++) {
            BlockPos pos = layout.cellPos(origin, facing, i);
            BlockState cell = level.getBlockState(pos);
            if (cell.is(originState.getBlock()) && cell.getValue(layout.part()) == i && !cell.getValue(property).equals(value)) {
                level.setBlock(pos, cell.setValue(property, value), flags);
            }
        }
    }

    /**
     * A creative player breaking a non-origin cell: remove the origin without its loot (the rest then
     * falls away through shape updates), so creative breaking never drops the item.
     */
    public static void removeOriginQuietly(Level level, BlockPos pos, BlockState state, MultiblockLayout layout) {
        BlockPos origin = origin(state, pos, layout);
        BlockState originState = level.getBlockState(origin);
        if (!pos.equals(origin) && originState.is(state.getBlock())) {
            level.setBlock(origin, Blocks.AIR.defaultBlockState(), REMOVE_QUIETLY);
            level.levelEvent(null, 2001, origin, Block.getId(originState));
        }
    }
}
