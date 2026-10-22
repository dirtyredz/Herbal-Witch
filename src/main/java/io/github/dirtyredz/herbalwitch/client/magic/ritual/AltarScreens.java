package io.github.dirtyredz.herbalwitch.client.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarPackets;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Optional;

/**
 * The altar's client wiring and its one piece of client state: the screen for the altar menu, and
 * the latest {@link AltarUpdate} the server sent for the open altar menu (kept only for that menu and
 * session, cleared on disconnect).
 */
public final class AltarScreens {
    private static AltarUpdate latest;
    private static int generation;

    private AltarScreens() {
    }

    /** Client setup (main thread). */
    public static void register() {
        MenuScreens.register(RitualMenus.ALTAR.get(), AltarScreen::new);
        AltarPackets.installClient(AltarScreens::received);
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            latest = null;
            generation++;
        });
    }

    private static void received(AltarUpdate update) {
        latest = update;
        generation++;
    }

    /** The update for this exact menu and session, if one arrived. */
    public static Optional<AltarUpdate> latest(int containerId, long session) {
        return Optional.ofNullable(latest).filter(u -> u.containerId() == containerId && u.session() == session);
    }

    /** Bumped on every update, so the open screen knows to rebuild. */
    public static int generation() {
        return generation;
    }
}
