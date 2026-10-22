package io.github.dirtyredz.herbalwitch.apothecary.stations.block;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockStructure;
import io.github.dirtyredz.herbalwitch.apothecary.stations.heat.HeatSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Block behaviour shared by the stations: facing the placer, an optional support rule (a sturdy
 * surface, a heat source or a conductor below), opening the menu on use, a server-only ticker, and
 * dropping the block entity's contents once on removal. Stations add their own state properties.
 *
 * <p>A station may span several blocks ({@link MultiblockLayout}): its subclass adds the layout's
 * {@code part} property. Only the origin cell holds the block entity, ticks and drops the item; any
 * other cell opens the origin's menu, and breaking any cell removes the whole station.
 */
public abstract class StationBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final List<Map<Direction, VoxelShape>> shapes;
    private final boolean needsSupport;
    private final Supplier<? extends BlockEntityType<?>> type;
    private final MultiblockLayout layout;

    protected StationBlock(Properties properties, Map<Direction, VoxelShape> shapes, boolean needsSupport,
                           Supplier<? extends BlockEntityType<?>> type) {
        this(properties, null, List.of(shapes), needsSupport, type);
    }

    /** A multiblock station: {@code shapes} has one entry per cell of {@code layout}. */
    protected StationBlock(Properties properties, MultiblockLayout layout, List<Map<Direction, VoxelShape>> shapes,
                           boolean needsSupport, Supplier<? extends BlockEntityType<?>> type) {
        super(properties);
        if (shapes.size() != (layout == null ? 1 : layout.size())) {
            throw new IllegalArgumentException("one shape map per cell");
        }
        this.shapes = List.copyOf(shapes);
        this.needsSupport = needsSupport;
        this.type = type;
        this.layout = layout;
    }

    /** The station's cells, or null for a one-block station. */
    public MultiblockLayout layout() {
        return layout;
    }

    /** Whether {@code state} is the cell that holds the block entity. */
    public boolean isOrigin(BlockState state) {
        return layout == null || layout.isOrigin(state);
    }

    /** The position of the station's origin cell (its block entity). */
    public BlockPos origin(BlockState state, BlockPos pos) {
        return layout == null ? pos : MultiblockStructure.origin(state, pos, layout);
    }

    /** Places a complete station at {@code origin} without a player (showcase, tests). */
    public void placeStation(Level level, BlockPos origin, BlockState state, int flags) {
        if (layout == null) {
            level.setBlock(origin, state, flags);
        } else {
            MultiblockStructure.placeAll(level, origin, state, layout, flags);
        }
    }

    /**
     * Sets a shared display property (lit, active) on the whole station from its origin: every cell
     * of a multiblock, or the one block.
     */
    public <T extends Comparable<T>> void setOnStation(Level level, BlockPos origin, Property<T> property, T value) {
        if (layout == null) {
            BlockState state = level.getBlockState(origin);
            if (state.is(this) && !state.getValue(property).equals(value)) {
                level.setBlock(origin, state.setValue(property, value), Block.UPDATE_ALL);
            }
        } else {
            MultiblockStructure.setOnAll(level, origin, layout, property, value, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        if (layout != null) {
            return MultiblockStructure.canPlace(context, layout, facing,
                    (part, pos) -> supported(context.getLevel(), pos)) ? state.setValue(layout.part(), 0) : null;
        }
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (layout != null && !level.isClientSide) {
            MultiblockStructure.placeRest(level, pos, state, layout, Block.UPDATE_ALL);
        }
    }

    private boolean supported(LevelReader level, BlockPos pos) {
        return !needsSupport || HeatSources.supports(level, pos.below());
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (layout != null && !layout.grounded(state.getValue(layout.part()))) {
            return true; // upper cells stand on their own structure (checked in updateShape)
        }
        return supported(level, pos);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (layout != null && !MultiblockStructure.intact(state, layout, direction, neighbour)) {
            return Blocks.AIR.defaultBlockState(); // the rest of the station is gone
        }
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // removal drops the station and its contents
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (layout != null && !level.isClientSide && player.isCreative()) {
            MultiblockStructure.removeOriginQuietly(level, pos, state, layout);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(layout == null ? 0 : state.getValue(layout.part())).get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos origin = origin(state, pos);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(origin) instanceof StationEntity station) {
            NetworkHooks.openScreen(serverPlayer, station, origin);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof StationEntity station) {
            station.dropContents();
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, next, moved);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != type.get() || !isOrigin(state)) {
            return null;
        }
        return (tickLevel, tickPos, tickState, entity) -> ((StationEntity) entity).serverTick();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isOrigin(state) ? type.get().create(pos, state) : null;
    }
}
