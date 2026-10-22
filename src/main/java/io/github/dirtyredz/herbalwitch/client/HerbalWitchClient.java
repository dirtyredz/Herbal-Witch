package io.github.dirtyredz.herbalwitch.client;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireScreens;
import io.github.dirtyredz.herbalwitch.client.stations.StationScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only wiring on the mod bus; each feature's client code lives in its own package. Never
 * loaded on a dedicated server. Render types come from the model JSON and colours from the
 * textures (nothing is tinted), so only screens (and the grimoire's client state) are registered here.
 */
@Mod.EventBusSubscriber(modid = HerbalWitch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HerbalWitchClient {
    private HerbalWitchClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(StationScreens::register);
        event.enqueueWork(GrimoireScreens::register);
    }
}
