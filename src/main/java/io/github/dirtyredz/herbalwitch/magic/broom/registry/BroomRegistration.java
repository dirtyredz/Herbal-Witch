package io.github.dirtyredz.herbalwitch.magic.broom.registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import io.github.dirtyredz.herbalwitch.magic.broom.lifecycle.*;
import io.github.dirtyredz.herbalwitch.magic.broom.network.BroomPackets;
public final class BroomRegistration {
 private BroomRegistration(){}
 public static void register(IEventBus modBus){BroomEntities.TYPES.register(modBus);BroomPackets.register();var bus=MinecraftForge.EVENT_BUS;bus.addListener(BroomLifecycle::logout);bus.addListener(BroomLifecycle::death);bus.addListener(BroomLifecycle::dimension);bus.addListener(BroomLifecycle::stopped);bus.addListener(BroomDismount::fall);}
}
