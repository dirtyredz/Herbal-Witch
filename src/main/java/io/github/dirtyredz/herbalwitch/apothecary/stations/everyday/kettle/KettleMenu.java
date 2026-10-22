package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;

/** The station slots, drawn to match its generated GUI atlas. */
public class KettleMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(KettleBlockEntity.INPUT_0, 30, 26),
            new SlotSpec(KettleBlockEntity.INPUT_1, 48, 26),
            new SlotSpec(KettleBlockEntity.WATER, 39, 50),
            new SlotSpec(KettleBlockEntity.OUTPUT, 116, 35),
            new SlotSpec(KettleBlockEntity.RETURN, 116, 58)};

    public KettleMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.KETTLE.get(), id, player, inventory, data, access, StationBlocks.KETTLE.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public KettleMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, KettleBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }
}
