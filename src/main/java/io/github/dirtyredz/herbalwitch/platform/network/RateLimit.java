package io.github.dirtyredz.herbalwitch.platform.network;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * At most {@code max} requests per player within any {@code window} server ticks; requests beyond
 * that are refused before any handler runs. Pure, keyed by the sender's id (never a payload id),
 * forgotten when the player leaves.
 */
public final class RateLimit {
    private final int max;
    private final int window;
    private final Map<UUID, Deque<Long>> recent = new HashMap<>();

    public RateLimit(int max, int window) {
        this.max = max;
        this.window = window;
    }

    /** Records a request at {@code tick}; false (nothing recorded) when the player is over the limit. */
    public boolean allow(UUID player, long tick) {
        Deque<Long> times = recent.computeIfAbsent(player, k -> new ArrayDeque<>());
        while (!times.isEmpty() && tick - times.peekFirst() >= window) {
            times.pollFirst();
        }
        if (times.size() >= max) {
            return false;
        }
        times.addLast(tick);
        return true;
    }

    public void forget(UUID player) {
        recent.remove(player);
    }
}
