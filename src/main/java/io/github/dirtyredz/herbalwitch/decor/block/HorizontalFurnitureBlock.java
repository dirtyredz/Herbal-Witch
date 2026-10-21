package io.github.dirtyredz.herbalwitch.decor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A free-standing piece that faces whoever placed it, with one collision/selection shape per
 * facing. Holds no behaviour beyond facing and shape; seating, storage and the like arrive as their
 * own behaviour families.
 */
public class HorizontalFurnitureBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapes;

    public HorizontalFurnitureBlock(Properties properties, Map<Direction, VoxelShape> shapes) {
        super(properties);
        this.shapes = Map.copyOf(shapes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla shape hook
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }
}
