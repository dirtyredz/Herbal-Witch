package io.github.dirtyredz.herbalwitch.client.botany;

import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

/**
 * The herb bed's frame uses the shared neutral wood texture, tinted (index 0) in the base dark oak
 * finish's frame colour, so it matches the furniture. Soil and plants are untinted.
 */
public final class BotanyColorHandlers {
    private BotanyColorHandlers() {
    }

    static int color(int tintIndex) {
        return tintIndex == 0 ? FinishCatalog.BASE.frameColor() : -1;
    }

    public static void registerBlocks(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> color(tint), BotanyBlocks.herbBed());
    }

    public static void registerItems(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> color(tint), BotanyBlocks.herbBed());
    }
}
