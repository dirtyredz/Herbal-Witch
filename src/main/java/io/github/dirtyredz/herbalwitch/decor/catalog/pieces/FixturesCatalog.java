package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The fixtures family (docs/catalogue/decor.md). Kitchen, bath, hall and garden fixtures, the two beds and the firepit. None is a station: the range, altar, rack and fireplace link to the functional ones in the grimoire.
 * Sizes are width x depth x height in cells.
 */
public final class FixturesCatalog {
    public static final FurnitureSpec KITCHEN_SINK = piece(Wave.A, Family.FIXTURES, "kitchen", "kitchen_sink", "Kitchen Sink")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec STOVE_OVEN = piece(Wave.A, Family.FIXTURES, "kitchen", "stove_oven", "Kitchen Range")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec FRIDGE = piece(Wave.A, Family.FIXTURES, "kitchen", "fridge", "Icebox")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec DRYING_RACK_DECOR = piece(Wave.A, Family.FIXTURES, "apothecary", "drying_rack_decor", "Decorative Drying Rack")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec ALTAR_TABLE = piece(Wave.A, Family.FIXTURES, "apothecary", "altar_table", "Altar Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec BED = piece(Wave.B, Family.FIXTURES, "bedroom", "bed", "Cottage Bed")
            .build(FurnitureBehavior.BED, Support.FREE, 1, 2, 1);
    public static final FurnitureSpec CANOPY_BED = piece(Wave.B, Family.FIXTURES, "bedroom", "canopy_bed", "Canopy Bed")
            .build(FurnitureBehavior.BED, Support.FREE, 2, 2, 3);
    public static final FurnitureSpec FIREPLACE = piece(Wave.B, Family.FIXTURES, "hearth", "fireplace", "Fireplace")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec MIRROR = piece(Wave.B, Family.FIXTURES, "hall", "mirror", "Mirror")
            .build(FurnitureBehavior.STATIC, Support.WALL, 1, 1, 2);
    public static final FurnitureSpec GLOBE_STAND = piece(Wave.B, Family.FIXTURES, "hall", "globe_stand", "Globe Stand")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec COAT_RACK = piece(Wave.B, Family.FIXTURES, "hall", "coat_rack", "Coat Rack")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec KEY_HOOKS = piece(Wave.B, Family.FIXTURES, "hall", "key_hooks", "Key Hooks")
            .build(FurnitureBehavior.STATIC, Support.WALL, 1, 1, 1);
    public static final FurnitureSpec UMBRELLA_STAND = piece(Wave.B, Family.FIXTURES, "hall", "umbrella_stand", "Umbrella Stand")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec CLAWFOOT_TUB = piece(Wave.C, Family.FIXTURES, "bathroom", "clawfoot_tub", "Clawfoot Tub")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 2, 1);
    public static final FurnitureSpec SHOWER_STALL = piece(Wave.C, Family.FIXTURES, "bathroom", "shower_stall", "Shower Stall")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec TOILET = piece(Wave.C, Family.FIXTURES, "bathroom", "toilet", "Commode")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec SINK_VANITY = piece(Wave.C, Family.FIXTURES, "bathroom", "sink_vanity", "Washstand")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec TOWEL_RACK = piece(Wave.C, Family.FIXTURES, "bathroom", "towel_rack", "Towel Rack")
            .build(FurnitureBehavior.STATIC, Support.WALL, 1, 1, 1);
    public static final FurnitureSpec PLANTER_BOX = piece(Wave.C, Family.FIXTURES, "garden", "planter_box", "Planter Box")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec TRELLIS = piece(Wave.C, Family.FIXTURES, "garden", "trellis", "Trellis")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec DECORATIVE_WELL = piece(Wave.C, Family.FIXTURES, "garden", "decorative_well", "Wishing Well")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 2, 3);
    public static final FurnitureSpec FIREPIT = piece(Wave.C, Family.FIXTURES, "hearth", "firepit", "Firepit")
            .build(FurnitureBehavior.LIGHT, Support.FLOOR, 2, 2, 1);

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(KITCHEN_SINK, STOVE_OVEN, FRIDGE, DRYING_RACK_DECOR, ALTAR_TABLE, BED, CANOPY_BED, FIREPLACE, MIRROR, GLOBE_STAND, COAT_RACK, KEY_HOOKS, UMBRELLA_STAND, CLAWFOOT_TUB, SHOWER_STALL, TOILET, SINK_VANITY, TOWEL_RACK, PLANTER_BOX, TRELLIS, DECORATIVE_WELL, FIREPIT);

    private FixturesCatalog() {
    }
}
