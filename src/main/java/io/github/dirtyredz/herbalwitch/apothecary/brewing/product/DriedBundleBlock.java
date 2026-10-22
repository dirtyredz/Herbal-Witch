package io.github.dirtyredz.herbalwitch.apothecary.brewing.product;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A dried herb bundle hung up as decoration: under a ceiling or on a wall (never on the floor),
 * needing a sturdy face like a button. It is one herb's bundle, breaks without the support, and
 * drops the very bundle item the recipes use.
 */
public class DriedBundleBlock extends FaceAttachedHorizontalDirectionalBlock {
    private static final VoxelShape CEILING = Block.box(4, 4, 4, 12, 16, 12);
    private static final VoxelShape NORTH = Block.box(4, 3, 12, 12, 15, 16);
    private static final VoxelShape SOUTH = Block.box(4, 3, 0, 12, 15, 4);
    private static final VoxelShape WEST = Block.box(12, 3, 4, 16, 15, 12);
    private static final VoxelShape EAST = Block.box(0, 3, 4, 4, 15, 12);

    private final HerbSpec herb;

    public DriedBundleBlock(Properties properties, HerbSpec herb) {
        super(properties);
        this.herb = herb;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.CEILING));
    }

    public HerbSpec herb() {
        return herb;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && state.getValue(FACE) == AttachFace.FLOOR ? null : state;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(FACE) != AttachFace.WALL) {
            return CEILING;
        }
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }
}
