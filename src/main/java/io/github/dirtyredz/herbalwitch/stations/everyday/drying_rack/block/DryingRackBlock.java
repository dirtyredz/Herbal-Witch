package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block;

import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A standing wooden drying frame. Needs a sturdy floor. {@link #ACTIVE} shows herbs hanging from it
 * whenever it holds anything.
 */
public class DryingRackBlock extends StationBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public DryingRackBlock(Properties properties) {
        super(properties, DryingRackShapes.FRAME, true, StationBlockEntities.DRYING_RACK);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }
}
