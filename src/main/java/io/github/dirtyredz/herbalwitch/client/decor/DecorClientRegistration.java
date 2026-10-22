package io.github.dirtyredz.herbalwitch.client.decor;

import io.github.dirtyredz.herbalwitch.client.decor.placement.FurniturePlacementPreview;
import io.github.dirtyredz.herbalwitch.client.decor.storage.FurnitureDisplayRenderer;
import io.github.dirtyredz.herbalwitch.client.decor.storage.FurnitureStorageScreen;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlockEntities;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorEntities;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Decor's client wiring, called from {@code HerbalWitchClient}: the storage screen, the display
 * renderer that draws shelved items, and the seat entity, which draws nothing (a sitter appears
 * seated on the furniture's own model).
 */
public final class DecorClientRegistration {
    private DecorClientRegistration() {
    }

    /** Screens and the placement preview (client setup, on the main thread). */
    public static void screens() {
        MenuScreens.register(DecorMenus.STORAGE.get(), FurnitureStorageScreen::new);
        FurniturePlacementPreview.register(MinecraftForge.EVENT_BUS);
    }

    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DecorEntities.SEAT.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(DecorBlockEntities.STORAGE.get(), FurnitureDisplayRenderer::new);
    }
}
