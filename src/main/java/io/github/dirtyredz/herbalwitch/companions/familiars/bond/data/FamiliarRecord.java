package io.github.dirtyredz.herbalwitch.companions.familiars.bond.data;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import net.minecraft.nbt.CompoundTag;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** One immutable bond snapshot. State and recovery NBT never escape by reference. */
public record FamiliarRecord(UUID bond, UUID owner, UUID creature, String species, String originalType,
                             long generation, Phase phase, UUID transaction, long revision,
                             CompoundTag state, CompoundTag payload) {
    public enum Phase { BINDING_PREPARED, ACTIVE, INCAPACITATION_PREPARED, INCAPACITATED, REVIVAL_PREPARED, TRANSFER_PREPARED, QUARANTINED }
    public FamiliarRecord {
        Objects.requireNonNull(bond); Objects.requireNonNull(owner); Objects.requireNonNull(creature);
        Objects.requireNonNull(transaction); Objects.requireNonNull(phase);
        var spec = FamiliarCatalog.find(species).orElseThrow(() -> new IllegalArgumentException("Unknown familiar species"));
        if (!spec.originalType().equals(originalType) || generation < 1 || revision < 1)
            throw new IllegalArgumentException("Invalid familiar identity");
        state = Objects.requireNonNull(state).copy(); payload = Objects.requireNonNull(payload).copy();
        FamiliarCommand.parse(state.getString("command"));
        if (phase != Phase.ACTIVE && phase != Phase.QUARANTINED && payload.isEmpty())
            throw new IllegalArgumentException("Missing recovery payload");
    }
    @Override public CompoundTag state() { return state.copy(); }
    @Override public CompoundTag payload() { return payload.copy(); }
    // Hot-path reads (every loaded familiar, every tick) without copying the whole state (M12 performance).
    /** Whether the state holds {@code key}. */
    public boolean has(String key) { return state.contains(key); }
    /** One top-level compound of the state, copied on its own. */
    public CompoundTag compound(String key) { return state.getCompound(key).copy(); }
    public int intValue(String key) { return state.getInt(key); }
    public long longValue(String key) { return state.getLong(key); }
    public String stringValue(String key) { return state.getString(key); }
    /** Loaded-time counters that change every tick and never make an owner's open view stale. */
    private static final java.util.Set<String> COUNTERS = java.util.Set.of("homeRest", "charmTicks", "progress", "homeProgress",
            "preparationGates", "poisonAt", "poisonCoating");
    /** Whether the owner-visible intent is equal: everything but loaded-time counters; buffs by name only. */
    public boolean sameIntent(FamiliarRecord other) {
        var keys = new java.util.HashSet<>(state.getAllKeys()); keys.addAll(other.state.getAllKeys());
        for (String key : keys) {
            if (COUNTERS.contains(key)) continue;
            if (key.equals("buffs")) { if (!state.getCompound(key).getAllKeys().equals(other.state.getCompound(key).getAllKeys())) return false; }
            else if (!Objects.equals(state.get(key), other.state.get(key))) return false;
        }
        return true;
    }
    public FamiliarCommand command() { return FamiliarCommand.parse(state.getString("command")); }
    public FamiliarRecord change(Consumer<CompoundTag> change) {
        CompoundTag next = state.copy(); change.accept(next);
        return new FamiliarRecord(bond, owner, creature, species, originalType, generation, phase, transaction, Math.addExact(revision, 1), next, payload);
    }
    public FamiliarRecord transition(Phase next, long incarnation, UUID operation, CompoundTag recovery) {
        if (incarnation < generation) throw new IllegalArgumentException("Generation cannot decrease");
        return new FamiliarRecord(bond, owner, creature, species, originalType, incarnation, next, operation, Math.addExact(revision, 1), state, recovery);
    }
    public FamiliarRecord quarantine() { return transition(Phase.QUARANTINED, generation, transaction, payload); }
    public boolean interrupted() { return phase != Phase.ACTIVE && phase != Phase.INCAPACITATED && phase != Phase.QUARANTINED
            || phase == Phase.ACTIVE && !payload.isEmpty() || state.contains("receipt"); }
    public static FamiliarRecord binding(UUID owner, UUID creature, String species, long generation, CompoundTag payload) {
        CompoundTag state = new CompoundTag(); state.putString("command", "stay");
        return new FamiliarRecord(UUID.randomUUID(), owner, creature, species, FamiliarCatalog.find(species).orElseThrow().originalType(),
                generation, Phase.BINDING_PREPARED, UUID.randomUUID(), 1, state, payload);
    }
}
