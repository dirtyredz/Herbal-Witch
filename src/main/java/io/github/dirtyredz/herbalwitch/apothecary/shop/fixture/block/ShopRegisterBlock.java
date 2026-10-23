package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The shop register: a till on the apothecary counter. Placing one creates nothing; an initiated witch
 * creates or reconnects a shop from it. {@link #OPEN} is the visible open/closed indicator, a
 * projection of the shop's lifecycle that the shop service sets; it is never authority.
 */
public final class ShopRegisterBlock extends ShopFixtureBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public ShopRegisterBlock(Properties properties) {
        super(ShopPieceCatalog.REGISTER, properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(OPEN, false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopFixtureBlockEntity(ShopBlockEntities.REGISTER.get(), pos, state);
    }
}
