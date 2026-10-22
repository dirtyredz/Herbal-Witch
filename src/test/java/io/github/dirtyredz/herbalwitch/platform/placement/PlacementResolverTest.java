package io.github.dirtyredz.herbalwitch.platform.placement;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.LightingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure placement resolution: the aimed cell (or its neighbour on the aimed face), the anchor cell
 * landing on it, every cell following the facing, negative coordinates, and the same plan every
 * time for the same input, which is what lets the preview promise the placement.
 */
class PlacementResolverTest {
    private static final List<Direction> FACINGS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    /** A structure with a fixed facing and anchor; states are irrelevant to the geometry. */
    private static StructurePlacement structure(MultiblockLayout layout, Direction facing, int anchor) {
        return new StructurePlacement() {
            @Override
            public MultiblockLayout placementLayout() {
                return layout;
            }

            @Override
            public Direction layoutFacing(BlockState originState) {
                return facing;
            }

            @Override
            public int anchorCell(BlockState originState) {
                return anchor;
            }

            @Override
            public BlockState cellState(BlockState originState, int index) {
                return originState;
            }

            @Override
            public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
                return true;
            }
        };
    }

    @Test
    void theTargetIsTheReplaceableCellOrItsNeighbourOnTheAimedFace() {
        BlockPos clicked = new BlockPos(-3, 70, -12);
        assertEquals(clicked, PlacementTarget.cellFor(clicked, Direction.UP, true), "a replaceable block is replaced");
        for (Direction face : Direction.values()) {
            assertEquals(clicked.relative(face), PlacementTarget.cellFor(clicked, face, false), face.toString());
        }
    }

    @Test
    void aOneCellPieceIsPlacedExactlyOnTheTarget() {
        BlockPos target = new BlockPos(-1, -60, -1);
        PlacementPlan plan = PlacementResolver.resolve(target, null, structure(null, Direction.EAST, 0));
        assertEquals(List.of(target), plan.positions());
        assertEquals(target, plan.origin());
    }

    @Test
    void theOriginLandsOnTheTargetAndTheCellsFollowTheFacing() {
        FurnitureSpec table = SurfacesCatalog.DINING_TABLE; // 3 x 2 x 1
        MultiblockLayout layout = FurnitureLayouts.of(table);
        BlockPos target = new BlockPos(-8, 64, 5);
        for (Direction facing : FACINGS) {
            PlacementPlan plan = PlacementResolver.resolve(target, null, structure(layout, facing, 0));
            assertEquals(target, plan.origin(), facing.toString());
            assertEquals(6, new HashSet<>(plan.positions()).size(), facing + ": six distinct cells");
            assertEquals(target.relative(facing.getCounterClockWise(), 2), plan.cells().get(2).pos(), facing + ": the third cell is two to the right");
            assertEquals(target.relative(facing.getOpposite()), plan.cells().get(3).pos(), facing + ": the second row is behind");
            for (PlacementPlan.Cell cell : plan.cells()) {
                assertEquals(target.getY(), cell.pos().getY(), "a table is one high");
            }
        }
    }

    @Test
    void ceilingPiecesHangTheirTopCellFromTheAimedCeiling() {
        // the pendant (two high) is described for wave C; its layout already exists
        FurnitureSpec pendant = FurnitureCatalog.DESCRIBED.stream().filter(s -> s.id().equals("pendant_light")).findFirst().orElseThrow();
        MultiblockLayout layout = FurnitureLayouts.of(pendant);
        int anchor = FurnitureLayouts.anchorCell(pendant);
        assertEquals(1, anchor, "the upper cell is anchored");
        BlockPos underCeiling = new BlockPos(2, 80, 2);
        PlacementPlan plan = PlacementResolver.resolve(underCeiling, null, structure(layout, Direction.NORTH, anchor));
        assertEquals(underCeiling.below(), plan.origin(), "the lamp hangs below the aimed cell");
        assertEquals(underCeiling, plan.cells().get(1).pos());
        assertEquals(0, FurnitureLayouts.anchorCell(LightingCatalog.TABLE_LAMP), "standing pieces anchor their origin");
    }

    @Test
    void resolutionIsDeterministicSoThePreviewIsThePlacement() {
        MultiblockLayout layout = MultiblockLayout.box(2, 2, 3);
        BlockPos target = new BlockPos(1000, 5, -1000);
        Set<List<BlockPos>> plans = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            plans.add(PlacementResolver.resolve(target, null, structure(layout, Direction.WEST, 0)).positions());
        }
        assertEquals(1, plans.size());
        assertNotEquals(PlacementResolver.resolve(target, null, structure(layout, Direction.WEST, 0)).positions(),
                PlacementResolver.resolve(target, null, structure(layout, Direction.EAST, 0)).positions());
        assertTrue(plans.iterator().next().contains(target));
    }
}
