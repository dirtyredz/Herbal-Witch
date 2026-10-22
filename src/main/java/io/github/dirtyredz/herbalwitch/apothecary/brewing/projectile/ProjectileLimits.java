package io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bounds on thrown preparations in flight: at most {@link #PER_THROWER} per thrower and
 * {@link #PER_DIMENSION} per dimension. A projectile counts from the moment it joins a level (thrown
 * or loaded with its chunk) until it leaves it, so a launch is refused, before anything is consumed,
 * while either limit is reached.
 */
public final class ProjectileLimits {
    public static final int PER_THROWER = 4;
    public static final int PER_DIMENSION = 128;
    private static final Map<ResourceKey<Level>, Set<UUID>> ACTIVE = new HashMap<>();
    private static final Map<UUID, Set<UUID>> BY_OWNER = new HashMap<>();

    private ProjectileLimits() {
    }

    public static synchronized boolean canLaunch(ServerLevel level, Entity thrower) {
        return active(level) < PER_DIMENSION && owned(thrower.getUUID()) < PER_THROWER;
    }

    public static synchronized int active(Level level) {
        return ACTIVE.getOrDefault(level.dimension(), Set.of()).size();
    }

    public static synchronized int owned(UUID owner) {
        return BY_OWNER.getOrDefault(owner, Set.of()).size();
    }

    static synchronized void joined(Level level, UUID projectile, UUID owner) {
        ACTIVE.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(projectile);
        if (owner != null) {
            BY_OWNER.computeIfAbsent(owner, k -> new HashSet<>()).add(projectile);
        }
    }

    static synchronized void left(Level level, UUID projectile, UUID owner) {
        Set<UUID> active = ACTIVE.get(level.dimension());
        if (active != null) {
            active.remove(projectile);
        }
        if (owner != null) {
            Set<UUID> owned = BY_OWNER.get(owner);
            if (owned != null) {
                owned.remove(projectile);
                if (owned.isEmpty()) {
                    BY_OWNER.remove(owner);
                }
            }
        }
    }

    /** Server stop: nothing is in flight any more. */
    public static synchronized void clear() {
        ACTIVE.clear();
        BY_OWNER.clear();
    }
}
