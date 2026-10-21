package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A standing A-frame drying rack, two blocks wide and two tall. Needs a sturdy floor under both
 * feet. {@link #ACTIVE} hangs herb bundles from its beam whenever it holds anything (input or
 * output); the bundles show occupancy, not which herb.
 */
public class DryingRackBlock extends StationBlock {
    public static final MultiblockLayout LAYOUT = StationCatalog.DRYING_RACK.layout();
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public DryingRackBlock(Properties properties) {
        super(properties, LAYOUT, DryingRackShapes.CELLS, true, StationBlockEntities.DRYING_RACK);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false)
                .setValue(LAYOUT.part(), 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, LAYOUT.part());
    }
}
