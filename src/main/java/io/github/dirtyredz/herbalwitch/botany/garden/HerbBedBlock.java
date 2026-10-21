package io.github.dirtyredz.herbalwitch.botany.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A raised herb bed: a wooden trough of tilled soil that holds one plant. Moisture works like
 * farmland ({@link GardenSoil}); unlike farmland it is never trampled and never dries back to dirt.
 * State lives entirely in block properties: no block entity, no continuous scan.
 */
public class HerbBedBlock extends Block {
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    public HerbBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MOISTURE, 0));
    }

    public static boolean isMoist(BlockState state) {
        return state.getValue(MOISTURE) > 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean watered = GardenSoil.isWatered(level, pos, level.isRainingAt(pos.above()));
        return defaultBlockState().setValue(MOISTURE, watered ? GardenSoil.MAX_MOISTURE : 0);
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
        if (next != moisture) {
            level.setBlock(pos, state.setValue(MOISTURE, next), Block.UPDATE_CLIENTS);
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
