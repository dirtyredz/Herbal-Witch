package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot;

import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot.InfusionPotBlockEntity;
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
public class InfusionPotMenu extends StationMenu {
    public static final SlotSpec[] SLOTS = {
            new SlotSpec(InfusionPotBlockEntity.INPUT_0, 30, 17),
            new SlotSpec(InfusionPotBlockEntity.INPUT_1, 48, 17),
            new SlotSpec(InfusionPotBlockEntity.BASE, 30, 53),
            new SlotSpec(InfusionPotBlockEntity.PACKAGING, 48, 53),
            new SlotSpec(InfusionPotBlockEntity.OUTPUT, 116, 35),
            new SlotSpec(InfusionPotBlockEntity.RETURN, 116, 58)};

    public InfusionPotMenu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(StationMenus.INFUSION_POT.get(), id, player, inventory, data, access, StationBlocks.INFUSION_POT.get(), SLOTS);
    }

    /** Client side: a mirror inventory filled by vanilla slot synchronisation. */
    public InfusionPotMenu(int id, Inventory player, FriendlyByteBuf extra) {
        this(id, player, InfusionPotBlockEntity.newInventory(), new SimpleContainerData(ProcessStation.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }
}
