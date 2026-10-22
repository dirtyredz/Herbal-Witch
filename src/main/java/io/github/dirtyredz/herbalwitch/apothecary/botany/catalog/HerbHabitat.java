package io.github.dirtyredz.herbalwitch.apothecary.botany.catalog;

import java.util.List;

/**
 * The five permanent herb groups. The group is the id segment ({@code botany/<group>/<herb>/...}),
 * chosen once so no generated directory ever holds all fourteen herbs. The defaults below seed the
 * generated condition and worldgen data; packs retune them through data without renaming anything.
 *
 * @param directory           id segment
 * @param discoveryChance     default chance per qualifying grass break of discovering a herb of this group
 * @param rarityChunks        default worldgen rarity: one patch attempt per this many chunks
 * @param preferences         live conditions that improve discovery and harvest
 */
public enum HerbHabitat {
    TEMPERATE("temperate", 1.0 / 32, 8, List.of()),
    DRY("dry", 1.0 / 32, 8, List.of()),
    WET("wet", 1.0 / 32, 8, List.of(Preference.RAIN)),
    SHADE("shade", 1.0 / 32, 16, List.of(Preference.LOW_SKY)),
    WOODLAND("woodland", 1.0 / 64, 32, List.of(Preference.NIGHT, Preference.FULL_MOON));

    /** A live environmental condition a herb likes. */
    public enum Preference {
        RAIN("rain"),
        LOW_SKY("low_sky"),
        NIGHT("night"),
        FULL_MOON("full_moon");

        private final String id;

        Preference(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static Preference byId(String id) {
            for (Preference p : values()) {
                if (p.id.equals(id)) {
                    return p;
                }
            }
            return null;
        }
    }

    private final String directory;
    private final double discoveryChance;
    private final int rarityChunks;
    private final List<Preference> preferences;

    HerbHabitat(String directory, double discoveryChance, int rarityChunks, List<Preference> preferences) {
        this.directory = directory;
        this.discoveryChance = discoveryChance;
        this.rarityChunks = rarityChunks;
        this.preferences = preferences;
    }

    public String directory() {
        return directory;
    }

    public double discoveryChance() {
        return discoveryChance;
    }

    public int rarityChunks() {
        return rarityChunks;
    }

    public List<Preference> preferences() {
        return preferences;
    }
}
