package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The openings family (docs/catalogue/decor.md). The door (vanilla door behaviour), a wall-mounted window panel and a freestanding archway.
 * Sizes are width x depth x height in cells.
 */
public final class OpeningsCatalog {
    public static final FurnitureSpec DOOR = piece(Wave.C, Family.OPENINGS, "frames", "door", "Cottage Door")
            .build(FurnitureBehavior.DOOR, Support.FLOOR, 1, 1, 2);
    public static final FurnitureSpec WINDOW = piece(Wave.C, Family.OPENINGS, "frames", "window", "Cottage Window")
            .build(FurnitureBehavior.STATIC, Support.WALL, 1, 1, 2);
    public static final FurnitureSpec ARCHWAY = piece(Wave.C, Family.OPENINGS, "frames", "archway", "Archway")
            .build(FurnitureBehavior.STATIC, new MountSpec(Support.FLOOR, List.of(new Cell(0, 0, 0), new Cell(2, 0, 0), new Cell(0, 1, 0), new Cell(2, 1, 0), new Cell(0, 2, 0), new Cell(1, 2, 0), new Cell(2, 2, 0))));

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(DOOR, WINDOW, ARCHWAY);

    private OpeningsCatalog() {
    }
}
