package io.github.dirtyredz.herbalwitch.companions.familiars.bond.data;

import java.util.*;

/** Derived UUID lineage index. Duplicate authority is a corruption, never last-writer-wins. */
public final class FamiliarEntityIndex {
    private final Map<UUID, UUID> bonds = new HashMap<>();
    public boolean add(FamiliarRecord record) {
        UUID previous = bonds.putIfAbsent(record.creature(), record.bond());
        return previous == null || previous.equals(record.bond());
    }
    public Optional<UUID> bond(UUID creature) { return Optional.ofNullable(bonds.get(creature)); }
}
