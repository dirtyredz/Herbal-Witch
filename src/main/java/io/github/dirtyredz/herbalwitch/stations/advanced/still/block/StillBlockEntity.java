package io.github.dirtyredz.herbalwitch.stations.advanced.still.block;

import io.github.dirtyredz.herbalwitch.stations.advanced.AdvancedStationEntity;
import io.github.dirtyredz.herbalwitch.stations.advanced.still.menu.StillMenu;
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
 * Copper still contents: two ingredients, a base liquid, a bottle to fill, the distillate and the returned container. Distilling needs heat below the boiler and a witch who starts each batch; distillation never improves water purity.
 */
public class StillBlockEntity extends AdvancedStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int BASE = 2;
    public static final int PACKAGING = 3;
    public static final int OUTPUT = 4;
    public static final int RETURN = 5;
    public static final boolean NEEDS_HEAT = true;

    public StillBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.STILL.get(), pos, state, newInventory(), StationRecipes.DISTILLATION, NEEDS_HEAT, true,
                (result, inv, reservation) -> result);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.BASE, ProcessInventory.Role.PACKAGING, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new StillMenu(id, player, inventory, data, access);
    }
}
