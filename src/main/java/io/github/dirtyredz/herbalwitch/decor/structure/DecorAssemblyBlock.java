package io.github.dirtyredz.herbalwitch.decor.structure;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.FurnitureShapes;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockStructure;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * A furniture piece of one or more cells: its facing (toward whoever placed it, or away from the
 * wall it hangs on), its cell index for a multiblock, its authored shape per cell, its support,
 * and the shared structure lifecycle: breaking any cell removes the whole piece through ordinary
 * neighbour updates (only the origin's loot table drops the item), creative breaking drops nothing,
 * losing support tears it down the same way. Placement goes through
 * {@link FurniturePlacementItem} and this block's {@link StructurePlacement} answers.
 * Behaviours (seating, storage, light) extend it with their own use; none adds a machine.
 */
public class DecorAssemblyBlock extends Block implements StructurePlacement {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** The piece being constructed: the state definition is built inside the super constructor. */
    private static final ThreadLocal<FurnitureSpec> BUILDING = new ThreadLocal<>();

    private final FurnitureSpec spec;
    private final MultiblockLayout layout;
    private final List<Map<Direction, VoxelShape>> shapes;

    public DecorAssemblyBlock(FurnitureSpec spec, Properties properties) {
        super(building(spec, properties));
        BUILDING.remove();
        this.spec = spec;
        this.layout = FurnitureLayouts.of(spec);
        this.shapes = FurnitureShapes.cells(spec);
        BlockState state = stateDefinition.any();
        if (spec.directional()) {
            state = state.setValue(FACING, Direction.NORTH);
        }
        if (layout != null) {
            state = state.setValue(layout.part(), 0);
        }
        registerDefaultState(state);
    }

    private static Properties building(FurnitureSpec spec, Properties properties) {
        BUILDING.set(spec);
        return properties;
    }

    /** The spec under construction (inside the super constructor) or of this block. */
    protected FurnitureSpec spec() {
        return spec != null ? spec : BUILDING.get();
    }

    public FurnitureSpec furniture() {
        return spec;
    }

    /** The cells, or null for a one-cell piece. */
    public MultiblockLayout layout() {
        return layout;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        FurnitureSpec building = spec();
        if (building.directional()) {
            builder.add(FACING);
        }
        MultiblockLayout cells = FurnitureLayouts.of(building);
        if (cells != null) {
            builder.add(cells.part());
        }
    }

    /** The cell index of a state (0 for a one-cell piece). */
    public int cellIndex(BlockState state) {
        return layout == null ? 0 : state.getValue(layout.part());
    }

    public Direction facing(BlockState state) {
        return spec.directional() ? state.getValue(FACING) : Direction.NORTH;
    }

    public boolean isOrigin(BlockState state) {
        return cellIndex(state) == 0;
    }

    /** The origin cell of the structure {@code pos} belongs to. */
    public BlockPos origin(BlockState state, BlockPos pos) {
        return layout == null ? pos.immutable() : layout.origin(pos, facing(state), cellIndex(state));
    }

    // ---------------------------------------------------------------- placement

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return spec.directional() ? state.setValue(FACING, placementFacing(context)) : state;
    }

    /** Wall pieces face away from the aimed wall face; everything else faces the player. */
    protected Direction placementFacing(BlockPlaceContext context) {
        Support support = spec.support();
        if ((support == Support.WALL || support == Support.CORNER) && context.getClickedFace().getAxis().isHorizontal()) {
            return context.getClickedFace();
        }
        return context.getHorizontalDirection().getOpposite();
    }

    @Override
    public MultiblockLayout placementLayout() {
        return layout;
    }

    @Override
    public Direction layoutFacing(BlockState originState) {
        return facing(originState);
    }

    @Override
    public int anchorCell(BlockState originState) {
        return FurnitureLayouts.anchorCell(spec);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return layout == null ? originState : originState.setValue(layout.part(), index);
    }

    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
        return FurnitureSupport.supported(spec.support(), layout, cellIndex(cellState), facing(cellState), level, pos);
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla survival hook
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supported(level, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla neighbour hook
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (layout != null && !MultiblockStructure.intact(state, layout, direction, neighbour)) {
            return Blocks.AIR.defaultBlockState(); // the rest of the piece is gone
        }
        if (FurnitureSupport.anchors(spec.support(), layout, cellIndex(state), facing(state)).contains(direction)
                && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // lost its support: the same teardown
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
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla shape hook
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(cellIndex(state)).get(facing(state));
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling: structure blocks rotate with their facing
    public BlockState rotate(BlockState state, Rotation rotation) {
        return spec.directional() && layout == null ? state.setValue(FACING, rotation.rotate(state.getValue(FACING))) : state;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState mirror(BlockState state, Mirror mirror) {
        return spec.directional() && layout == null ? state.rotate(mirror.getRotation(state.getValue(FACING))) : state;
    }
}
