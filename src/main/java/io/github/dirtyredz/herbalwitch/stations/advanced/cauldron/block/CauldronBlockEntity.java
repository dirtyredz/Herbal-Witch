package io.github.dirtyredz.herbalwitch.stations.advanced.cauldron.block;

import io.github.dirtyredz.herbalwitch.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.stations.advanced.AdvancedStationEntity;
import io.github.dirtyredz.herbalwitch.stations.advanced.cauldron.menu.CauldronMenu;
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
 * Witch's cauldron contents: three ingredients, a water base, the mixture and the returned bottle. Brewing needs heat from below and a witch who starts each batch; the water's metadata rides into the mixture.
 */
public class CauldronBlockEntity extends AdvancedStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int BASE = 3;
    public static final int OUTPUT = 4;
    public static final int RETURN = 5;
    public static final boolean NEEDS_HEAT = true;

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.CAULDRON.get(), pos, state, newInventory(), StationRecipes.CAULDRON, NEEDS_HEAT, true,
                (result, inv, reservation) -> PreparationMetadata.carry(inv.getStackInSlot(BASE), result));
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.BASE, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new CauldronMenu(id, player, inventory, data, access);
    }
}
