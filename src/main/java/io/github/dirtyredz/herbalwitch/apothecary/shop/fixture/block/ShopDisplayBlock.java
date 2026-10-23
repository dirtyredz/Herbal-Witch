package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The stock shelf (three offers, on a wall) and stock jar (one offer, on a surface) share this block;
 * the piece descriptor supplies mount, shape and slot count. Displays show public projections only.
 */
public final class ShopDisplayBlock extends ShopFixtureBlock {
    public ShopDisplayBlock(ShopPieceSpec spec, Properties properties) {
        super(spec, properties);
        if (!spec.display()) throw new IllegalArgumentException(spec.key() + " is not a display");
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopDisplayBlockEntity(ShopBlockEntities.DISPLAY.get(), pos, state);
    }
}
