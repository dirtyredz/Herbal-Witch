package io.github.dirtyredz.herbalwitch.companions.lifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/** Loaded object identities only; absence never means dead, recoverable, or safe to reconstruct. */
public final class CompanionEntityIndex<T> {
    private final Map<UUID, T> loaded = new HashMap<>();
    private final Function<T, UUID> identity;
    private final Predicate<T> present;

    public CompanionEntityIndex(Function<T, UUID> identity, Predicate<T> present) {
        this.identity = Objects.requireNonNull(identity);
        this.present = Objects.requireNonNull(present);
    }

    /** Idempotent for the exact object, refusing a second live incarnation with the same UUID. */
    public boolean admit(T entity) {
        if (!present.test(entity)) return false;
        UUID id = identity.apply(entity);
        T prior = current(id).orElse(null);
        if (prior != null && prior != entity) return false;
        loaded.put(id, entity);
        return true;
    }

    public Optional<T> current(UUID id) {
        T entity = loaded.get(id);
        if (entity != null && !present.test(entity)) {
            loaded.remove(id);
            entity = null;
        }
        return Optional.ofNullable(entity);
    }

    /** A delayed leave event from an earlier incarnation cannot remove its replacement. */
    public void leave(T entity) {
        UUID id = identity.apply(entity);
        if (loaded.get(id) == entity) loaded.remove(id);
    }

    public Optional<T> remove(UUID id) { return Optional.ofNullable(loaded.remove(id)); }
}
