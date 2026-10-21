package io.github.dirtyredz.herbalwitch.stations.catalog;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;

import java.util.List;

/** The everyday stations (docs/catalogue/stations.md): Milestone 3's five and the witch's stove (0.2.0). */
public final class StationCatalog {
    public static final StationSpec DRYING_RACK = new StationSpec("drying_rack", "Herb Drying Rack", true,
            MultiblockLayout.rectangle(2, 2));
    public static final StationSpec MORTAR = new StationSpec("mortar", "Mortar and Pestle", false);
    public static final StationSpec KETTLE = new StationSpec("kettle", "Tea Kettle", false);
    public static final StationSpec HEARTH = new StationSpec("hearth", "Hearth", true, MultiblockLayout.rectangle(2, 2));
    public static final StationSpec STOVE = new StationSpec("stove", "Witch's Stove", true, MultiblockLayout.rectangle(2, 1));
    public static final StationSpec INFUSION_POT = new StationSpec("infusion_pot", "Infusion Pot", false);

    /** Creative-tab and showcase order. */
    public static final List<StationSpec> ALL = List.of(DRYING_RACK, MORTAR, KETTLE, HEARTH, STOVE, INFUSION_POT);

    private StationCatalog() {
    }
}
