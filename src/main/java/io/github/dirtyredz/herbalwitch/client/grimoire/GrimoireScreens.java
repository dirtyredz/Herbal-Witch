package io.github.dirtyredz.herbalwitch.client.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;

/**
 * Client wiring of the grimoire: the reader screen, the client state as the receiver of grimoire
 * messages, and the resets that keep it honest (recipe and tag updates rebuild the content,
 * disconnecting forgets everything).
 */
public final class GrimoireScreens {
    private GrimoireScreens() {
    }

    /** Call from client setup (enqueued on the main thread). */
    public static void register() {
        MenuScreens.register(GrimoireMenus.READER.get(), GrimoireScreen::new);
        GrimoirePackets.installClient(GrimoireClientState.INSTANCE);
        MinecraftForge.EVENT_BUS.addListener(GrimoireClientState::onRecipesUpdated);
        MinecraftForge.EVENT_BUS.addListener(GrimoireClientState::onTagsUpdated);
        MinecraftForge.EVENT_BUS.addListener(GrimoireClientState::onLoggingOut);
    }
}
