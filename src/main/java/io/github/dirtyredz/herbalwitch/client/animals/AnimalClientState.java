package io.github.dirtyredz.herbalwitch.client.animals;

import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireClientState;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalPackets;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalSnapshot;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

/** Only the current card's private snapshot survives; menus and disconnects invalidate it. */
public final class AnimalClientState {
    private static AnimalSnapshot current;
    private AnimalClientState() {}
    public static void setup() {
        MenuScreens.register(AnimalMenus.CARD.get(), AnimalScreen::new);
        AnimalPackets.client(AnimalClientState::receive);
        io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig.listen(
                () -> Minecraft.getInstance().execute(() -> { current = null; GrimoireClientState.INSTANCE.invalidate(); }));
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> current = null);
    }
    private static void receive(AnimalSnapshot snapshot) {
        var player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof AnimalMenu menu) || !matches(menu, snapshot)) return;
        if (current == null || !matches(menu, current) || snapshot.revision() > current.revision()) current = snapshot;
    }
    public static AnimalSnapshot get(AnimalMenu menu) { return current != null && matches(menu, current) ? current : null; }
    public static void clear() { current = null; }
    private static boolean matches(AnimalMenu menu, AnimalSnapshot snapshot) {
        return snapshot.menu() == menu.containerId && snapshot.nonce() == menu.nonce() && snapshot.target().equals(menu.target());
    }
}
