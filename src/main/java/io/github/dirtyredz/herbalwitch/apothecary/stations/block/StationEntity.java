package io.github.dirtyredz.herbalwitch.apothecary.stations.block;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import net.minecraft.world.MenuProvider;

/** What {@link StationBlock} needs from its block entity: a server tick, a menu, its inventory and dropping its contents. */
public interface StationEntity extends MenuProvider {
    void serverTick();

    /** The authoritative inventory (menus, automation and the showcase all go through it). */
    ProcessInventory inventory();

    /** Drops each stored stack once; called when the block is removed. */
    void dropContents();
}
