package io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A gothic fireplace, two blocks wide and two tall, burning ordinary furnace fuel. {@link #LIT}
 * while it burns, on every cell: light 13 and heat for a vessel on its chimney top (it is in the
 * heat-source tag). It never spreads fire.
 */
public class HearthBlock extends StationBlock {
    public static final MultiblockLayout LAYOUT = StationCatalog.HEARTH.layout();
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIGHT = 13;

    public HearthBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? LIGHT : 0), LAYOUT, HearthShapes.CELLS, false,
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
