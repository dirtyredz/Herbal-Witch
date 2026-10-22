package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The loaded, charged wards of each dimension, indexed by every chunk their volume touches, so a
 * target-acquisition event asks only the chunk it happens in (never every record or every entity).
 * Pure: entries are copied facts (centre, effective radius, height, profile, owner). A ward's volume
 * is a box: {@code radius} blocks horizontally each way and {@code height} vertically.
 */
public final class WardIndex {
    /** One indexed ward. */
    public record Entry(long id, String dimension, BlockPos centre, int radius, int height, WardCatalog.Profile profile, UUID owner) {
        public boolean contains(BlockPos pos) {
            return Math.abs(pos.getX() - centre.getX()) <= radius && Math.abs(pos.getZ() - centre.getZ()) <= radius
                    && Math.abs(pos.getY() - centre.getY()) <= height;
        }

        /** Whether two ward volumes share any block. */
        public boolean overlaps(BlockPos otherCentre, int otherRadius, int otherHeight) {
            return Math.abs(otherCentre.getX() - centre.getX()) <= radius + otherRadius
                    && Math.abs(otherCentre.getZ() - centre.getZ()) <= radius + otherRadius
                    && Math.abs(otherCentre.getY() - centre.getY()) <= height + otherHeight;
        }

        long centreChunk() {
            return ChunkPos.asLong(centre.getX() >> 4, centre.getZ() >> 4);
        }
    }

    private final Map<Long, Entry> entries = new LinkedHashMap<>();
    private final Map<String, Map<Long, List<Entry>>> byChunk = new HashMap<>();

    /** Adds (or replaces) a ward. */
    public void add(Entry entry) {
        remove(entry.id());
        entries.put(entry.id(), entry);
        Map<Long, List<Entry>> chunks = byChunk.computeIfAbsent(entry.dimension(), k -> new HashMap<>());
        for (long chunk : chunks(entry)) {
            chunks.computeIfAbsent(chunk, k -> new ArrayList<>()).add(entry);
        }
    }

    /** Removes a ward at once (deactivated, unloaded, retired). */
    public void remove(long id) {
        Entry entry = entries.remove(id);
        if (entry == null) {
            return;
        }
        Map<Long, List<Entry>> chunks = byChunk.get(entry.dimension());
        if (chunks == null) {
            return;
        }
        for (long chunk : chunks(entry)) {
            List<Entry> list = chunks.get(chunk);
            if (list != null) {
                list.removeIf(e -> e.id() == id);
                if (list.isEmpty()) {
                    chunks.remove(chunk);
                }
            }
        }
    }

    public boolean contains(long id) {
        return entries.containsKey(id);
    }

    public Entry entry(long id) {
        return entries.get(id);
    }

    /** The wards whose volume holds {@code pos}: one chunk lookup. */
    public List<Entry> covering(String dimension, BlockPos pos) {
        Map<Long, List<Entry>> chunks = byChunk.get(dimension);
        if (chunks == null) {
            return List.of();
        }
        List<Entry> list = chunks.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (list == null) {
            return List.of();
        }
        return list.stream().filter(e -> e.contains(pos)).toList();
    }

    /** Active wards in a dimension. */
    public int active(String dimension) {
        return (int) entries.values().stream().filter(e -> e.dimension().equals(dimension)).count();
    }

    /** Active ward centres in the chunk holding {@code pos}. */
    public int centres(String dimension, BlockPos pos) {
        long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        return (int) entries.values().stream().filter(e -> e.dimension().equals(dimension) && e.centreChunk() == chunk).count();
    }

    /** Whether a volume would overlap an active ward of the dimension other than {@code except}. */
    public boolean overlaps(String dimension, BlockPos centre, int radius, int height, long except) {
        for (Entry e : entries.values()) {
            if (e.id() != except && e.dimension().equals(dimension) && e.overlaps(centre, radius, height)) {
                return true;
            }
        }
        return false;
    }

    public List<Entry> all() {
        return List.copyOf(entries.values());
    }

    public void clear() {
        entries.clear();
        byChunk.clear();
    }

    private static List<Long> chunks(Entry e) {
        List<Long> out = new ArrayList<>();
        int x0 = (e.centre().getX() - e.radius()) >> 4;
        int x1 = (e.centre().getX() + e.radius()) >> 4;
        int z0 = (e.centre().getZ() - e.radius()) >> 4;
        int z1 = (e.centre().getZ() + e.radius()) >> 4;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                out.add(ChunkPos.asLong(x, z));
            }
        }
        return out;
    }
}
