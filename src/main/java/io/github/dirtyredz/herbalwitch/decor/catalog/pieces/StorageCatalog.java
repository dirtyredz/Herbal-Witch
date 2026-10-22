package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The storage family (docs/catalogue/decor.md). Cabinets (one origin inventory each, 27 or 54 slots) and display shelves (one item per slot, drawn where it stands).
 * Sizes are width x depth x height in cells.
 */
public final class StorageCatalog {
    public static final FurnitureSpec SMALL_WALL_SHELF = piece(Wave.M1, Family.STORAGE, null, "small_wall_shelf", "Small Wall Shelf").template("wall_shelf").slots(3)
            .build(FurnitureBehavior.DISPLAY, Support.WALL, 1, 1, 1);
    public static final FurnitureSpec LOWER_CABINET = piece(Wave.A, Family.STORAGE, "cabinets", "lower_cabinet", "Lower Cabinet").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec UPPER_CABINET = piece(Wave.A, Family.STORAGE, "cabinets", "upper_cabinet", "Upper Cabinet").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.WALL, 1, 1, 1);
    public static final FurnitureSpec PANTRY = piece(Wave.A, Family.STORAGE, "cabinets", "pantry", "Pantry").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec SIDEBOARD = piece(Wave.A, Family.STORAGE, "cabinets", "sideboard", "Sideboard").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec CHINA_CABINET = piece(Wave.A, Family.STORAGE, "cabinets", "china_cabinet", "China Cabinet").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec WINE_RACK = piece(Wave.A, Family.STORAGE, "cabinets", "wine_rack", "Wine Rack").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec INGREDIENT_CABINET = piece(Wave.A, Family.STORAGE, "cabinets", "ingredient_cabinet", "Ingredient Cabinet").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec OPEN_SHELVES = piece(Wave.A, Family.STORAGE, "shelves", "open_shelves", "Open Shelves").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec POTION_SHELF = piece(Wave.A, Family.STORAGE, "shelves", "potion_shelf", "Potion Shelf").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec NIGHTSTAND = piece(Wave.B, Family.STORAGE, "bedroom", "nightstand", "Nightstand").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec DRESSER = piece(Wave.B, Family.STORAGE, "bedroom", "dresser", "Dresser").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec WARDROBE = piece(Wave.B, Family.STORAGE, "bedroom", "wardrobe", "Wardrobe").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec TRUNK = piece(Wave.B, Family.STORAGE, "bedroom", "trunk", "Trunk").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec ROLLTOP_DESK = piece(Wave.B, Family.STORAGE, "study", "rolltop_desk", "Rolltop Desk").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec FILING_CABINET = piece(Wave.B, Family.STORAGE, "study", "filing_cabinet", "Filing Cabinet").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec SHOE_CABINET = piece(Wave.B, Family.STORAGE, "bedroom", "shoe_cabinet", "Shoe Cabinet").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec BOOKSHELF = piece(Wave.B, Family.STORAGE, "study", "bookshelf", "Bookshelf").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec LINEN_CABINET = piece(Wave.C, Family.STORAGE, "bedroom", "linen_cabinet", "Linen Cabinet").slots(27)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec STORAGE_CABINET = piece(Wave.C, Family.STORAGE, "study", "storage_cabinet", "Storage Cabinet").slots(54)
            .build(FurnitureBehavior.STORAGE, Support.FLOOR, 2, 1, 2);
    public static final FurnitureSpec WALL_SHELF_MEDIUM = piece(Wave.C, Family.STORAGE, "shelves", "wall_shelf_medium", "Medium Wall Shelf").slots(6)
            .build(FurnitureBehavior.DISPLAY, Support.WALL, 2, 1, 1);
    public static final FurnitureSpec WALL_SHELF_LARGE = piece(Wave.C, Family.STORAGE, "shelves", "wall_shelf_large", "Large Wall Shelf").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.WALL, 2, 1, 2);
    public static final FurnitureSpec CORNER_SHELF = piece(Wave.C, Family.STORAGE, "shelves", "corner_shelf", "Corner Shelf").slots(6)
            .build(FurnitureBehavior.DISPLAY, Support.CORNER, 1, 1, 2);
    public static final FurnitureSpec FREE_STANDING_SHELF_SHORT = piece(Wave.C, Family.STORAGE, "shelves", "free_standing_shelf_short", "Short Standing Shelf").slots(6)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec FREE_STANDING_SHELF_MEDIUM = piece(Wave.C, Family.STORAGE, "shelves", "free_standing_shelf_medium", "Standing Shelf").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec FREE_STANDING_SHELF_LARGE = piece(Wave.C, Family.STORAGE, "shelves", "free_standing_shelf_large", "Tall Standing Shelf").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 3);
    public static final FurnitureSpec LADDER_SHELF = piece(Wave.C, Family.STORAGE, "shelves", "ladder_shelf", "Ladder Shelf").slots(9)
            .build(FurnitureBehavior.DISPLAY, Support.FLOOR, 1, 1, 2);

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(SMALL_WALL_SHELF, LOWER_CABINET, UPPER_CABINET, PANTRY, SIDEBOARD, CHINA_CABINET, WINE_RACK, INGREDIENT_CABINET, OPEN_SHELVES, POTION_SHELF, NIGHTSTAND, DRESSER, WARDROBE, TRUNK, ROLLTOP_DESK, FILING_CABINET, SHOE_CABINET, BOOKSHELF, LINEN_CABINET, STORAGE_CABINET, WALL_SHELF_MEDIUM, WALL_SHELF_LARGE, CORNER_SHELF, FREE_STANDING_SHELF_SHORT, FREE_STANDING_SHELF_MEDIUM, FREE_STANDING_SHELF_LARGE, LADDER_SHELF);

    private StorageCatalog() {
    }
}
