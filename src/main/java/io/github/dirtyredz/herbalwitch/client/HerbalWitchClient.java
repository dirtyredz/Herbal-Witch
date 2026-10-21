package io.github.dirtyredz.herbalwitch.client;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.decor.DecorColorHandlers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only wiring on the mod bus; each feature's client code lives in its own package. Never
 * loaded on a dedicated server. Render types come from the model JSON, so none are set here.
 */
@Mod.EventBusSubscriber(modid = HerbalWitch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HerbalWitchClient {
    private HerbalWitchClient() {
    }

    @SubscribeEvent
    public static void blockColors(RegisterColorHandlersEvent.Block event) {
        DecorColorHandlers.registerBlocks(event);
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        DecorColorHandlers.registerItems(event);
    }
}
