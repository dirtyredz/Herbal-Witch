package io.github.dirtyredz.herbalwitch.decor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A lamp standing on a sturdy surface. Using it switches it on or off on the server; the state is
 * an ordinary block property, so it is saved with the chunk and reaches every client through the
 * normal block update. Losing the surface below breaks it and drops it.
 */
public class TableLampBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIGHT = 14;

    private final VoxelShape shape;

    public TableLampBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    /** Light emitted in a state; pass to {@code Properties.lightLevel}. */
    public static int lightEmission(BlockState state) {
        return state.getValue(LIT) ? LIGHT : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla survival hook
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla neighbour hook
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // vanilla then destroys the lamp with its drop
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla use hook
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockState next = state.cycle(LIT);
            level.setBlock(pos, next, Block.UPDATE_ALL);
            level.playSound(null, pos, next.getValue(LIT) ? SoundEvents.WOODEN_BUTTON_CLICK_ON
                    : SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.6F, 1.2F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla shape hook
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }
}
