package io.github.dirtyredz.herbalwitch.magic.ward.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

import java.util.Locale;

/**
 * The ward stone's permanent identity and its hard bounds, plain data. Block, item, block entity and
 * menu {@code herbalwitch:magic/ward/stone}; records in the Overworld's {@code herbalwitch_wards}
 * saved data. Numbers a server may tune (cost, charge, radii, owner and dimension caps) are in the
 * witch rules ({@code WorkingRules}); these are the fixed ceilings the plan sets.
 */
public final class WardCatalog {
    public static final String PATH = "magic/ward/stone";
    public static final String NAME = "Ward Stone";
    public static final String SHOWCASE = "ward_stone";
    /** Block light of a charged ward. */
    public static final int ACTIVE_LIGHT = 4;
    /** Blocks from the stone a witch must stand within to activate or configure it directly. */
    public static final double REACH = 8.0;
    /** A hearth or stove this close (blocks, each axis) focuses the ward. */
    public static final int FOCUS_REACH = 4;
    /** Saved records per world. */
    public static final int MAX_RECORDS = 4096;
    /** Ward centres per chunk. */
    public static final int CENTRES_PER_CHUNK = 8;
    /** Ticks between a ward's sweeps of existing targets (staggered by record). */
    public static final int SWEEP_INTERVAL = 20;
    /** Mobs one ward inspects per sweep. */
    public static final int CHECKS_PER_SWEEP = 32;
    /** Mobs all wards of a dimension inspect per tick. */
    public static final int CHECKS_PER_DIMENSION_TICK = 256;

    /** What a ward keeps from choosing players inside it: never arbitrary entity classes. */
    public enum Profile {
        UNDEAD,
        ARTHROPODS,
        BOTH;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        public Profile next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Profile byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : BOTH;
        }
    }

    private WardCatalog() {
    }

    public static String id() {
        return HerbalWitch.MOD_ID + ":" + PATH;
    }
}
