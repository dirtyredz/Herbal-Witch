package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.wand_bench;

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
public class WandBenchMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(WandBenchBlockEntity.INPUT_0, 30, 26),
            new SlotSpec(WandBenchBlockEntity.INPUT_1, 48, 26),
            new SlotSpec(WandBenchBlockEntity.INPUT_2, 66, 26),
            new SlotSpec(WandBenchBlockEntity.OUTPUT, 124, 35)};

    public WandBenchMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.WAND_BENCH.get(), id, player, inventory, data, access, StationBlocks.WAND_BENCH.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public WandBenchMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, WandBenchBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT), ContainerLevelAccess.NULL);
    }
}
