package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing.FamiliarHerbCursor;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarObservations;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarSenseTest {
    @Test void cursorVisitsNearbyPositionsOnceWithoutAllocatingTheSearchVolume() {
        var cursor = new FamiliarHerbCursor(); var visited = new HashSet<BlockPos>();
        for (int i = 0; i < 4096; i++) {
            var pos = cursor.next(BlockPos.ZERO); assertTrue(visited.add(pos));
            assertTrue(Math.abs(pos.getX()) <= 8 && Math.abs(pos.getY()) <= 8 && Math.abs(pos.getZ()) <= 8);
        }
        assertTrue(visited.contains(new BlockPos(-7,-7,-7))); assertTrue(visited.contains(new BlockPos(8,8,8)));
    }
    @Test void observationsBoundResultsBeforeEncoding() {
        var mark = new FamiliarObservations.Mark(UUID.randomUUID(), 1, "Herb", "herb");
        assertThrows(IllegalArgumentException.class, () -> new FamiliarObservations(UUID.randomUUID(), "minecraft:overworld", Collections.nCopies(9, mark)));
        assertThrows(IllegalArgumentException.class, () -> new FamiliarObservations(UUID.randomUUID(), "minecraft:overworld",
                List.of(new FamiliarObservations.Mark(UUID.randomUUID(), 0, "x".repeat(65), "herb"))));
    }
}
