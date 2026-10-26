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
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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
    private static final String PONDER_MOD_ID = "ponder";

    private HerbalWitchClient() {
    }

    /** Client configuration (the HUD), registered from the mod constructor's client-only branch. */
    public static void registerConfig() {
        WitchClientConfig.register();
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(io.github.dirtyredz.herbalwitch.client.broom.BroomClientRegistration::setup);
        event.enqueueWork(io.github.dirtyredz.herbalwitch.client.wand.WandClientRegistration::setup);
        event.enqueueWork(StationScreens::register);
        event.enqueueWork(GrimoireScreens::register);
        event.enqueueWork(io.github.dirtyredz.herbalwitch.client.animals.AnimalClientState::setup);
        event.enqueueWork(io.github.dirtyredz.herbalwitch.client.familiars.FamiliarClient::setup);
        event.enqueueWork(DecorClientRegistration::screens);
        event.enqueueWork(AltarScreens::register);
        event.enqueueWork(io.github.dirtyredz.herbalwitch.client.stations.shop.ShopClient::setup);
        event.enqueueWork(WitchClientEvents::setup);
        event.enqueueWork(() -> PlacementPreviewRenderer.register(MinecraftForge.EVENT_BUS));
        event.enqueueWork(() -> CirclePlacementPreview.register(MinecraftForge.EVENT_BUS));
        event.enqueueWork(() -> RitualTargets.register(MinecraftForge.EVENT_BUS));
        event.enqueueWork(() -> WardBoundaryPreview.register(MinecraftForge.EVENT_BUS, WardRegistration.MENU.get()));
        // Optional Ponder lesson: the integration package is touched only when Ponder is loaded, so no Ponder,
        // Catnip or Flywheel class is resolved without it (the guide then simply shows no Watch link).
        if (ModList.get().isLoaded(PONDER_MOD_ID)) {
            event.enqueueWork(io.github.dirtyredz.herbalwitch.client.grimoire.ponder.PonderIntegration::setup);
        }
    }

    @SubscribeEvent
    public static void guide(RegisterClientReloadListenersEvent event) {
        // All guide definitions must exist before the first resource reload starts preparing pages.
        io.github.dirtyredz.herbalwitch.client.grimoire.guide.GuideClient.setup();
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        io.github.dirtyredz.herbalwitch.client.broom.BroomClientRegistration.renderers(event);
        DecorClientRegistration.renderers(event);
        PreparationRenderers.renderers(event);
        io.github.dirtyredz.herbalwitch.client.stations.shop.ShopClient.renderers(event);
        io.github.dirtyredz.herbalwitch.client.familiars.render.FamiliarNativeRenderers.register(event);
        io.github.dirtyredz.herbalwitch.client.familiars.render.FamiliarRenderers.register(event);
    }

    @SubscribeEvent
    public static void toolKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) { io.github.dirtyredz.herbalwitch.client.broom.BroomClientRegistration.keys(event); io.github.dirtyredz.herbalwitch.client.wand.WandClientRegistration.keys(event); }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent event) {
        io.github.dirtyredz.herbalwitch.client.broom.BroomClientRegistration.overlays(event);
        io.github.dirtyredz.herbalwitch.client.wand.WandClientRegistration.overlays(event);
        WitchClientEvents.overlays(event);
    }
}
