package io.github.dirtyredz.herbalwitch.botany.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A raised herb bed: a wooden planter of tilled soil that holds one plant. Moisture works like
 * farmland ({@link GardenSoil}); unlike farmland it is never trampled and never dries back to dirt.
 * Beds side by side join into one planter ({@link GardenBedConnections}): the shared rails and
 * posts disappear. State lives entirely in block properties: no block entity, no continuous scan.
 */
public class HerbBedBlock extends Block {
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    /** Soil tonic: the next mature harvest above gives one extra fresh herb, then clears. */
    public static final BooleanProperty ENRICHED = BooleanProperty.create("enriched");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    public HerbBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MOISTURE, 0).setValue(ENRICHED, false)
                .setValue(GardenBedConnections.NORTH, false).setValue(GardenBedConnections.EAST, false)
                .setValue(GardenBedConnections.SOUTH, false).setValue(GardenBedConnections.WEST, false));
    }

    public static boolean isMoist(BlockState state) {
        return state.getValue(MOISTURE) > 0;
    }

    /** If the bed at {@code pos} is enriched, clears it and returns 1 extra herb; otherwise 0. */
    public static int consumeEnrichment(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof HerbBedBlock && state.getValue(ENRICHED)) {
            level.setBlock(pos, state.setValue(ENRICHED, false), Block.UPDATE_CLIENTS);
            return 1;
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE, ENRICHED, GardenBedConnections.NORTH, GardenBedConnections.EAST, GardenBedConnections.SOUTH,
                GardenBedConnections.WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean watered = GardenSoil.isWatered(level, pos, level.isRainingAt(pos.above()));
        return GardenBedConnections.connect(defaultBlockState().setValue(MOISTURE, watered ? GardenSoil.MAX_MOISTURE : 0), level, pos);
    }

    /**
     * Newly placed by any means (including a plain setBlock, as the showcase does): join existing
     * neighbours. A bed changing its own state (moisture, enrichment) keeps its connections.
     */
    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        if (old.is(this)) {
            return;
        }
        BlockState joined = GardenBedConnections.connect(state, level, pos);
        if (joined != state) {
            level.setBlock(pos, joined, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        BooleanProperty side = GardenBedConnections.property(direction);
        return side == null ? state : state.setValue(side, GardenBedConnections.joins(state, neighbour));
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState rotate(BlockState state, Rotation rotation) {
        return GardenBedConnections.rotate(state, rotation);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState mirror(BlockState state, Mirror mirror) {
        return GardenBedConnections.mirror(state, mirror);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla random tick
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        int next = GardenSoil.nextMoisture(moisture, GardenSoil.isWatered(level, pos, level.isRainingAt(pos.above())));
        // Also repairs beds saved before connections existed (their sides default to unjoined).
        BlockState updated = GardenBedConnections.connect(state.setValue(MOISTURE, next), level, pos);
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        }
    }

    /** Moist soil speeds growth like moist farmland (vanilla crop growth reads this). */
    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return isMoist(state);
    }

    /** A plain fall: no trampling. */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
        entity.causeFallDamage(distance, 1.0F, entity.damageSources().fall());
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla shape hook
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
