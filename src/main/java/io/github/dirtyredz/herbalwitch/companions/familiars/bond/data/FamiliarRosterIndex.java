package io.github.dirtyredz.herbalwitch.companions.familiars.bond.data;

import java.util.*;

/** Derived owner keyset index. Page work is bounded independently of total bonds. */
public final class FamiliarRosterIndex {
    private final Map<UUID, NavigableSet<UUID>> owners = new HashMap<>();
    private final Map<UUID, Long> revisions = new HashMap<>();
    public void changed(FamiliarRecord record) {
        owners.computeIfAbsent(record.owner(), ignored -> new TreeSet<>()).add(record.bond());
        revisions.merge(record.owner(), 1L, Math::addExact);
    }
    public long revision(UUID owner) { return revisions.getOrDefault(owner, 0L); }
    public List<UUID> page(UUID owner, UUID after, int maximum) {
        if (maximum < 1 || maximum > 32) throw new IllegalArgumentException("Invalid roster page size");
        var bonds = owners.get(owner); if (bonds == null) return List.of();
        return (after == null ? bonds : bonds.tailSet(after, false)).stream().limit(maximum).toList();
    }
}
