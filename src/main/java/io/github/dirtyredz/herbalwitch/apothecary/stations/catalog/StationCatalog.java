package io.github.dirtyredz.herbalwitch.apothecary.stations.catalog;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;

import java.util.List;

/**
 * The stations (docs/catalogue/stations.md): Milestone 3's five everyday stations and the witch's stove
 * (0.2.0), then Milestone 7's four production stations (cauldron, still, bottling bench, charm
 * workbench), which show an active model while they work.
 */
public final class StationCatalog {
    public static final StationSpec DRYING_RACK = new StationSpec("drying_rack", "Herb Drying Rack", true,
            MultiblockLayout.rectangle(2, 2));
    public static final StationSpec MORTAR = new StationSpec("mortar", "Mortar and Pestle", false);
    public static final StationSpec KETTLE = new StationSpec("kettle", "Tea Kettle", false);
    public static final StationSpec HEARTH = new StationSpec("hearth", "Hearth", true, MultiblockLayout.rectangle(2, 2));
    public static final StationSpec STOVE = new StationSpec("stove", "Witch's Stove", true, MultiblockLayout.rectangle(2, 1));
    public static final StationSpec INFUSION_POT = new StationSpec("infusion_pot", "Infusion Pot", false);
    public static final StationSpec CAULDRON = new StationSpec("cauldron", "Witch's Cauldron", true);
    /** One wide, one deep, two high: the boiler below, the condenser above. */
    public static final StationSpec STILL = new StationSpec("still", "Copper Still", true, MultiblockLayout.rectangle(1, 2));
    public static final StationSpec BOTTLING_BENCH = new StationSpec("bottling_bench", "Bottling Bench", true,
            MultiblockLayout.rectangle(2, 1));
    public static final StationSpec CHARM_WORKBENCH = new StationSpec("charm_workbench", "Charm Workbench", true,
            MultiblockLayout.rectangle(2, 1));

    /** The everyday stations (Milestone 3 and the stove). */
    public static final List<StationSpec> EVERYDAY = List.of(DRYING_RACK, MORTAR, KETTLE, HEARTH, STOVE, INFUSION_POT);
    /** The production stations (Milestone 7). */
    public static final List<StationSpec> ADVANCED = List.of(CAULDRON, STILL, BOTTLING_BENCH, CHARM_WORKBENCH);

    /** Creative-tab and showcase order. */
    public static final List<StationSpec> ALL = List.of(DRYING_RACK, MORTAR, KETTLE, HEARTH, STOVE, INFUSION_POT, CAULDRON, STILL,
            BOTTLING_BENCH, CHARM_WORKBENCH);

    private StationCatalog() {
    }
}
