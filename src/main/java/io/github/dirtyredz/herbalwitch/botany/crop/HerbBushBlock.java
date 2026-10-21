package io.github.dirtyredz.herbalwitch.botany.crop;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * A perennial herb bush: grows and harvests like a herb crop, but a harvest by hand cuts it back to
 * age 1 rather than to a sprout, and it fills more of its block.
 */
public class HerbBushBlock extends HerbCropBlock {
    private static final VoxelShape[] SHAPES = {
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(2, 0, 2, 14, 8, 14),
            Block.box(1, 0, 1, 15, 12, 15),
            Block.box(1, 0, 1, 15, 15, 15)};

    public HerbBushBlock(Properties properties, HerbSpec herb, Supplier<? extends ItemLike> seeds) {
        super(properties, herb, seeds);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[getAge(state)];
    }
}
