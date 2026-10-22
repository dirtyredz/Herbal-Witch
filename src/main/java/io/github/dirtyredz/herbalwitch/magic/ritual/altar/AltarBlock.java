package io.github.dirtyredz.herbalwitch.magic.ritual.altar;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarAccess;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlockEntities;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockStructure;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The functional altar, 2 wide x 1 deep x 2 high: facing toward whoever placed it, a cell index, and
 * {@code active} while a ritual runs (shared by every cell, light {@link AltarCatalog#ACTIVE_LIGHT},
 * visual only: no heat, no processing). Placement goes through the shared checked structure item
 * ({@link StructurePlacement}); both bottom cells need a sturdy floor. Breaking any cell, losing
 * support or an explosion removes the whole altar through neighbour updates (only the origin drops
 * the item) and cancels its ritual; pistons cannot move it. Any cell opens the origin's menu. There
 * is no inventory, item handler, fuel or automatic activation.
 */
public class AltarBlock extends BaseEntityBlock implements StructurePlacement {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final MultiblockLayout LAYOUT = AltarCatalog.LAYOUT;

    public AltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LAYOUT.part(), 0).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LAYOUT.part(), ACTIVE);
    }

    public static int cell(BlockState state) {
        return state.getValue(LAYOUT.part());
    }

    /** The origin of the altar {@code pos} belongs to. */
    public static BlockPos origin(BlockState state, BlockPos pos) {
        return MultiblockStructure.origin(state, pos, LAYOUT);
    }

    // ---------------------------------------------------------------- placement

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public MultiblockLayout placementLayout() {
        return LAYOUT;
    }

    @Override
    public Direction layoutFacing(BlockState originState) {
        return originState.getValue(FACING);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return originState.setValue(LAYOUT.part(), index);
    }

    /** Bottom cells stand on a sturdy top face; never reading an unloaded chunk. */
    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
        if (!LAYOUT.grounded(cell(cellState))) {
            return true;
        }
        BlockPos below = pos.below();
        return level.hasChunkAt(below) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /** Places a complete altar without a player (showcase, tests). */
    public void placeAltar(Level level, BlockPos origin, Direction facing, int flags) {
        MultiblockStructure.placeAll(level, origin, defaultBlockState().setValue(FACING, facing), LAYOUT, flags);
    }

    /** Shows (or clears) the ritual glow on every standing cell. */
    public static void setActive(Level level, BlockPos origin, boolean active) {
        if (level.isLoaded(origin) && level.getBlockState(origin).getBlock() instanceof AltarBlock) {
            MultiblockStructure.setOnAll(level, origin, LAYOUT, ACTIVE, active, Block.UPDATE_ALL);
        }
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supported(level, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos,
                                  BlockPos neighbourPos) {
        if (!MultiblockStructure.intact(state, LAYOUT, direction, neighbour)) {
            return Blocks.AIR.defaultBlockState(); // the rest of the altar is gone
        }
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // lost its floor: the same teardown
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            MultiblockStructure.removeOriginQuietly(level, pos, state, LAYOUT);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && cell(state) == 0 && level instanceof ServerLevel server) {
            AltarSessionService.altarGone(server, pos);
        }
        super.onRemove(state, level, pos, next, moved);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos origin = origin(state, pos);
        if (!(player instanceof ServerPlayer serverPlayer) || !AltarAccess.mayUse(serverPlayer, origin)) {
            return InteractionResult.FAIL;
        }
        return AltarMenu.open(serverPlayer, origin) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AltarShapes.shape(cell(state), state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return cell(state) == 0 ? new AltarBlockEntity(pos, state) : null;
    }

    /** Only an active origin ticks: it clears a glow no session owns (after a restart or a crash). */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || cell(state) != 0 || !state.getValue(ACTIVE) || type != RitualBlockEntities.ALTAR.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, entity) -> ((AltarBlockEntity) entity).serverTick();
    }
}
