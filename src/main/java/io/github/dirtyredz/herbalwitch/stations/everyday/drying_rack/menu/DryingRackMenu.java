package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.menu;

import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlockEntity;
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

/** Fresh herb in, bundle out. */
public class DryingRackMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(DryingRackBlockEntity.INPUT, 56, 35),
            new SlotSpec(DryingRackBlockEntity.OUTPUT, 116, 35)};

    public DryingRackMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.DRYING_RACK.get(), id, player, inventory, data, access, StationBlocks.DRYING_RACK.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public DryingRackMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, DryingRackBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }
}
