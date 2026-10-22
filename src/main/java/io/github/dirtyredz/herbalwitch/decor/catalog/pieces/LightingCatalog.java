package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The lighting family (docs/catalogue/decor.md). Lamps: toggled by hand, light 14 while lit, no fuel or heat (decor/lighting).
 * Sizes are width x depth x height in cells.
 */
public final class LightingCatalog {
    public static final FurnitureSpec TABLE_LAMP = piece(Wave.M1, Family.LIGHTING, null, "table_lamp", "Table Lamp").template("table_lamp")
            .build(FurnitureBehavior.LIGHT, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec WALL_SCONCE = piece(Wave.C, Family.LIGHTING, "lamps", "wall_sconce", "Wall Sconce")
            .build(FurnitureBehavior.LIGHT, Support.WALL, 1, 1, 1);
    public static final FurnitureSpec HANGING_LANTERN = piece(Wave.C, Family.LIGHTING, "lamps", "hanging_lantern", "Hanging Lantern")
            .build(FurnitureBehavior.LIGHT, Support.CEILING, 1, 1, 1);
    public static final FurnitureSpec CHANDELIER = piece(Wave.C, Family.LIGHTING, "lamps", "chandelier", "Chandelier")
            .build(FurnitureBehavior.LIGHT, Support.CEILING, 2, 2, 1);
    public static final FurnitureSpec CEILING_LIGHT = piece(Wave.C, Family.LIGHTING, "lamps", "ceiling_light", "Ceiling Light")
            .build(FurnitureBehavior.LIGHT, Support.CEILING, 1, 1, 1);
    public static final FurnitureSpec FLOOR_LAMP = piece(Wave.C, Family.LIGHTING, "lamps", "floor_lamp", "Floor Lamp")
            .build(FurnitureBehavior.LIGHT, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec POST_LANTERN = piece(Wave.C, Family.LIGHTING, "lamps", "post_lantern", "Post Lantern")
            .build(FurnitureBehavior.LIGHT, Support.FLOOR, 1, 1, 3);
    public static final FurnitureSpec PENDANT_LIGHT = piece(Wave.C, Family.LIGHTING, "lamps", "pendant_light", "Pendant Light")
            .build(FurnitureBehavior.LIGHT, Support.CEILING, 1, 1, 2);

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(TABLE_LAMP, WALL_SCONCE, HANGING_LANTERN, CHANDELIER, CEILING_LIGHT, FLOOR_LAMP, POST_LANTERN, PENDANT_LIGHT);

    private LightingCatalog() {
    }
}
