package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlockEntities;
import net.minecraft.core.Direction;

/** A ceramic infusion pot; stands on a sturdy surface, a heat source or a conductor. */
public class InfusionPotBlock extends StationBlock {
    public InfusionPotBlock(Properties properties) {
        super(properties, InfusionPotShapes.BODY, true, StationBlockEntities.INFUSION_POT);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
}
