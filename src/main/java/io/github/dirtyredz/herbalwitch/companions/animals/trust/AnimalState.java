package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Durable animal care only. Mutators are package-private; M9 consumes the service's read views. */
public final class AnimalState {
    final Map<UUID, AnimalRelationshipView> relationships = new LinkedHashMap<>();
    long clock;
    long hurtUntil;
    long healUntil;
    long calmUntil;
    long calmBlockedUntil;
    UUID calmSource;
    UUID keeper;
    long generation;

    public AnimalRelationshipView relationship(UUID player) {
        return relationships.getOrDefault(player, AnimalRelationshipView.EMPTY);
    }

    public int relationshipCount() { return relationships.size(); }
    public long clock() { return clock; }
    public long hurtRemaining() { return Math.max(0, hurtUntil - clock); }
    public long healRemaining() { return Math.max(0, healUntil - clock); }
    public long calmRemaining() { return Math.max(0, calmUntil - clock); }
    public long calmLockoutRemaining() { return Math.max(0, calmBlockedUntil - clock); }
    public Optional<UUID> calmSource() { return Optional.ofNullable(calmSource); }
    public Optional<UUID> keeper() { return Optional.ofNullable(keeper); }
    public long generation() { return generation; }

    boolean feed(UUID player, int capacity) {
        var before = relationship(player);
        if (player == null || capacity < 1 || capacity > TrustRules.MAX_CAPACITY
                || before.trust() == TrustRules.MAX_TRUST || before.remaining(clock) > 0 || hurtRemaining() > 0
                || (!relationships.containsKey(player) && relationships.size() >= capacity)
                || clock > Long.MAX_VALUE - TrustRules.FEED_INTERVAL) return false;
        relationships.put(player, new AnimalRelationshipView(Math.min(100, before.trust() + TrustRules.FEED_GAIN),
                Math.min(1_000_000, before.feeds() + 1), clock + TrustRules.FEED_INTERVAL));
        return true;
    }

    void tick() {
        if (clock < Long.MAX_VALUE - TrustRules.FEED_INTERVAL) clock++;
        if (calmRemaining() == 0) calmSource = null;
    }

    void damaged(UUID player) {
        hurtUntil = clock + TrustRules.HURT_LOCKOUT;
        calmBlockedUntil = clock + TrustRules.CALM_LOCKOUT;
        endCalm();
        var before = relationships.get(player);
        if (before != null) relationships.put(player, new AnimalRelationshipView(Math.max(0, before.trust() - TrustRules.HURT_LOSS),
                before.feeds(), before.nextFeed()));
    }

    void endCalm() {
        calmUntil = clock;
        calmSource = null;
    }
}
