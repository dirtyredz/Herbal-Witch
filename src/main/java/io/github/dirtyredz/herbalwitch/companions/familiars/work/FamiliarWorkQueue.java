package io.github.dirtyredz.herbalwitch.companions.familiars.work;

import java.util.*;
import java.util.function.Consumer;

/** One outstanding turn per identity. Removal before execution permits fair re-enqueue at the tail. */
public final class FamiliarWorkQueue<T> {
    private final LinkedHashSet<T> waiting = new LinkedHashSet<>();
    public void offer(T task) { waiting.add(task); }
    public void remove(T task) { waiting.remove(task); }
    public int run(int limit, Consumer<T> operation) {
        int turns = Math.min(limit, waiting.size());
        for (int i = 0; i < turns; i++) {
            var iterator = waiting.iterator(); T next = iterator.next(); iterator.remove(); operation.accept(next);
        }
        return turns;
    }
    public int size() { return waiting.size(); }
}
