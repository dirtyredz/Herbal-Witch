package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.block;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.DrinkMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.menu.KettleMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlockEntities;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kettle contents: two ingredients, one water bottle, the drink and a returned-container slot.
 * Steeping needs heat from below; the water bottle becomes the drink, carrying its water metadata.
 */
public class KettleBlockEntity extends ProcessStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int WATER = 2;
    public static final int OUTPUT = 3;
    public static final int RETURN = 4;
    public static final boolean NEEDS_HEAT = true;

    public KettleBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.KETTLE.get(), pos, state, newInventory(), StationRecipes.STEEPING, NEEDS_HEAT, false,
                (result, inv, reservation) -> DrinkMetadata.carry(inv.getStackInSlot(WATER), result));
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.BASE,
                ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new KettleMenu(id, player, inventory, data, access);
    }
}
