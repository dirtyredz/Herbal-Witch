package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.wand_bench;

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
 * Wand bench contents: three ingredients and one result; each batch needs a witch to start it.
 */
public class WandBenchBlockEntity extends AdvancedStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int OUTPUT = 3;

    public static final boolean NEEDS_HEAT = false;

    public WandBenchBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.WAND_BENCH.get(), pos, state, newInventory(), StationRecipes.WAND_CARVING, NEEDS_HEAT, true,
                (result, inv, reservation) -> result);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.OUTPUT);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new WandBenchMenu(id, player, inventory, data, access);
    }
}
