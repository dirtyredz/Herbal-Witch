package io.github.dirtyredz.herbalwitch.apothecary.botany.crop;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * A herb grown like a vanilla crop over four ages (0..3; 3 is mature). Growth is vanilla crop
 * growth; using a mature plant harvests it in place ({@link HerbHarvest}) and it regrows from
 * {@link #harvestResetAge()}. Breaking follows the block's loot table: one seed when immature, the
 * full harvest when mature.
 */
public class HerbCropBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] SHAPES = {
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(2, 0, 2, 14, 6, 14),
            Block.box(2, 0, 2, 14, 10, 14),
            Block.box(2, 0, 2, 14, 13, 14)};

    private final HerbSpec herb;
    private final Supplier<? extends ItemLike> seeds;

    public HerbCropBlock(Properties properties, HerbSpec herb, Supplier<? extends ItemLike> seeds) {
        super(properties);
        this.herb = herb;
        this.seeds = seeds;
    }

    public HerbSpec herb() {
        return herb;
    }

    /** Age after a harvest by hand: 0 for crops, 1 for bushes. */
    public int harvestResetAge() {
        return herb.form().harvestResetAge();
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return HerbSpec.MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seeds.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState below, BlockGetter level, BlockPos pos) {
        return HerbGrowth.canStandOn(below);
    }

    /**
     * Soil only, like any bush: vanilla crops also demand light to survive, but light is not computed
     * while worldgen places wild patches, so a light check would stop every patch generating. Growth
     * still needs light (vanilla crop random tick).
     */
    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla survival hook
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        if (state.getBlock() == this) {
            return level.getBlockState(below).canSustainPlant(level, below, Direction.UP, this);
        }
        return mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return HerbGrowth.bonemealIncrease(level.random);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla use hook
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return HerbHarvest.harvestByUse(this, state, level, pos, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[getAge(state)];
    }
}
