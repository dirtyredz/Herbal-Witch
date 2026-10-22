package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessJob;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * A production station's origin entity: the shared process-station lifecycle plus the visual working
 * state, {@link AdvancedStationBlock#ACTIVE} on every cell exactly while the job is working (set only
 * when it changes, never per tick).
 */
public abstract class AdvancedStationEntity extends ProcessStationEntity {
    protected AdvancedStationEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ProcessInventory inventory,
                                    Supplier<? extends RecipeType<? extends ProcessRecipe>> recipeType, boolean needsHeat, boolean magical,
                                    ProcessJob.ResultDecorator decorator) {
        super(type, pos, state, inventory, recipeType, needsHeat, magical, decorator);
    }

    @Override
    public void serverTick() {
        super.serverTick();
        boolean working = station().job().status() == ProcessStatus.WORKING;
        BlockState state = getBlockState();
        if (state.hasProperty(AdvancedStationBlock.ACTIVE) && state.getValue(AdvancedStationBlock.ACTIVE) != working
                && state.getBlock() instanceof AdvancedStationBlock block) {
            block.setOnStation(level, worldPosition, AdvancedStationBlock.ACTIVE, working);
        }
    }
}
