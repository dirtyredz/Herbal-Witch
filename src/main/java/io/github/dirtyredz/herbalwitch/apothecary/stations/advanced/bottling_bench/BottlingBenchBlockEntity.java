package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.PreparedMixtureItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.AdvancedStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.BottlingBenchMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlockEntities;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bottling bench contents: up to three ingredients (a mixture, gunpowder for vials, paper for a label), the glass to fill, the bottled doses and a return slot. Ordinary work: no heat, no witch, hoppers welcome; the mixture's water metadata rides into every bottle.
 */
public class BottlingBenchBlockEntity extends AdvancedStationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int PACKAGING = 3;
    public static final int OUTPUT = 4;
    public static final int RETURN = 5;
    public static final boolean NEEDS_HEAT = false;

    public BottlingBenchBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.BOTTLING_BENCH.get(), pos, state, newInventory(), StationRecipes.BOTTLING, NEEDS_HEAT, false,
                (result, inv, reservation) -> PreparationMetadata.carry(mixture(inv), result));
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.PACKAGING, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
    }

    /** The mixture being bottled (the input holding a mixture item), or empty. */
    private static ItemStack mixture(ProcessInventory inventory) {
        for (int slot : new int[] {INPUT_0, INPUT_1, INPUT_2}) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof PreparedMixtureItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        return new BottlingBenchMenu(id, player, inventory, data, access);
    }
}
