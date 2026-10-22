package io.github.dirtyredz.herbalwitch.decor.catalog.pieces;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Family;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.piece;

/**
 * The seating family (docs/catalogue/decor.md). Chairs, benches and lounges. Every piece seats riders through the one seating implementation (decor/seating).
 * Sizes are width x depth x height in cells.
 */
public final class SeatingCatalog {
    public static final FurnitureSpec ARMCHAIR = piece(Wave.M1, Family.SEATING, null, "armchair", "Armchair").template("armchair").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FREE, 1, 1, 1);
    public static final FurnitureSpec DINING_CHAIR = piece(Wave.A, Family.SEATING, "chairs", "dining_chair", "Dining Chair").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec BENCH = piece(Wave.A, Family.SEATING, "benches", "bench", "Bench").seats(2)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec SOFA = piece(Wave.B, Family.SEATING, "lounges", "sofa", "Sofa").seats(3)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 3, 1, 1);
    public static final FurnitureSpec LOVESEAT = piece(Wave.B, Family.SEATING, "lounges", "loveseat", "Loveseat").seats(2)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec CHAISE_LOUNGE = piece(Wave.B, Family.SEATING, "lounges", "chaise_lounge", "Chaise Lounge").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 1, 2, 1);
    public static final FurnitureSpec VANITY_CHAIR = piece(Wave.B, Family.SEATING, "chairs", "vanity_chair", "Vanity Stool").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec DESK_CHAIR = piece(Wave.B, Family.SEATING, "chairs", "desk_chair", "Desk Chair").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec READING_CHAIR = piece(Wave.B, Family.SEATING, "chairs", "reading_chair", "Reading Chair").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec ENTRY_BENCH = piece(Wave.B, Family.SEATING, "benches", "entry_bench", "Entry Bench").seats(2)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec GARDEN_BENCH = piece(Wave.C, Family.SEATING, "benches", "garden_bench", "Garden Bench").seats(2)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 2, 1, 1);
    public static final FurnitureSpec OUTDOOR_CHAIR = piece(Wave.C, Family.SEATING, "chairs", "outdoor_chair", "Garden Chair").seats(1)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 1, 1, 1);
    public static final FurnitureSpec GARDEN_SWING = piece(Wave.C, Family.SEATING, "benches", "garden_swing", "Garden Swing").seats(2)
            .build(FurnitureBehavior.SEATING, Support.FLOOR, 3, 2, 3);

    /** Catalogue order (creative tab, showcase, grimoire). */
    public static final List<FurnitureSpec> ALL = List.of(ARMCHAIR, DINING_CHAIR, BENCH, SOFA, LOVESEAT, CHAISE_LOUNGE, VANITY_CHAIR, DESK_CHAIR, READING_CHAIR, ENTRY_BENCH, GARDEN_BENCH, OUTDOOR_CHAIR, GARDEN_SWING);

    private SeatingCatalog() {
    }
}
