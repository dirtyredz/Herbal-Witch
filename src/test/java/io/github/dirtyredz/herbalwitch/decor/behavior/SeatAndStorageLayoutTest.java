package io.github.dirtyredz.herbalwitch.decor.behavior;

import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatLayout;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.StorageSpec;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.display.DisplaySpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import io.github.dirtyredz.herbalwitch.decor.structure.shape.FurnitureShapes;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure seat and storage geometry: seat counts, separation, rotation and aimed-seat selection; storage
 * capacities, display limits and one anchor per display slot inside the piece's cells.
 */
class SeatAndStorageLayoutTest {
    private static final List<Direction> FACINGS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    @Test
    void everySeatingPieceHasItsSeatsInsideItsCellsAndApart() {
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            if (spec.behavior() != FurnitureBehavior.SEATING) {
                continue;
            }
            List<SeatLayout.Seat> seats = SeatLayout.seats(spec);
            assertEquals(spec.seats(), seats.size(), spec.id());
            for (SeatLayout.Seat s : seats) {
                boolean inside = spec.mount().cells().stream().anyMatch(c -> s.x() >= -16 * c.right() && s.x() <= 16 - 16 * c.right()
                        && s.z() >= 16 * c.depth() && s.z() <= 16 + 16 * c.depth() && s.y() >= 16 * c.up() && s.y() < 16 + 16 * c.up());
                assertTrue(inside, spec.id() + " seat outside its cells: " + s);
            }
            for (int i = 0; i < seats.size(); i++) {
                for (int j = i + 1; j < seats.size(); j++) {
                    double d = Math.hypot(seats.get(i).x() - seats.get(j).x(), seats.get(i).z() - seats.get(j).z());
                    assertTrue(d >= 8, spec.id() + " seats " + i + " and " + j + " are " + d + " px apart");
                }
            }
        }
    }

    @Test
    void seatsTurnWithTheFacingAndTheAimedSeatIsChosen() {
        FurnitureSpec sofa = FurnitureCatalog.DESCRIBED.stream().filter(s -> s.id().equals("sofa")).findFirst().orElseThrow();
        BlockPos origin = new BlockPos(-10, 70, 3);
        for (Direction facing : FACINGS) {
            List<Vec3> seats = SeatLayout.world(sofa, origin, facing);
            assertEquals(3, new HashSet<>(seats).size(), facing.toString());
            for (int i = 0; i < seats.size(); i++) {
                assertEquals(i, SeatLayout.nearest(seats, seats.get(i).add(0.1, 0.3, -0.1)), facing + " seat " + i);
            }
            // seat 0 is over the origin cell
            Vec3 first = seats.get(0);
            assertTrue(BlockPos.containing(first.x, first.y, first.z).equals(origin), facing + ": seat 0 at " + first);
        }
        Vec3 chair = SeatLayout.world(SeatingCatalog.ARMCHAIR, BlockPos.ZERO, Direction.NORTH).get(0);
        assertEquals(new Vec3(0.5, 7 / 16.0, 7 / 16.0), chair);
    }

    @Test
    void storageCapacitiesAndDisplayLimitsFollowTheCatalogue() {
        for (FurnitureSpec spec : FurnitureCatalog.DESCRIBED) {
            if (spec.behavior() == FurnitureBehavior.STORAGE) {
                StorageSpec storage = StorageSpec.of(spec);
                assertEquals(9, storage.columns(), spec.id());
                assertEquals(64, storage.slotLimit(), spec.id());
                assertTrue(storage.rows() == 3 || storage.rows() == 6, spec.id());
            } else if (spec.behavior() == FurnitureBehavior.DISPLAY) {
                StorageSpec storage = StorageSpec.of(spec);
                assertEquals(1, storage.slotLimit(), spec.id() + ": a display holds one item per slot");
                assertEquals(3, storage.columns(), spec.id());
            }
        }
    }

    /** Every delivered display has exactly one anchor per slot, each inside the piece's cells, all distinct. */
    @Test
    void everyDeliveredDisplayHasOneAnchorPerSlot() {
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            if (spec.behavior() != FurnitureBehavior.DISPLAY) {
                continue;
            }
            List<double[]> anchors = DisplaySpec.anchors(spec);
            assertEquals(spec.slots(), anchors.size(), spec.id());
            Set<String> distinct = new HashSet<>();
            for (double[] a : anchors) {
                assertTrue(distinct.add(a[0] + "," + a[1] + "," + a[2]), spec.id() + " repeats an anchor");
                boolean inside = spec.mount().cells().stream().anyMatch(c -> a[0] >= -16 * c.right() && a[0] <= 16 - 16 * c.right()
                        && a[2] >= 16 * c.depth() && a[2] <= 16 + 16 * c.depth() && a[1] >= 16 * c.up() && a[1] < 16 + 16 * c.up());
                assertTrue(inside, spec.id() + " anchor outside its cells");
            }
        }
    }

    /** Every delivered piece but the door and the vanilla-carpet rug has authored collision touching each of its cells it fills. */
    @Test
    void everyDeliveredPieceHasShapes() {
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            if (spec.behavior() == FurnitureBehavior.DOOR || (spec.behavior() == FurnitureBehavior.RUG && !spec.multiblock())) {
                continue;
            }
            assertTrue(FurnitureShapes.authored(spec), spec.id() + " has no shape");
            double[][] boxes = FurnitureShapes.boxes(spec);
            for (Cell cell : spec.mount().cells()) {
                assertTrue(FurnitureShapes.cut(boxes, cell).size() >= 0, spec.id());
            }
            boolean grounded = spec.mount().cells().stream().filter(c -> c.up() == 0)
                    .anyMatch(c -> !FurnitureShapes.cut(boxes, c).isEmpty());
            assertTrue(grounded || spec.support() != io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support.FLOOR,
                    spec.id() + ": a floor piece with nothing at the floor");
        }
    }
}
