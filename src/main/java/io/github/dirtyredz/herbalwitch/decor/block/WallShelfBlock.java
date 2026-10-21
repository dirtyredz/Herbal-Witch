package io.github.dirtyredz.herbalwitch.decor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A shelf hung on a wall: it faces away from the wall behind it and needs that wall's face to be
 * sturdy, like a wall torch. Losing the wall breaks it and drops it. Display storage arrives later.
 */
public class WallShelfBlock extends HorizontalFurnitureBlock {
    public WallShelfBlock(Properties properties, Map<Direction, VoxelShape> shapes) {
        super(properties, shapes);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction looking : context.getNearestLookingDirections()) {
            if (looking.getAxis().isHorizontal()) {
                BlockState state = defaultBlockState().setValue(FACING, looking.getOpposite());
                if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla survival hook
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla neighbour hook
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // vanilla then destroys the shelf with its drop
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }
}
