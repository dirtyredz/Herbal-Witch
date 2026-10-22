package io.github.dirtyredz.herbalwitch.platform.multiblock;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cell numbering, 3D transforms, inverse origin lookup, sparse layouts and neighbours in every facing. */
class MultiblockLayoutTest {
    private static final List<Direction> FACINGS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    @Test
    void legacyRectanglesKeepTheirNumbering() {
        MultiblockLayout rack = MultiblockLayout.rectangle(2, 2);
        assertEquals(List.of(new Cell(0, 0), new Cell(1, 0), new Cell(0, 1), new Cell(1, 1)), rack.cells());
        assertEquals(List.of(new Cell(0, 0), new Cell(1, 0)), MultiblockLayout.rectangle(2, 1).cells());
        assertEquals("part", rack.part().getName());
    }

    @Test
    void boxesNumberByHeightThenDepthThenRight() {
        MultiblockLayout table = MultiblockLayout.box(3, 2, 1);
        assertEquals(List.of(new Cell(0, 0, 0), new Cell(1, 0, 0), new Cell(2, 0, 0), new Cell(0, 0, 1), new Cell(1, 0, 1),
                new Cell(2, 0, 1)), table.cells());
        MultiblockLayout canopy = MultiblockLayout.box(2, 2, 3);
        assertEquals(12, canopy.size());
        assertEquals(new Cell(0, 1, 0), canopy.cell(4));
    }

    @Test
    void cellsTurnWithTheFacingAndTheOriginIsRecoveredFromEveryCell() {
        MultiblockLayout box = MultiblockLayout.box(3, 2, 2);
        BlockPos origin = new BlockPos(-5, 64, -7); // negative coordinates on purpose
        for (Direction facing : FACINGS) {
            Set<BlockPos> seen = new HashSet<>();
            for (int i = 0; i < box.size(); i++) {
                BlockPos pos = box.cellPos(origin, facing, i);
                assertTrue(seen.add(pos), facing + ": two cells at " + pos);
                assertEquals(origin, box.origin(pos, facing, i), facing + " cell " + i);
            }
            // the viewer's right of a north-facing piece is west, its back is south
            if (facing == Direction.NORTH) {
                assertEquals(origin.west(), box.cellPos(origin, facing, 1));
                assertEquals(origin.south(), box.cellPos(origin, facing, 3));
                assertEquals(origin.above(), box.cellPos(origin, facing, 6));
            }
            if (facing == Direction.EAST) {
                assertEquals(origin.north(), box.cellPos(origin, facing, 1));
                assertEquals(origin.west(), box.cellPos(origin, facing, 3));
            }
        }
    }

    @Test
    void neighboursPointAtTheExpectedCellsInEveryFacing() {
        MultiblockLayout box = MultiblockLayout.box(2, 2, 2);
        for (Direction facing : FACINGS) {
            for (int i = 0; i < box.size(); i++) {
                for (Map.Entry<Direction, Integer> n : box.neighbours(facing, i).entrySet()) {
                    BlockPos here = box.cellPos(BlockPos.ZERO, facing, i);
                    assertEquals(box.cellPos(BlockPos.ZERO, facing, n.getValue()), here.relative(n.getKey()), facing + " " + i + " -> " + n);
                }
                assertEquals(3, box.neighbours(facing, i).size(), "every cell of a 2x2x2 box has three neighbours");
            }
        }
    }

    @Test
    void sparseLayoutsMustBeConnectedAndStartAtTheOrigin() {
        List<Cell> arch = List.of(new Cell(0, 0, 0), new Cell(2, 0, 0), new Cell(0, 1, 0), new Cell(2, 1, 0), new Cell(0, 2, 0),
                new Cell(1, 2, 0), new Cell(2, 2, 0));
        MultiblockLayout archway = MultiblockLayout.of("part", arch);
        assertEquals(7, archway.size());
        assertFalse(archway.grounded(5), "the lintel's middle stands on nothing of its own");
        assertTrue(archway.top(5) && archway.top(4) && !archway.top(0));
        assertTrue(MultiblockLayout.problems(List.of(new Cell(0, 0, 0), new Cell(2, 0, 0))).contains("cells are not face-connected"));
        assertTrue(MultiblockLayout.problems(List.of(new Cell(1, 0, 0), new Cell(0, 0, 0))).contains("cell 0 must be the origin"));
        assertTrue(MultiblockLayout.problems(List.of(new Cell(0, 0, 0), new Cell(1, 0, 0), new Cell(1, 0, 0))).contains("a cell appears twice"));
        assertThrows(IllegalArgumentException.class, () -> MultiblockLayout.of("part", List.of(new Cell(0, 0, 0), new Cell(0, 0, 2))));
    }

    @Test
    void rearAndTopFollowTheCells() {
        MultiblockLayout bed = MultiblockLayout.box(1, 2, 1);
        assertFalse(bed.rear(0));
        assertTrue(bed.rear(1));
        MultiblockLayout pantry = MultiblockLayout.box(1, 1, 2);
        assertFalse(pantry.top(0));
        assertTrue(pantry.top(1) && pantry.grounded(0) && !pantry.grounded(1));
    }
}
