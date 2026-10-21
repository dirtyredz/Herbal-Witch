package io.github.dirtyredz.herbalwitch.stations.everyday.hearth.menu;

import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.stations.registry.StationMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;

/** Fuel and the returned container, with remaining burn time. */
public class HearthMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(HearthBlockEntity.FUEL, 80, 44),
            new SlotSpec(HearthBlockEntity.RETURN, 116, 44)};

    public HearthMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.HEARTH.get(), id, player, inventory, data, access, StationBlocks.HEARTH.get(), SLOTS);
    }

    public HearthMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, HearthBlockEntity.newInventory(), new SimpleContainerData(HearthBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }
}
