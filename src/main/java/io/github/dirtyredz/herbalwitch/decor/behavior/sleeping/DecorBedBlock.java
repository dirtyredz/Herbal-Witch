package io.github.dirtyredz.herbalwitch.decor.behavior.sleeping;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.FurnitureShapes;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

/**
 * The cottage bed and the canopy bed: vanilla's {@link BedBlock} (so sleeping, the night and monster
 * rules, occupancy, spawn setting, unsafe dimensions and waking all stay vanilla's) over a furniture
 * layout. Each cell carries its index in a separate {@code cell} property, never vanilla's
 * {@code part}; the foot and head are the sleeping pair, every other cell routes use to the head and
 * is not a bed. One sleeper per bed. Static art from compiled models, no bed renderer, no colour.
 * Breaking any cell removes the whole bed (waking a sleeper) and drops it once, from the foot.
 */
public class DecorBedBlock extends BedBlock implements StructurePlacement {
    private static final ThreadLocal<FurnitureSpec> BUILDING = new ThreadLocal<>();

    private final FurnitureSpec spec;
    private final MultiblockLayout layout;
    private final List<Map<Direction, VoxelShape>> shapes;

    public DecorBedBlock(FurnitureSpec spec, Properties properties) {
        super(DyeColor.GREEN, building(spec, properties));
        BUILDING.remove();
        this.spec = spec;
        this.layout = FurnitureLayouts.of(spec);
        this.shapes = FurnitureShapes.cells(spec);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false).setValue(layout.part(), 0));
    }

    private static Properties building(FurnitureSpec spec, Properties properties) {
        BUILDING.set(spec);
        return properties;
    }

    public FurnitureSpec furniture() {
        return spec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FurnitureLayouts.of(spec != null ? spec : BUILDING.get()).part());
    }

    public int cell(BlockState state) {
        return state.getValue(layout.part());
    }

    private Direction front(BlockState state) {
        return BedLayout.front(state.getValue(FACING));
    }

    private BlockPos origin(BlockState state, BlockPos pos) {
        return layout.origin(pos, front(state), cell(state));
    }

    // ---------------------------------------------------------------- placement

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public MultiblockLayout placementLayout() {
        return layout;
    }

    @Override
    public Direction layoutFacing(BlockState originState) {
        return front(originState);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return originState.setValue(layout.part(), index).setValue(PART, BedLayout.part(layout, index));
    }

    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
        return true; // vanilla beds need no support
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // every cell is already placed by the checked placement; vanilla would place a head here
    }

    // ---------------------------------------------------------------- sleeping

    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, Entity sleeper) {
        return BedLayout.sleepingPair(layout, cell(state));
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla use hook
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BedLayout.sleepingPair(layout, cell(state))) {
            return super.use(state, level, pos, player, hand, hit);
        }
        BlockPos head = layout.cellPos(origin(state, pos), front(state), BedLayout.head(layout));
        BlockState headState = level.getBlockState(head);
        if (!headState.is(this)) {
            return InteractionResult.CONSUME;
        }
        return super.use(headState, level, head, player, hand, hit);
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla neighbour hook
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos,
                                  BlockPos neighbourPos) {
        Integer expected = layout.neighbours(front(state), cell(state)).get(direction);
        if (expected == null) {
            return state;
        }
        boolean intact = neighbour.is(this) && neighbour.getValue(FACING) == state.getValue(FACING) && cell(neighbour) == expected;
        if (!intact) {
            return Blocks.AIR.defaultBlockState(); // the rest of the bed is gone
        }
        boolean pair = BedLayout.sleepingPair(layout, cell(state)) && BedLayout.sleepingPair(layout, expected);
        return pair ? state.setValue(OCCUPIED, neighbour.getValue(OCCUPIED)) : state;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BlockPos origin = origin(state, pos);
            BlockState originState = level.getBlockState(origin);
            if (!pos.equals(origin) && originState.is(this)) {
                level.setBlock(origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, origin, Block.getId(originState));
            }
        }
        // vanilla's own creative handling assumes a two-block bed; do the rest of Block's part instead
        spawnDestroyParticles(level, player, pos, state);
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla shape hook
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(cell(state)).get(front(state));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null; // static art: no bed renderer, no colour
    }
}
