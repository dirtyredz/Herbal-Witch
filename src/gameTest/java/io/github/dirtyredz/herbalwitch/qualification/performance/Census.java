package io.github.dirtyredz.herbalwitch.qualification.performance;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.FamiliarWork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** Loaded populations at one instant: entities by type, Herbal Witch block entities by type, chunks and work budgets. */
final class Census {
    private Census() {
    }

    static Map<String, Object> take(ServerLevel level) {
        Map<String, Integer> entities = new TreeMap<>();
        for (Entity entity : level.getAllEntities()) {
            entities.merge(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())), 1, Integer::sum);
        }
        Map<String, Integer> blockEntities = new TreeMap<>();
        int chunks = 0;
        for (long packed : level.getForcedChunks()) {
            ChunkPos pos = new ChunkPos(packed);
            var chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk == null) continue;
            chunks++;
            chunk.getBlockEntities().values().forEach(be -> {
                var id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
                if (id != null && id.getNamespace().equals(HerbalWitch.MOD_ID)) blockEntities.merge(id.toString(), 1, Integer::sum);
            });
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("entities", entities);
        out.put("entityTotal", entities.values().stream().mapToInt(Integer::intValue).sum());
        out.put("herbalWitchBlockEntities", blockEntities);
        out.put("herbalWitchBlockEntityTotal", blockEntities.values().stream().mapToInt(Integer::intValue).sum());
        out.put("forcedChunks", chunks);
        out.put("loadedChunks", level.getChunkSource().getLoadedChunksCount());
        var work = FamiliarWork.in(level);
        out.put("familiarScansLastTick", work.lastScans());
        out.put("familiarPathsLastTick", work.lastPaths());
        return out;
    }
}
