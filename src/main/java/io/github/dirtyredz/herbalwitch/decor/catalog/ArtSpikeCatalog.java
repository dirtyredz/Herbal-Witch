package io.github.dirtyredz.herbalwitch.decor.catalog;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Behavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;

import java.util.List;

/**
 * The five representative pieces of the Milestone 1 art spike, each registered once in the cottage design.
 * Mass furniture production waits for the owner's approval of these (docs/ART.md).
 */
public final class ArtSpikeCatalog {
    public static final FurnitureSpec ARMCHAIR =
            new FurnitureSpec("armchair", "Armchair", Family.SEATING, "armchair", Behavior.FREESTANDING);
    public static final FurnitureSpec APOTHECARY_COUNTER =
            new FurnitureSpec("apothecary_counter", "Apothecary Counter", Family.SURFACES, "apothecary_counter", Behavior.FREESTANDING);
    public static final FurnitureSpec SMALL_WALL_SHELF =
            new FurnitureSpec("small_wall_shelf", "Small Wall Shelf", Family.STORAGE, "wall_shelf", Behavior.WALL_MOUNTED);
    public static final FurnitureSpec TABLE_LAMP =
            new FurnitureSpec("table_lamp", "Table Lamp", Family.LIGHTING, "table_lamp", Behavior.TABLE_LAMP);
    public static final FurnitureSpec SMALL_RUG =
            new FurnitureSpec("small_rug", "Small Rug", Family.TEXTILES, "rug", Behavior.RUG);

    /** Creative-tab order. */
    public static final List<FurnitureSpec> ALL = List.of(ARMCHAIR, APOTHECARY_COUNTER, SMALL_WALL_SHELF, TABLE_LAMP, SMALL_RUG);

    private ArtSpikeCatalog() {
    }
}
