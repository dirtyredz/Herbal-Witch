package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
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
 * Infusion pot contents: two ingredients, a base liquid, a package, the product and returned
 * containers. Infusion needs heat from below (600 ticks); the bottle of the base comes back. Witch
 * oils (venom, thorn, repellents, emberguard) wait for their witch to start them.
 */
public class InfusionPotBlockEntity extends ProcessStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int BASE = 2;
    public static final int PACKAGING = 3;
    public static final int OUTPUT = 4;
    public static final int RETURN = 5;
    public static final boolean NEEDS_HEAT = true;

    public InfusionPotBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.INFUSION_POT.get(), pos, state, newInventory(), StationRecipes.INFUSION, NEEDS_HEAT, true);
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.BASE,
                ProcessInventory.Role.PACKAGING, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new InfusionPotMenu(id, player, inventory, data, access);
    }
}
