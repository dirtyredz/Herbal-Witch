package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.menu;

import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.block.CauldronBlockEntity;
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
public class CauldronMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(CauldronBlockEntity.INPUT_0, 30, 17),
            new SlotSpec(CauldronBlockEntity.INPUT_1, 48, 17),
            new SlotSpec(CauldronBlockEntity.INPUT_2, 66, 17),
            new SlotSpec(CauldronBlockEntity.BASE, 48, 53),
            new SlotSpec(CauldronBlockEntity.OUTPUT, 124, 35),
            new SlotSpec(CauldronBlockEntity.RETURN, 124, 58)};

    public CauldronMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.CAULDRON.get(), id, player, inventory, data, access, StationBlocks.CAULDRON.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public CauldronMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, CauldronBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT), ContainerLevelAccess.NULL);
    }
}
