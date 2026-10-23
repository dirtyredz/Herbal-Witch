package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand;

import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.AdvancedStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlockEntities;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Broom stand contents: three ingredients, one result and the reserved broom to repair.
 */
public class BroomStandBlockEntity extends AdvancedStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int OUTPUT = 3;
    public static final int BASE = 4;
    public static final boolean NEEDS_HEAT = false;

    public BroomStandBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.BROOM_STAND.get(), pos, state, newInventory(), StationRecipes.BROOM_WORK, NEEDS_HEAT, true,
                (result, inv, reservation) -> result);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.BASE);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new BroomStandMenu(id, player, inventory, new ContainerData() {
            public int get(int index) { return index < data.getCount() ? data.get(index) : station().job().recipe() instanceof BroomWorkRecipe recipe ? recipe.repair() : 0; }
            public void set(int index, int value) { if (index < data.getCount()) data.set(index, value); }
            public int getCount() { return data.getCount() + 1; }
        }, access);
    }
}
