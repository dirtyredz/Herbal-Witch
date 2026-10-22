package io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block;

import io.github.dirtyredz.herbalwitch.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.menu.MortarMenu;
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
 * Mortar contents: three ingredient slots, the product and a returned-container slot. Ordinary grinding
 * starts by itself once a recipe is assembled (100 ticks); no heat. A witch recipe (blinding dust)
 * waits for its witch to start it.
 */
public class MortarBlockEntity extends ProcessStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int OUTPUT = 3;
    public static final int RETURN = 4;
    public static final boolean NEEDS_HEAT = false;

    public MortarBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.MORTAR.get(), pos, state, newInventory(), StationRecipes.GRINDING, NEEDS_HEAT, true);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT,
                ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new MortarMenu(id, player, inventory, data, access);
    }
}
