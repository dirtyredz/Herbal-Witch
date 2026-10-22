package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.menu;

import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.block.StillBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;

/** The station slots, drawn to match its generated GUI atlas (tools/art/stations/interfaces.mjs). */
public class StillMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(StillBlockEntity.INPUT_0, 30, 17),
            new SlotSpec(StillBlockEntity.INPUT_1, 48, 17),
            new SlotSpec(StillBlockEntity.BASE, 30, 53),
            new SlotSpec(StillBlockEntity.PACKAGING, 48, 53),
            new SlotSpec(StillBlockEntity.OUTPUT, 116, 35),
            new SlotSpec(StillBlockEntity.RETURN, 116, 58)};

    public StillMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.STILL.get(), id, player, inventory, data, access, StationBlocks.STILL.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public StillMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, StillBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT), ContainerLevelAccess.NULL);
    }
}
