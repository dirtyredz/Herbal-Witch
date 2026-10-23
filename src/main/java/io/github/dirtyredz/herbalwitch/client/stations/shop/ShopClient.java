package io.github.dirtyredz.herbalwitch.client.stations.shop;

import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlockEntities;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopMenus;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopPackets;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

/** Client wiring for commerce: the screen, the display renderer and the projection cache lifecycle. */
public final class ShopClient {
    private ShopClient() {
    }

    public static void setup() {
        MenuScreens.register(ShopMenus.MENU.get(), ShopScreen::new);
        ShopPackets.client(ShopClientState::accept);
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ShopClientState.clear());
    }

    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ShopBlockEntities.DISPLAY.get(), ShopDisplayRenderer::new);
    }
}
