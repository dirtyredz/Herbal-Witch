package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand;

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

/** The station slots, drawn to match its generated GUI atlas (tools/art/interface/stations.mjs). */
public class BroomStandMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(BroomStandBlockEntity.INPUT_0, 30, 26),
            new SlotSpec(BroomStandBlockEntity.INPUT_1, 48, 26),
            new SlotSpec(BroomStandBlockEntity.INPUT_2, 66, 26),
            new SlotSpec(BroomStandBlockEntity.OUTPUT, 124, 35),
            new SlotSpec(BroomStandBlockEntity.BASE, 48, 58)};

    public BroomStandMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.BROOM_STAND.get(), id, player, inventory, data, access, StationBlocks.BROOM_STAND.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public BroomStandMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, BroomStandBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT + 1), ContainerLevelAccess.NULL);
    }
}
