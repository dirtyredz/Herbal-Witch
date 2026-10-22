package io.github.dirtyredz.herbalwitch.magic.ward;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardIndex;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The loaded-ward index: box bounds, one-chunk lookups across chunk edges, overlap, dimension isolation, immediate removal. */
class WardIndexTest {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final String NETHER = "minecraft:the_nether";
    private static final UUID OWNER = UUID.nameUUIDFromBytes("owner".getBytes());

    private static WardIndex.Entry ward(long id, String dim, BlockPos centre, int radius) {
        return new WardIndex.Entry(id, dim, centre, radius, 4, WardCatalog.Profile.BOTH, OWNER);
    }

    @Test
    void theVolumeIsABoxOfRadiusAndHeight() {
        WardIndex.Entry w = ward(1, OVERWORLD, new BlockPos(0, 64, 0), 8);
        assertTrue(w.contains(new BlockPos(8, 68, -8)));
        assertFalse(w.contains(new BlockPos(9, 64, 0)));
        assertFalse(w.contains(new BlockPos(0, 69, 0)));
    }

    @Test
    void lookupsFindWardsAcrossChunkEdgesAndOnlyInTheirDimension() {
        WardIndex index = new WardIndex();
        index.add(ward(1, OVERWORLD, new BlockPos(14, 64, 14), 8));
        assertEquals(1, index.covering(OVERWORLD, new BlockPos(20, 64, 20)).size(), "a neighbouring chunk inside the box");
        assertEquals(1, index.covering(OVERWORLD, new BlockPos(6, 64, 6)).size());
        assertTrue(index.covering(OVERWORLD, new BlockPos(23, 64, 14)).isEmpty(), "just outside");
        assertTrue(index.covering(NETHER, new BlockPos(14, 64, 14)).isEmpty(), "another dimension");
        index.remove(1);
        assertTrue(index.covering(OVERWORLD, new BlockPos(14, 64, 14)).isEmpty(), "removal is immediate");
        assertEquals(0, index.active(OVERWORLD));
    }

    @Test
    void overlapsCountOnlyOtherActiveWardsOfTheSameDimension() {
        WardIndex index = new WardIndex();
        index.add(ward(1, OVERWORLD, new BlockPos(0, 64, 0), 8));
        assertTrue(index.overlaps(OVERWORLD, new BlockPos(16, 64, 0), 8, 4, -1), "edges touch");
        assertFalse(index.overlaps(OVERWORLD, new BlockPos(17, 64, 0), 8, 4, -1));
        assertFalse(index.overlaps(OVERWORLD, new BlockPos(0, 64, 0), 8, 4, 1), "a ward never overlaps itself");
        assertFalse(index.overlaps(NETHER, new BlockPos(0, 64, 0), 8, 4, -1));
    }

    @Test
    void centresAreCountedPerChunk() {
        WardIndex index = new WardIndex();
        index.add(ward(1, OVERWORLD, new BlockPos(1, 64, 1), 1));
        index.add(ward(2, OVERWORLD, new BlockPos(10, 64, 10), 1));
        index.add(ward(3, OVERWORLD, new BlockPos(20, 64, 1), 1));
        assertEquals(2, index.centres(OVERWORLD, new BlockPos(5, 0, 5)));
        assertEquals(3, index.active(OVERWORLD));
    }
}
