package io.github.dirtyredz.herbalwitch.magic.charm.registry;

import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.CharmEvents;
import io.github.dirtyredz.herbalwitch.magic.charm.grimoire.CharmEntries;
import io.github.dirtyredz.herbalwitch.magic.charm.showcase.CharmShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Wires the protective charms (Milestone 7): the items and their Apothecary tab entries, charge
 * draining and damage softening, and the showcase section. Curios support is optional
 * ({@code platform/compat/curios}) and installs itself only when Curios is present.
 */
public final class CharmRegistration {
    private CharmRegistration() {
    }

    public static void register(IEventBus modBus) {
        CharmItems.ITEMS.register(modBus);
        modBus.addListener(CharmRegistration::creativeTab);
        MinecraftForge.EVENT_BUS.addListener(CharmEvents::playerTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, CharmEvents::onDamage);
        GrimoireContent.register(new CharmEntries());
        ShowcaseRegistry.register(new CharmShowcaseSection());
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) {
            CharmItems.all().forEach(event::accept);
        }
    }
}
