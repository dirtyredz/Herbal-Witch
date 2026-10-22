package io.github.dirtyredz.herbalwitch.grimoire.content.reference;

import io.github.dirtyredz.herbalwitch.grimoire.content.Contributions;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/** Feature-registered forms of a discovery. Suppliers resolve only after item registration. */
public final class DiscoveryForms {
    public enum Order { RAW, PREPARED }
    private static final Map<EntryKey, Contributions<Supplier<List<Item>>>> FORMS = new ConcurrentHashMap<>();
    /** First-registration order of the keys. */
    private static final List<EntryKey> KEYS = new CopyOnWriteArrayList<>();
    private static volatile Map<Item, EntryKey> byItem;

    private DiscoveryForms() { }

    public static synchronized void register(EntryKey key, Order order, Supplier<List<Item>> items) {
        FORMS.computeIfAbsent(key, ignored -> {
            KEYS.add(key);
            return new Contributions<>();
        }).add(order.ordinal(), items);
        byItem = null;
    }

    public static List<EntryKey> keys() {
        return List.copyOf(KEYS);
    }

    public static List<Item> forms(EntryKey key) {
        Contributions<Supplier<List<Item>>> forms = FORMS.get(key);
        return forms == null ? List.of() : forms.ordered().stream().flatMap(f -> f.get().stream()).toList();
    }

    public static List<ItemStack> formStacks(EntryKey key) {
        return forms(key).stream().map(ItemStack::new).toList();
    }

    public static Optional<EntryKey> keyOf(Item item) {
        Map<Item, EntryKey> map = byItem;
        if (map == null) {
            Map<Item, EntryKey> built = new HashMap<>();
            KEYS.forEach(key -> forms(key).forEach(form -> built.put(form, key)));
            byItem = map = Map.copyOf(built);
        }
        return Optional.ofNullable(map.get(item));
    }
}
