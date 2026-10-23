package io.github.dirtyredz.herbalwitch.client.broom;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.*;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomEntities;
import io.github.dirtyredz.herbalwitch.magic.broom.network.BroomPackets;
public final class BroomClientRegistration {
 private BroomClientRegistration(){}
 public static void setup(){BroomPackets.client(BroomControls::accept);var bus=MinecraftForge.EVENT_BUS;bus.addListener(BroomControls::tick);bus.addListener((ClientPlayerNetworkEvent.LoggingOut e)->BroomControls.clear());bus.addListener((ClientPlayerNetworkEvent.Clone e)->BroomControls.clear());bus.addListener(BroomPlacementPreview::render);}
 public static void keys(RegisterKeyMappingsEvent event){event.register(BroomControls.DESCEND);event.register(BroomControls.LAND);}
 public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(BroomEntities.BROOM.get(),BroomRenderer::new);}
 public static void overlays(RegisterGuiOverlaysEvent event){event.registerAboveAll("broom",BroomHud.OVERLAY);}
}
