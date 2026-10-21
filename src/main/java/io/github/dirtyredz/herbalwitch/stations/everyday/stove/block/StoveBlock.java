package io.github.dirtyredz.herbalwitch.stations.everyday.stove.block;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlock;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The witch's stove: a two-block cooking range with a burner on each cell. It burns fuel exactly
 * like the hearth (the same block entity, menu and screen) and both burners heat a vessel standing
 * on them while it is {@link #LIT}.
 */
public class StoveBlock extends StationBlock {
    public static final MultiblockLayout LAYOUT = StationCatalog.STOVE.layout();
    public static final BooleanProperty LIT = HearthBlock.LIT;

    public StoveBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? HearthBlock.LIGHT : 0), LAYOUT, StoveShapes.CELLS, false,
                StationBlockEntities.HEARTH);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false)
                .setValue(LAYOUT.part(), 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, LAYOUT.part());
    }
}
