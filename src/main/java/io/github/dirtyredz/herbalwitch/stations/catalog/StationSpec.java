package io.github.dirtyredz.herbalwitch.stations.catalog;

/**
 * One functional station's identity and presentation: plain data, no inventory logic. Ids are
 * {@code stations/<directory>/station}; each station has one authored appearance.
 *
 * @param directory   id segment and model/GUI directory, e.g. {@code drying_rack}
 * @param displayName English name
 * @param activeModel whether the block shows a second (active) model
 */
public record StationSpec(String directory, String displayName, boolean activeModel) {
    public String path() {
        return "stations/" + directory + "/station";
    }
}
