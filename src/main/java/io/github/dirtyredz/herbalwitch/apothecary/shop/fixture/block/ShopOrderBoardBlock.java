package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The wall-mounted order board: opens a linked shop's nonbinding customer requests. */
public final class ShopOrderBoardBlock extends ShopFixtureBlock {
    public ShopOrderBoardBlock(Properties properties) {
        super(ShopPieceCatalog.ORDER_BOARD, properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopFixtureBlockEntity(ShopBlockEntities.ORDER_BOARD.get(), pos, state);
    }
}
