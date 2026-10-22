package io.github.dirtyredz.herbalwitch.decor.catalog;

import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The whole described catalogue against the independent M5 inventory: the five 0.1.0 pieces and
 * A 22, B 30, C 37 additions (94 canonical ids), unique paths, and only behaviour/support
 * combinations the blocks implement.
 */
class FurnitureCatalogTest {
    @Test
    void theCatalogueDescribesTheCanonicalInventoryInOrder() {
        assertEquals(DecorPieces.ALL.stream().map(DecorPieces.Piece::id).toList(), FurnitureCatalog.DESCRIBED.stream().map(FurnitureSpec::id).toList());
        Map<String, Long> waves = FurnitureCatalog.DESCRIBED.stream().collect(Collectors.groupingBy(s -> s.wave().name(), Collectors.counting()));
        assertEquals(Map.of("M1", 5L, "A", 22L, "B", 30L, "C", 37L), waves);
        assertEquals(94, FurnitureCatalog.DESCRIBED.size());
        Map<String, Long> families = FurnitureCatalog.DESCRIBED.stream()
                .collect(Collectors.groupingBy(s -> s.family().directory(), Collectors.counting()));
        assertEquals(Map.of("seating", 13L, "storage", 27L, "surfaces", 15L, "fixtures", 22L, "lighting", 8L, "textiles", 6L,
                "openings", 3L), families);
    }

    @Test
    void pathsAreUniqueAndPermanent() {
        Set<String> paths = new HashSet<>();
        Set<String> recipes = new HashSet<>();
        Set<String> art = new HashSet<>();
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            assertTrue(paths.add(spec.path()), spec.path());
            assertTrue(recipes.add(spec.recipePath()), spec.recipePath());
            assertTrue(art.add(spec.artProject()), spec.artProject());
            assertTrue(spec.id().matches("[a-z_]+"), spec.id());
        }
        for (String old : List.of("decor/seating/armchair", "decor/surfaces/apothecary_counter", "decor/storage/small_wall_shelf",
                "decor/lighting/table_lamp", "decor/textiles/small_rug")) {
            assertTrue(paths.contains(old), old + " keeps its id");
            assertTrue(recipes.contains(old), old + " keeps its recipe id");
        }
    }

    @Test
    void deliveredIsEveryWaveUpToTheCheckpoint() {
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            assertEquals(spec.wave().compareTo(FurnitureCatalog.DELIVERED) <= 0, FurnitureCatalog.ALL.contains(spec), spec.id());
        }
    }

    @Test
    void behavioursAndSupportsCombineAsTheBlocksImplement() {
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            switch (spec.behavior()) {
                case SEATING -> assertTrue(spec.seats() >= 1 && spec.seats() <= 3, spec.id());
                case STORAGE -> assertTrue(spec.slots() == 27 || spec.slots() == 54, spec.id() + " " + spec.slots());
                case DISPLAY -> assertTrue(List.of(3, 6, 9).contains(spec.slots()), spec.id() + " " + spec.slots());
                case BED -> assertEquals(Support.FREE, spec.support(), spec.id());
                case DOOR -> assertEquals(List.of(1, 1, 2), List.of(spec.mount().size()[0], spec.mount().size()[1], spec.mount().size()[2]));
                case RUG -> assertEquals(Support.CARPET, spec.support(), spec.id());
                default -> {
                }
            }
            if (spec.support() == Support.WALL || spec.support() == Support.CORNER) {
                assertEquals(1, spec.mount().size()[1], spec.id() + ": wall pieces are one deep");
            }
        }
        // the plan's capacity defaults
        Set<String> big = Set.of("sideboard", "china_cabinet", "dresser", "wardrobe", "trunk", "rolltop_desk", "storage_cabinet");
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            if (spec.behavior() == FurnitureBehavior.STORAGE) {
                assertEquals(big.contains(spec.id()) ? 54 : 27, spec.slots(), spec.id());
            }
        }
        Map<String, Integer> seats = Map.of("sofa", 3, "bench", 2, "loveseat", 2, "entry_bench", 2, "garden_bench", 2, "garden_swing", 2);
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            if (spec.behavior() == FurnitureBehavior.SEATING) {
                assertEquals(seats.getOrDefault(spec.id(), 1), spec.seats(), spec.id());
            }
        }
    }
}
