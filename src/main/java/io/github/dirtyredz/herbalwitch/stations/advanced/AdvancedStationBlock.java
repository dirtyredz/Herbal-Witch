package io.github.dirtyredz.herbalwitch.stations.advanced;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.heat.HeatSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The block of every production station (cauldron, still, bottling bench, charm workbench): one class
 * parameterised by its cells, shapes and support, never one per station. It shares the station block
 * lifecycle ({@link StationBlock}: menu on any cell, origin-only entity and loot, teardown) and places
 * through the shared checked, previewed structure placement ({@link StructurePlacement}), so the whole
 * station lands or nothing does. {@link #ACTIVE} (shared by every cell) shows the working state.
 */
public class AdvancedStationBlock extends StationBlock implements StructurePlacement {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final boolean heated;

    /**
     * @param layout the cells, or null for a one-block station
     * @param shapes one shape map per cell
     * @param heated whether it stands on a heat source (a sturdy floor, a heat source or a conductor)
     *               rather than only a sturdy floor
     */
    public static AdvancedStationBlock create(Properties properties, MultiblockLayout layout, List<Map<Direction, VoxelShape>> shapes,
                                             boolean heated, Supplier<? extends BlockEntityType<?>> type) {
        PendingLayout.set(layout);
        try {
            return new AdvancedStationBlock(properties, layout, shapes, heated, type);
        } finally {
            PendingLayout.set(null);
        }
    }

    private AdvancedStationBlock(Properties properties, MultiblockLayout layout, List<Map<Direction, VoxelShape>> shapes, boolean heated,
                                 Supplier<? extends BlockEntityType<?>> type) {
        super(properties, layout, shapes, true, type);
        this.heated = heated;
        BlockState state = stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false);
        if (layout != null) {
            state = state.setValue(layout.part(), 0);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
        MultiblockLayout pending = PendingLayout.get();
        if (pending != null) {
            builder.add(pending.part());
        }
    }

    /**
     * The layout whose part property the state definition needs while {@link #create} runs: vanilla
     * builds the definition inside the Block constructor, before any field of this class is set.
     */
    private static final class PendingLayout {
        private static final ThreadLocal<MultiblockLayout> CURRENT = new ThreadLocal<>();

        static MultiblockLayout get() {
            return CURRENT.get();
        }

        static void set(MultiblockLayout layout) {
            CURRENT.set(layout);
        }
    }

    // ---------------------------------------------------------------- placement

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // every cell was placed by the structure placement already
    }

    @Override
    public MultiblockLayout placementLayout() {
        return layout();
    }

    @Override
    public Direction layoutFacing(BlockState originState) {
        return originState.getValue(FACING);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return layout() == null ? originState : originState.setValue(layout().part(), index);
    }

    /** Ground cells stand on a sturdy top (heated stations: or a heat source or conductor); never reading an unloaded chunk. */
    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
        if (layout() != null && !layout().grounded(cellState.getValue(layout().part()))) {
            return true;
        }
        BlockPos below = pos.below();
        if (!level.hasChunkAt(below)) {
            return false;
        }
        return heated ? HeatSources.supports(level, below)
                : level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supported(level, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
