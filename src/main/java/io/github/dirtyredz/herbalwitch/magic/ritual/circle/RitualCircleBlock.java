package io.github.dirtyredz.herbalwitch.magic.ritual.circle;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarAccess;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockStructure;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The ritual circle: 16 low engraved cells in a 5 x 5 ring ({@link CircleCatalog}), placed whole
 * through the shared checked, previewed structure placement, each cell on a sturdy floor. No block
 * entity, no menu of its own, no inventory or hopper access: using any cell opens the altar it
 * surrounds when one stands correctly inside, and otherwise says what is wrong. Breaking any cell
 * removes the whole circle (one item drops, from the origin); {@link #ACTIVE} (every cell) glows while
 * a circle ritual runs.
 */
public class RitualCircleBlock extends Block implements StructurePlacement {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final MultiblockLayout LAYOUT = CircleCatalog.LAYOUT;
    public static final String NO_ALTAR = "ritual." + HerbalWitch.MOD_ID + ".circle.no_altar";

    public RitualCircleBlock(Properties properties) {
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

    /** The origin of the circle {@code pos} belongs to. */
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

    /** Every cell lies on a sturdy top face; never reading an unloaded chunk. */
    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
        BlockPos below = pos.below();
        return level.hasChunkAt(below) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /** Places a complete circle without a player (showcase, tests). */
    public void placeCircle(Level level, BlockPos origin, Direction facing, int flags) {
        MultiblockStructure.placeAll(level, origin, defaultBlockState().setValue(FACING, facing), LAYOUT, flags);
    }

    /** Shows (or clears) the ritual glow on every loaded cell of the circle around {@code altarOrigin}. */
    public static void setActive(Level level, BlockPos altarOrigin, boolean active) {
        if (!level.isLoaded(altarOrigin) || !(level.getBlockState(altarOrigin).getBlock() instanceof AltarBlock)) {
            return;
        }
        Direction facing = level.getBlockState(altarOrigin).getValue(AltarBlock.FACING);
        for (BlockPos pos : CircleLayout.cells(CircleLayout.circleOrigin(altarOrigin, facing), facing)) {
            if (level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof RitualCircleBlock && state.getValue(ACTIVE) != active) {
                    level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_CLIENTS);
                }
            }
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
            return Blocks.AIR.defaultBlockState(); // the rest of the circle is gone
        }
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        Direction facing = state.getValue(FACING);
        BlockPos altar = CircleLayout.altarOrigin(origin(state, pos), facing);
        boolean aligned = AltarAccess.complete(level, altar) && level.getBlockState(altar).getValue(AltarBlock.FACING) == facing;
        if (!aligned) {
            serverPlayer.displayClientMessage(Component.translatable(NO_ALTAR), true);
            return InteractionResult.FAIL;
        }
        return AltarMenu.open(serverPlayer, altar) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RitualCircleShapes.shape(cell(state), state.getValue(FACING));
    }
}
