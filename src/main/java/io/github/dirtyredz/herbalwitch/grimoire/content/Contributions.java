package io.github.dirtyredz.herbalwitch.grimoire.content;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe, explicitly ordered feature contributions. Lower priority reads first; equal
 * priorities keep registration order. Features own their own priority number, so the core never
 * names them and a new feature adds no edit here. Reads see a consistent snapshot.
 */
public final class Contributions<T> {
    private record Item<T>(int priority, long sequence, T value) { }

    private final List<Item<T>> items = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    public void add(int priority, T value) {
        items.add(new Item<>(priority, sequence.getAndIncrement(), value));
    }

    /** The contributions in priority order, then registration order. */
    public List<T> ordered() {
        return items.stream().sorted(Comparator.<Item<T>>comparingInt(Item::priority).thenComparingLong(Item::sequence))
                .map(Item::value).toList();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
