package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.block;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.menu.DryingRackMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlockEntities;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.BlockState;

/** Drying rack contents: one fresh herb slot and one bundle slot; drying needs no heat. Shows bundles while it holds anything. */
public class DryingRackBlockEntity extends ProcessStationEntity {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.DRYING_RACK.get(), pos, state, newInventory(), StationRecipes.DRYING, false, false);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.OUTPUT);
    }

    @Override
    public void serverTick() {
        super.serverTick();
        boolean active = !inventory().getStackInSlot(INPUT).isEmpty() || !inventory().getStackInSlot(OUTPUT).isEmpty();
        BlockState state = getBlockState();
        if (state.hasProperty(DryingRackBlock.ACTIVE) && state.getValue(DryingRackBlock.ACTIVE) != active
                && state.getBlock() instanceof DryingRackBlock rack) {
            rack.setOnStation(level, worldPosition, DryingRackBlock.ACTIVE, active);
        }
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new DryingRackMenu(id, player, inventory, data, access);
    }
}
