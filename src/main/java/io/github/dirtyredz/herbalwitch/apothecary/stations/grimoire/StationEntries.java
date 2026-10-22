package io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationItems;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * One public page per station: what it processes (from {@link ProcessingCategories}) and whether it
 * needs heat, or, for the hearth and stove, how they heat the vessel above them.
 */
public final class StationEntries implements GrimoireContentSource {
    private static final String PREFIX = "grimoire." + HerbalWitch.MOD_ID + ".station.";
    public static final String RUNS = PREFIX + "runs";
    public static final String NEEDS_HEAT = PREFIX + "needs_heat";
    public static final String NO_HEAT = PREFIX + "no_heat";
    public static final String HEAT_SOURCE = PREFIX + "heat_source";
    public static final String MULTIBLOCK = PREFIX + "multiblock";

    public static EntryKey key(StationSpec station) {
        return EntryKey.of(EntryKey.Kind.STATION, HerbalWitch.MOD_ID + ":" + station.path());
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        for (StationSpec station : StationCatalog.ALL) {
            List<Component> body = new ArrayList<>();
            List<EntryKey> links = new ArrayList<>();
            Optional<ProcessingCategories.Category> category = ProcessingCategories.byStation(station);
            category.ifPresentOrElse(c -> {
                body.add(Component.translatable(RUNS, Component.translatable(c.titleKey())));
                body.add(Component.translatable(c.heated() ? NEEDS_HEAT : NO_HEAT));
                if (c.heated()) {
                    links.add(key(StationCatalog.HEARTH));
                    links.add(key(StationCatalog.STOVE));
                }
            }, () -> {
                body.add(Component.translatable(HEAT_SOURCE));
                links.add(key(StationCatalog.KETTLE));
                links.add(key(StationCatalog.INFUSION_POT));
            });
            if (station.layout() != null) {
                body.add(Component.translatable(MULTIBLOCK, station.layout().size()));
            }
            links.add(GuideEntries.key("heat"));
            out.accept(new GrimoireEntry(key(station), GrimoireContent.STATIONS,
                    StationBlocks.block(station).getName(), StationItems.item(station).getDefaultInstance(), body, links,
                    Optional.empty(), true, Optional.empty()));
        }
    }
}
