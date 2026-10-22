package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench;

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
public class BottlingBenchMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(BottlingBenchBlockEntity.INPUT_0, 30, 17),
            new SlotSpec(BottlingBenchBlockEntity.INPUT_1, 48, 17),
            new SlotSpec(BottlingBenchBlockEntity.INPUT_2, 66, 17),
            new SlotSpec(BottlingBenchBlockEntity.PACKAGING, 48, 53),
            new SlotSpec(BottlingBenchBlockEntity.OUTPUT, 124, 35),
            new SlotSpec(BottlingBenchBlockEntity.RETURN, 124, 58)};

    public BottlingBenchMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.BOTTLING_BENCH.get(), id, player, inventory, data, access, StationBlocks.BOTTLING_BENCH.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public BottlingBenchMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, BottlingBenchBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT), ContainerLevelAccess.NULL);
    }
}
