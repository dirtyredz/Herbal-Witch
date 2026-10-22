package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.menu;

import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

/** Fuel and the returned container, with remaining burn time: the hearth's and the stove's menu. */
public class HearthMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(HearthBlockEntity.FUEL, 80, 44),
            new SlotSpec(HearthBlockEntity.RETURN, 116, 44)};

    /** @param block the hearth or stove the menu belongs to (the distance and block check) */
    public HearthMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access,
                      Block block) {
        super(StationMenus.HEARTH.get(), id, player, inventory, data, access, block, SLOTS);
    }

    public HearthMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, HearthBlockEntity.newInventory(), new SimpleContainerData(HearthBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL, StationBlocks.HEARTH.get());
    }
}
