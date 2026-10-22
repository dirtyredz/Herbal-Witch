package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench;

import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench.CharmWorkbenchBlockEntity;
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

/** The station slots, drawn to match its generated GUI atlas (tools/art/stations/interfaces.mjs). */
public class CharmWorkbenchMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(CharmWorkbenchBlockEntity.INPUT_0, 30, 26),
            new SlotSpec(CharmWorkbenchBlockEntity.INPUT_1, 48, 26),
            new SlotSpec(CharmWorkbenchBlockEntity.INPUT_2, 66, 26),
            new SlotSpec(CharmWorkbenchBlockEntity.OUTPUT, 124, 35),
            new SlotSpec(CharmWorkbenchBlockEntity.RETURN, 124, 58)};

    public CharmWorkbenchMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.CHARM_WORKBENCH.get(), id, player, inventory, data, access, StationBlocks.CHARM_WORKBENCH.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public CharmWorkbenchMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, CharmWorkbenchBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT), ContainerLevelAccess.NULL);
    }
}
