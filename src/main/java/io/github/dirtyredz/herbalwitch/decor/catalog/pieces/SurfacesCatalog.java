package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The surfaces family (docs/catalogue/decor.md). Tables, counters and desks: decorative, with full tops so a lamp can stand on them. The work table is a lookalike, not a station.
 * Sizes are width x depth x height in cells.
 */
public final class SurfacesCatalog {
    public static final FurnitureSpec APOTHECARY_COUNTER = piece(Wave.M1, Family.SURFACES, null, "apothecary_counter", "Apothecary Counter").template("apothecary_counter")
            .build(FurnitureBehavior.STATIC, Support.FREE, 1, 1, 1);
    public static final FurnitureSpec KITCHEN_COUNTER = piece(Wave.A, Family.SURFACES, "kitchen", "kitchen_counter", "Kitchen Counter")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec CORNER_COUNTER = piece(Wave.A, Family.SURFACES, "kitchen", "corner_counter", "Corner Counter")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec KITCHEN_ISLAND = piece(Wave.A, Family.SURFACES, "kitchen", "kitchen_island", "Kitchen Island")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec DINING_TABLE = piece(Wave.A, Family.SURFACES, "tables", "dining_table", "Dining Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 3, 2, 1);
    public static final FurnitureSpec SERVING_CART = piece(Wave.A, Family.SURFACES, "kitchen", "serving_cart", "Serving Cart")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec WORK_TABLE = piece(Wave.A, Family.SURFACES, "desks", "work_table", "Work Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec COFFEE_TABLE = piece(Wave.B, Family.SURFACES, "tables", "coffee_table", "Coffee Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec SIDE_TABLE = piece(Wave.B, Family.SURFACES, "tables", "side_table", "Side Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec CONSOLE_TABLE = piece(Wave.B, Family.SURFACES, "tables", "console_table", "Console Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec TV_STAND = piece(Wave.B, Family.SURFACES, "desks", "tv_stand", "Curio Cabinet")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec VANITY_TABLE = piece(Wave.B, Family.SURFACES, "desks", "vanity_table", "Vanity Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec WRITING_DESK = piece(Wave.B, Family.SURFACES, "desks", "writing_desk", "Writing Desk")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec MAP_TABLE = piece(Wave.B, Family.SURFACES, "tables", "map_table", "Map Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 2, 2, 1);
    public static final FurnitureSpec GARDEN_TABLE = piece(Wave.C, Family.SURFACES, "tables", "garden_table", "Garden Table")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 1, 1, 1);

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(APOTHECARY_COUNTER, KITCHEN_COUNTER, CORNER_COUNTER, KITCHEN_ISLAND, DINING_TABLE, SERVING_CART, WORK_TABLE, COFFEE_TABLE, SIDE_TABLE, CONSOLE_TABLE, TV_STAND, VANITY_TABLE, WRITING_DESK, MAP_TABLE, GARDEN_TABLE);

    private SurfacesCatalog() {
    }
}
