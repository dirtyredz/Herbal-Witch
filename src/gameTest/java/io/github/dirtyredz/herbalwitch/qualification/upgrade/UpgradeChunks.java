package io.github.dirtyredz.herbalwitch.qualification.upgrade;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Loads the fixture's chunks (from {@code chunks.txt}) at full status without making them tick. */
final class UpgradeChunks {
    // Distance 0 is ticket level 33: loaded and accessible, but neither block- nor entity-ticking.
    private static final TicketType<ChunkPos> HOLD = TicketType.create("herbalwitch_save_upgrade", Comparator.comparingLong(ChunkPos::toLong));

    private UpgradeChunks() {
    }

    static List<LevelChunk> load(ServerLevel level, Path fixture) {
        List<String> lines;
        try {
            lines = Files.readAllLines(fixture.resolve("chunks.txt"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<LevelChunk> chunks = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            String[] xz = line.trim().split(" ");
            ChunkPos pos = new ChunkPos(Integer.parseInt(xz[0]), Integer.parseInt(xz[1]));
            level.getChunkSource().addRegionTicket(HOLD, pos, 0, pos);
            chunks.add(level.getChunk(pos.x, pos.z));
        }
        return chunks;
    }
}
