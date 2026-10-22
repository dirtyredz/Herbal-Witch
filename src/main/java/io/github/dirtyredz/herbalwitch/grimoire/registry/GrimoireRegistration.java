package io.github.dirtyredz.herbalwitch.grimoire.registry;

import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.recipe.StationRecipeEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.HerbEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.DecorEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.StationEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.WitchEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryEvents;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching.TeachingEvents;
import io.github.dirtyredz.herbalwitch.grimoire.network.ContentSync;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderRequest;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingRequest;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ReaderService;
import io.github.dirtyredz.herbalwitch.grimoire.showcase.GrimoireShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.stations.registry.StationCreativeTab;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Wires the grimoire: the book, the research blocks and the reader menu; the discovery capability
 * and its lifecycle; editorial reloads, content sources and content sync; the network messages;
 * reading, research and teaching on the server; the Apothecary tab contribution (through the tab
 * event, so stations never depend on the grimoire); and the showcase section.
 */
public final class GrimoireRegistration {
    private GrimoireRegistration() {
    }

    /** Called once from the mod constructor. */
    public static void register(IEventBus modBus) {
        GrimoireBlocks.BLOCKS.register(modBus);
        GrimoireItems.ITEMS.register(modBus);
        GrimoireMenus.MENUS.register(modBus);
        modBus.addListener(DiscoveryEvents::registerCapabilities);
        modBus.addListener(GrimoireRegistration::creativeTab);

        GrimoireContent.register(new GuideEntries());
        GrimoireContent.register(new WitchEntries());
        GrimoireContent.register(new StationEntries());
        GrimoireContent.register(new DecorEntries());
        GrimoireContent.register(new HerbEntries());
        GrimoireContent.register(new StationRecipeEntries());
        ServerGrimoireContent.listen(TeachingEvents::contentChanged);

        GrimoirePackets.register(new GrimoirePackets.ServerHandlers() {
            @Override
            public void reader(ServerPlayer sender, ReaderRequest request) {
                ReaderService.handle(sender, request);
            }

            @Override
            public void teaching(ServerPlayer sender, TeachingRequest request) {
                TeachingEvents.handle(sender, request);
            }
        });
        ReaderService.onOpened(TeachingEvents::readerOpened);

        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addGenericListener(Entity.class, DiscoveryEvents::attach);
        forge.addListener(DiscoveryEvents::clone);
        forge.addListener(DiscoveryEvents::loggedIn);
        forge.addListener(DiscoveryEvents::respawned);
        forge.addListener(DiscoveryEvents::changedDimension);
        forge.addListener(DiscoveryEvents::playerTick);
        forge.addListener(ReaderService::playerTick);
        forge.addListener(GrimoireDefinitionLoader::onAddReloadListeners);
        forge.addListener(ContentSync::onDatapackSync);
        forge.addListener(TeachingEvents::serverStarted);
        forge.addListener(TeachingEvents::serverStopped);
        forge.addListener(TeachingEvents::serverTick);
        forge.addListener(TeachingEvents::loggedOut);
        forge.addListener(TeachingEvents::died);
        forge.addListener(TeachingEvents::changedDimension);
        forge.addListener((ServerStoppedEvent event) -> ServerGrimoireContent.reset());

        ShowcaseRegistry.register(new GrimoireShowcaseSection());
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) {
            GrimoireItems.all().forEach(event::accept);
        }
    }
}
