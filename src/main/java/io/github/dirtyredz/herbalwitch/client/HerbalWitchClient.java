package io.github.dirtyredz.herbalwitch.client;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.brewing.PreparationRenderers;
import io.github.dirtyredz.herbalwitch.client.decor.DecorClientRegistration;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireScreens;
import io.github.dirtyredz.herbalwitch.client.placement.PlacementPreviewRenderer;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.AltarScreens;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.CirclePlacementPreview;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.RitualTargets;
import io.github.dirtyredz.herbalwitch.client.stations.StationScreens;
import io.github.dirtyredz.herbalwitch.client.magic.witch.WitchClientConfig;
import io.github.dirtyredz.herbalwitch.client.magic.ward.WardBoundaryPreview;
import io.github.dirtyredz.herbalwitch.client.magic.witch.WitchClientEvents;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only wiring on the mod bus; each feature's client code lives in its own package. Never
 * loaded on a dedicated server. Render types come from the model JSON and colours from the
 * textures (nothing is tinted), so only screens, client state, the shared placement preview and
 * the energy HUD are registered here.
 */
@Mod.EventBusSubscriber(modid = HerbalWitch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HerbalWitchClient {
    private HerbalWitchClient() {
    }

    /** Client configuration (the HUD), registered from the mod constructor's client-only branch. */
    public static void registerConfig() {
        WitchClientConfig.register();
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(StationScreens::register);
        event.enqueueWork(GrimoireScreens::register);
        event.enqueueWork(DecorClientRegistration::screens);
        event.enqueueWork(AltarScreens::register);
        event.enqueueWork(WitchClientEvents::setup);
        event.enqueueWork(() -> PlacementPreviewRenderer.register(MinecraftForge.EVENT_BUS));
        event.enqueueWork(() -> CirclePlacementPreview.register(MinecraftForge.EVENT_BUS));
        event.enqueueWork(() -> RitualTargets.register(MinecraftForge.EVENT_BUS));
        event.enqueueWork(() -> WardBoundaryPreview.register(MinecraftForge.EVENT_BUS, WardRegistration.MENU.get()));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        DecorClientRegistration.renderers(event);
        PreparationRenderers.renderers(event);
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent event) {
        WitchClientEvents.overlays(event);
    }
}
