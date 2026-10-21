package io.github.dirtyredz.herbalwitch.stations.catalog;

import java.util.List;

/** The five everyday stations of Milestone 3 (docs/catalogue/stations.md). */
public final class StationCatalog {
    public static final StationSpec DRYING_RACK = new StationSpec("drying_rack", "Herb Drying Rack", true);
    public static final StationSpec MORTAR = new StationSpec("mortar", "Mortar and Pestle", false);
    public static final StationSpec KETTLE = new StationSpec("kettle", "Tea Kettle", false);
    public static final StationSpec HEARTH = new StationSpec("hearth", "Hearth", true);
    public static final StationSpec INFUSION_POT = new StationSpec("infusion_pot", "Infusion Pot", false);

    /** Creative-tab and showcase order. */
    public static final List<StationSpec> ALL = List.of(DRYING_RACK, MORTAR, KETTLE, HEARTH, INFUSION_POT);

    private StationCatalog() {
    }
}
