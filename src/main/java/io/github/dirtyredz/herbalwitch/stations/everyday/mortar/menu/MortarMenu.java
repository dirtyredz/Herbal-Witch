package io.github.dirtyredz.herbalwitch.stations.everyday.mortar.menu;

import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.stations.registry.StationMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;

/** The station slots, drawn to match its generated GUI atlas. */
public class MortarMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(MortarBlockEntity.INPUT_0, 30, 35),
            new SlotSpec(MortarBlockEntity.INPUT_1, 48, 35),
            new SlotSpec(MortarBlockEntity.INPUT_2, 66, 35),
            new SlotSpec(MortarBlockEntity.OUTPUT, 124, 35),
            new SlotSpec(MortarBlockEntity.RETURN, 124, 58)};

    public MortarMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.MORTAR.get(), id, player, inventory, data, access, StationBlocks.MORTAR.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public MortarMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, MortarBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }
}
