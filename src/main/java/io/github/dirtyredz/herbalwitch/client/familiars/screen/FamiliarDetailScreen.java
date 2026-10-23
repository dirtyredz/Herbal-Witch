package io.github.dirtyredz.herbalwitch.client.familiars.screen;

import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarDetailMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FamiliarDetailScreen extends FamiliarRosterScreen<FamiliarDetailMenu> {
    public FamiliarDetailScreen(FamiliarDetailMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
}
