package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The textiles family (docs/catalogue/decor.md). Rugs, curtains and screens. The small rug stays vanilla carpet.
 * Sizes are width x depth x height in cells.
 */
public final class TextilesCatalog {
    public static final FurnitureSpec SMALL_RUG = piece(Wave.M1, Family.TEXTILES, null, "small_rug", "Small Rug").template("rug")
            .build(FurnitureBehavior.RUG, Support.CARPET, 1, 1, 1);
    public static final FurnitureSpec MEDIUM_RUG = piece(Wave.C, Family.TEXTILES, "rugs", "medium_rug", "Medium Rug")
            .build(FurnitureBehavior.RUG, Support.CARPET, 2, 2, 1);
    public static final FurnitureSpec LARGE_RUG = piece(Wave.C, Family.TEXTILES, "rugs", "large_rug", "Large Rug")
            .build(FurnitureBehavior.RUG, Support.CARPET, 3, 3, 1);
    public static final FurnitureSpec CURTAIN = piece(Wave.C, Family.TEXTILES, "drapes", "curtain", "Curtain")
            .build(FurnitureBehavior.STATIC, Support.CEILING, 2, 1, 2);
    public static final FurnitureSpec ROOM_DIVIDER = piece(Wave.C, Family.TEXTILES, "drapes", "room_divider", "Folding Screen")
            .build(FurnitureBehavior.STATIC, Support.FLOOR, 3, 1, 2);
    public static final FurnitureSpec HANGING_DIVIDER = piece(Wave.C, Family.TEXTILES, "drapes", "hanging_divider", "Hanging Divider")
            .build(FurnitureBehavior.STATIC, Support.CEILING, 3, 1, 2);

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(SMALL_RUG, MEDIUM_RUG, LARGE_RUG, CURTAIN, ROOM_DIVIDER, HANGING_DIVIDER);

    private TextilesCatalog() {
    }
}
