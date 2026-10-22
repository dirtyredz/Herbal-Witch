package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlockEntities;
import net.minecraft.core.Direction;

/** A stone mortar and pestle on a small wooden base; needs a sturdy surface. */
public class MortarBlock extends StationBlock {
    public MortarBlock(Properties properties) {
        super(properties, MortarShapes.BODY, true, StationBlockEntities.MORTAR);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
}
