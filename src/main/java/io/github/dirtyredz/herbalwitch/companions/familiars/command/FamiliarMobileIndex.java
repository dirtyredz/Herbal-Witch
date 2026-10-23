package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import java.util.*;

/** Derived from canonical assignments, including unloaded entities. Never walks the full roster. */
public final class FamiliarMobileIndex {
    private record Reservation(long order, UUID bond) implements Comparable<Reservation> {
        @Override public int compareTo(Reservation other) { int time = Long.compare(order, other.order); return time == 0 ? bond.compareTo(other.bond) : time; }
    }
    private final Map<UUID, NavigableSet<Reservation>> owners = new HashMap<>();
    private final Map<UUID, Reservation> bonds = new HashMap<>();
    private long sequence;
    public void changed(FamiliarRecord record) {
        var set = owners.computeIfAbsent(record.owner(), ignored -> new TreeSet<>());
        var previous = bonds.remove(record.bond()); if (previous != null) set.remove(previous);
        if (record.phase() == FamiliarRecord.Phase.ACTIVE && record.command().mobile()) {
            long order = record.state().getLong("mobileOrder");
            sequence = Math.max(sequence, order); var reservation = new Reservation(order, record.bond());
            set.add(reservation); bonds.put(record.bond(), reservation);
        }
        if (set.isEmpty()) owners.remove(record.owner());
    }
    public long nextOrder() { return sequence = Math.addExact(sequence, 1); }
    public List<UUID> assignments(UUID owner) { return owners.getOrDefault(owner, Collections.emptyNavigableSet()).stream().map(Reservation::bond).toList(); }
    public int count(UUID owner) { return owners.getOrDefault(owner, Collections.emptyNavigableSet()).size(); }
    public Set<UUID> owners() { return Set.copyOf(owners.keySet()); }
}
