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
        AnimalItems.ITEMS.register(bus);
        io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry.register(new io.github.dirtyredz.herbalwitch.companions.animals.showcase.AnimalShowcaseSection());
        AnimalMenus.MENUS.register(bus);
        bus.addListener(AnimalRegistration::creative);
        io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent.register(
                io.github.dirtyredz.herbalwitch.companions.animals.grimoire.AnimalEntries.PRIORITY,
                new io.github.dirtyredz.herbalwitch.companions.animals.grimoire.AnimalEntries());
        io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.register(
                io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalPermissionPolicy::check);
        io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalPackets.register();
        AnimalServerConfig.register(bus);
        bus.addListener(AnimalLifecycleEvents::registerCapabilities);
        var forge = MinecraftForge.EVENT_BUS;
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.capture.command.CaptureCommands::register);
        forge.addListener(EventPriority.HIGHEST, io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureReconciliation::join);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureReconciliation::leave);
        forge.addListener((ServerStoppedEvent event) -> {
            io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureReconciliation.reset();
            io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureSavedData.reset();
        });
        forge.addGenericListener(Entity.class, AnimalLifecycleEvents::attach);
        forge.addListener(AnimalLifecycleEvents::tick);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalGoalInstaller::tick);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalGoalInstaller::levelTick);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalGoalInstaller::leave);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.ai.BaitInterestIndex::tick);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.ai.BaitInterestIndex::logout);
        forge.addListener(EventPriority.LOWEST, AnimalLifecycleEvents::damaged);
        forge.addListener(AnimalServerConfig::tagsUpdated);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents::interact);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents::logout);
        forge.addListener(io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu::tick);
        forge.addListener((ServerStoppedEvent event) -> AnimalServerConfig.reset());
        forge.addListener((ServerStoppedEvent event) -> io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalGoalInstaller.reset());
        forge.addListener((ServerStoppedEvent event) -> io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents.reset());
    }

    private static void creative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab.APOTHECARY.getKey()))
            AnimalItems.all().forEach(event::accept);
    }
}
