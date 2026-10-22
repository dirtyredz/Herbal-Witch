package io.github.dirtyredz.herbalwitch.client.witch;

import io.github.dirtyredz.herbalwitch.client.witch.hud.EnergyOverlay;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchPackets;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;

/**
 * The witch feature's client wiring: the snapshot and rules receiver, the energy HUD overlay, and
 * the resets that keep the cache honest (disconnecting forgets the server; a replaced local player,
 * as after respawning, forgets the old snapshot until the server's new one arrives).
 */
public final class WitchClientEvents {
    private static final EnergyOverlay OVERLAY = new EnergyOverlay();

    private WitchClientEvents() {
    }

    /** Client setup (main thread). */
    public static void setup() {
        WitchPackets.installClient(WitchClientState.INSTANCE);
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            WitchClientState.INSTANCE.disconnected();
            OVERLAY.reset();
        });
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.Clone event) -> {
            WitchClientState.INSTANCE.reset();
            OVERLAY.reset();
        });
    }

    /** Mod bus: the HUD sits above the hotbar layer. */
    public static void overlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), EnergyOverlay.ID, OVERLAY);
    }
}
