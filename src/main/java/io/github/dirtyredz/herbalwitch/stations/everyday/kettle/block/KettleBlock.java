package io.github.dirtyredz.herbalwitch.stations.everyday.kettle.block;

import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import net.minecraft.core.Direction;

/** An iron tea kettle; stands on a sturdy surface, a heat source or a conductor. */
public class KettleBlock extends StationBlock {
    public KettleBlock(Properties properties) {
        super(properties, KettleShapes.BODY, true, StationBlockEntities.KETTLE);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
}
