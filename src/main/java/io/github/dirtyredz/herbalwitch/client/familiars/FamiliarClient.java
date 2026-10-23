package io.github.dirtyredz.herbalwitch.client.familiars;

import io.github.dirtyredz.herbalwitch.client.familiars.screen.*;
import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarClientState;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarPackets;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

public final class FamiliarClient {
    private FamiliarClient() {}
    public static void setup() {
        MenuScreens.<io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu,
                FamiliarRosterScreen<io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu>>register(
                FamiliarMenus.ROSTER.get(), FamiliarRosterScreen::new);
        MenuScreens.register(FamiliarMenus.DETAIL.get(), FamiliarDetailScreen::new);
        FamiliarPackets.client(FamiliarClientState::accept);
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> FamiliarClientState.clear());
    }
}
