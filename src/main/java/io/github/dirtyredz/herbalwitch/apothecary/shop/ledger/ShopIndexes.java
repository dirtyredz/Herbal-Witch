package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Derived lookups rebuilt from validated records: an owner's non-retired shops, the shop bound to a
 * register position and the shop a fixture or counter position belongs to. Unreadable shards are
 * listed as unavailable, never as absent.
 */
public final class ShopIndexes {
    private final Map<UUID, Set<UUID>> owners = new HashMap<>();
    private final Map<String, UUID> registers = new HashMap<>();
    private final Map<String, UUID> fixtures = new HashMap<>();
    private final Map<UUID, String> unavailable = new HashMap<>();
    private int active;

    public static String key(String dimension, long pos) {
        return dimension + "@" + pos;
    }

    void changed(ShopRecord before, ShopRecord after) {
        if (before != null) remove(before);
        if (after != null) add(after);
    }

    void unavailable(UUID id, String problem) {
        unavailable.put(id, problem);
    }

    private void remove(ShopRecord r) {
        if (!r.retired()) {
            active--;
            Set<UUID> mine = owners.get(r.owner);
            if (mine != null) {
                mine.remove(r.id);
                if (mine.isEmpty()) owners.remove(r.owner);
            }
        }
        if (r.binding != null) registers.remove(key(r.binding.dimension(), r.binding.pos()), r.id);
        for (ShopLink link : r.links) fixtures.remove(key(link.dimension(), link.pos()), r.id);
    }

    private void add(ShopRecord r) {
        if (!r.retired()) {
            active++;
            owners.computeIfAbsent(r.owner, k -> new LinkedHashSet<>()).add(r.id);
        }
        if (r.binding != null) registers.put(key(r.binding.dimension(), r.binding.pos()), r.id);
        for (ShopLink link : r.links) fixtures.put(key(link.dimension(), link.pos()), r.id);
    }

    public Set<UUID> owned(UUID owner) {
        return Set.copyOf(owners.getOrDefault(owner, Set.of()));
    }

    public int active() {
        return active;
    }

    public Optional<UUID> register(String dimension, long pos) {
        return Optional.ofNullable(registers.get(key(dimension, pos)));
    }

    public Optional<UUID> fixture(String dimension, long pos) {
        return Optional.ofNullable(fixtures.get(key(dimension, pos)));
    }

    public Map<UUID, String> unavailable() {
        return Map.copyOf(unavailable);
    }
}
