package io.github.dirtyredz.herbalwitch.apothecary.stations.catalog;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;

/**
 * One functional station's identity and presentation: plain data, no inventory logic. Ids are
 * {@code stations/<directory>/station}; each station has one authored appearance.
 *
 * @param directory   id segment and model/GUI directory, e.g. {@code drying_rack}
 * @param displayName English name
 * @param activeModel whether the block shows a second (active) model
 * @param layout      the cells of a multiblock station, or null for one block
 */
public record StationSpec(String directory, String displayName, boolean activeModel, MultiblockLayout layout) {
    public StationSpec(String directory, String displayName, boolean activeModel) {
        this(directory, displayName, activeModel, null);
    }

    public String path() {
        return "stations/" + directory + "/station";
    }
}
