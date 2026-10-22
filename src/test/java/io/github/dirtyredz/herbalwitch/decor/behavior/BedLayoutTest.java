package io.github.dirtyredz.herbalwitch.decor.behavior;

import io.github.dirtyredz.herbalwitch.decor.behavior.sleeping.BedLayout;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bed head/foot and frame cells in every facing: the head lies where vanilla's facing points. */
class BedLayoutTest {
    private static FurnitureSpec bed(String id) {
        return FurnitureCatalog.DESCRIBED.stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void theHeadIsWhereVanillaFacingPointsInEveryFacing() {
        for (String id : List.of("bed", "canopy_bed")) {
            MultiblockLayout layout = FurnitureLayouts.of(bed(id));
            assertEquals("cell", layout.part().getName(), id + ": vanilla's bed owns 'part'");
            int head = BedLayout.head(layout);
            for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                BlockPos foot = new BlockPos(3, 64, -9);
                assertEquals(foot.relative(facing), layout.cellPos(foot, BedLayout.front(facing), head), id + " " + facing);
            }
            assertEquals(BedPart.FOOT, BedLayout.part(layout, 0));
            assertEquals(BedPart.HEAD, BedLayout.part(layout, head));
            int pair = 0;
            for (int i = 0; i < layout.size(); i++) {
                pair += BedLayout.sleepingPair(layout, i) ? 1 : 0;
            }
            assertEquals(2, pair, id + ": one sleeping pair, one sleeper");
        }
        MultiblockLayout canopy = FurnitureLayouts.of(bed("canopy_bed"));
        assertEquals(12, canopy.size());
        assertFalse(BedLayout.sleepingPair(canopy, 1), "the canopy's side cell is frame");
        assertTrue(BedLayout.sleepingPair(canopy, BedLayout.head(canopy)));
    }
}
