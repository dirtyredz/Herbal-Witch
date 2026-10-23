package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import java.util.*;

/** Derived occupancy: one bond per home instance, including unloaded or incapacitated occupants. */
public final class FamiliarHomeIndex {
    private final Map<UUID, UUID> occupants = new HashMap<>(), homes = new HashMap<>();
    public Optional<UUID> occupant(UUID instance) { return Optional.ofNullable(occupants.get(instance)); }
    public boolean accepts(FamiliarRecord record) {
        var home = record.state().getCompound("home");
        return !home.hasUUID("instance") || occupant(home.getUUID("instance")).filter(id -> !id.equals(record.bond())).isEmpty();
    }
    public boolean changed(FamiliarRecord record) {
        if (!accepts(record)) return false;
        UUID previous = homes.remove(record.bond()); if (previous != null) occupants.remove(previous, record.bond());
        var home = record.state().getCompound("home");
        if (home.hasUUID("instance")) { UUID instance = home.getUUID("instance"); occupants.put(instance, record.bond()); homes.put(record.bond(), instance); }
        return true;
    }
}
