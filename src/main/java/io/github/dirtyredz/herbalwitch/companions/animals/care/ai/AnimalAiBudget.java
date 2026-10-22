package io.github.dirtyredz.herbalwitch.companions.animals.care.ai;

import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Pure dimension-local counters. Active leases are per animal; calming can replace its bait lease. */
public final class AnimalAiBudget {
    private final Map<UUID, UUID> active = new HashMap<>();
    private final Map<UUID, Integer> holders = new HashMap<>();
    private long tick = Long.MIN_VALUE;
    private int checks, paths;
    public void begin(long now) { if (tick != now) { tick = now; checks = 0; paths = 0; } }
    public boolean candidate(AnimalRules rules) { if (checks >= rules.checksPerTick()) return false; checks++; return true; }
    public boolean path(AnimalRules rules) { if (paths >= rules.pathsPerTick()) return false; paths++; return true; }
    public int checks() { return checks; }
    public int paths() { return paths; }
    public int active() { return active.size(); }
    public boolean claim(UUID animal, UUID holder, AnimalRules rules) {
        if (active.containsKey(animal) && Objects.equals(active.get(animal), holder)) return true;
        if (!active.containsKey(animal) && active.size() >= rules.activeGoals()) return false;
        if (holder != null && holders.getOrDefault(holder, 0) >= rules.attractedPerPlayer()) return false;
        releaseAll(animal);
        active.put(animal, holder);
        if (holder != null) holders.merge(holder, 1, Integer::sum);
        return true;
    }
    public void release(UUID animal, UUID holder) {
        if (active.containsKey(animal) && Objects.equals(active.get(animal), holder)) releaseAll(animal);
    }
    public void releaseAll(UUID animal) {
        UUID prior = active.remove(animal);
        if (prior != null) { int left = holders.getOrDefault(prior, 1) - 1; if (left == 0) holders.remove(prior); else holders.put(prior, left); }
    }
}
