package io.github.dirtyredz.herbalwitch.companions.animals.registry;

import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalLifecycleEvents;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/** Feature-owned registration. Relationships are entity-local; later familiar bonds have a separate authority. */
public final class AnimalRegistration {
    private AnimalRegistration() {}

    public static void register(IEventBus bus) {
        AnimalServerConfig.register(bus);
        bus.addListener(AnimalLifecycleEvents::registerCapabilities);
        var forge = MinecraftForge.EVENT_BUS;
        forge.addGenericListener(Entity.class, AnimalLifecycleEvents::attach);
        forge.addListener(AnimalLifecycleEvents::tick);
        forge.addListener(EventPriority.LOWEST, AnimalLifecycleEvents::damaged);
        forge.addListener(AnimalServerConfig::tagsUpdated);
        forge.addListener((ServerStoppedEvent event) -> AnimalServerConfig.reset());
    }
}
