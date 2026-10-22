package io.github.dirtyredz.herbalwitch.stations.advanced.charm_workbench.block;

import io.github.dirtyredz.herbalwitch.stations.advanced.AdvancedStationEntity;
import io.github.dirtyredz.herbalwitch.stations.advanced.charm_workbench.menu.CharmWorkbenchMenu;
import io.github.dirtyredz.herbalwitch.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Charm workbench contents: three ingredients, the charm and a return slot. No heat; a witch starts each charm.
 */
public class CharmWorkbenchBlockEntity extends AdvancedStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int OUTPUT = 3;
    public static final int RETURN = 4;
    public static final boolean NEEDS_HEAT = false;

    public CharmWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.CHARM_WORKBENCH.get(), pos, state, newInventory(), StationRecipes.CHARM_WORK, NEEDS_HEAT, true,
                (result, inv, reservation) -> result);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new CharmWorkbenchMenu(id, player, inventory, data, access);
    }
}
