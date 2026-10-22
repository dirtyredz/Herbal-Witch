package io.github.dirtyredz.herbalwitch.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The ritual circle's 16-cell order, its hollow interior, the altar's place inside it in every facing, and the inverse lookups. */
class CircleLayoutTest {
    private static final List<Direction> FACINGS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    @Test
    void theRingHasSixteenCellsInCanonicalOrder() {
        List<Cell> ring = CircleCatalog.LAYOUT.cells();
        assertEquals(16, ring.size());
        assertEquals(List.of(new Cell(0, 0, 0), new Cell(1, 0, 0), new Cell(2, 0, 0), new Cell(3, 0, 0), new Cell(4, 0, 0),
                new Cell(0, 0, 1), new Cell(4, 0, 1), new Cell(0, 0, 2), new Cell(4, 0, 2), new Cell(0, 0, 3), new Cell(4, 0, 3),
                new Cell(0, 0, 4), new Cell(1, 0, 4), new Cell(2, 0, 4), new Cell(3, 0, 4), new Cell(4, 0, 4)), ring);
        assertEquals(15, CircleCatalog.LAYOUT.part().getPossibleValues().size() - 1, "one part value per cell");
        assertTrue(MultiblockLayout.problems(ring).isEmpty());
    }

    @Test
    void theAltarFitsTheHollowInterior() {
        Set<Cell> ring = new HashSet<>(CircleCatalog.LAYOUT.cells());
        Cell a = CircleCatalog.ALTAR;
        for (Cell c : AltarCatalog.LAYOUT.cells()) {
            Cell at = new Cell(a.right() + c.right(), a.up() + c.up(), a.depth() + c.depth());
            assertFalse(ring.contains(new Cell(at.right(), 0, at.depth())), "altar cell " + at + " collides with the ring");
            assertTrue(at.right() >= 1 && at.right() <= 3 && at.depth() >= 1 && at.depth() <= 3, at + " outside the interior");
        }
    }

    @Test
    void altarAndCircleOriginsAreInversesInEveryFacing() {
        BlockPos circle = new BlockPos(100, 64, -40);
        for (Direction facing : FACINGS) {
            BlockPos altar = CircleLayout.altarOrigin(circle, facing);
            assertEquals(circle, CircleLayout.circleOrigin(altar, facing), facing.toString());
            assertEquals(2, Math.abs(altar.getX() - circle.getX()) + Math.abs(altar.getZ() - circle.getZ()) - 1, facing + ": one right, two back");
            List<BlockPos> cells = CircleLayout.cells(circle, facing);
            assertEquals(16, new HashSet<>(cells).size());
            assertEquals(circle, cells.get(0));
            for (int i = 0; i < cells.size(); i++) {
                assertEquals(circle, CircleCatalog.LAYOUT.origin(cells.get(i), facing, i));
            }
        }
    }

    @Test
    void facingNorthTheRingExtendsWestAndSouth() {
        BlockPos circle = BlockPos.ZERO;
        assertEquals(new BlockPos(-1, 0, 2), CircleLayout.altarOrigin(circle, Direction.NORTH));
        assertEquals(new BlockPos(-4, 0, 4), CircleLayout.cells(circle, Direction.NORTH).get(15));
    }
}
