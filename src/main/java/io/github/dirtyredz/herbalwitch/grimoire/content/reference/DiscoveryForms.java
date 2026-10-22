package io.github.dirtyredz.herbalwitch.grimoire.content.reference;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Feature-registered forms of a discovery. Suppliers resolve only after item registration. */
public final class DiscoveryForms {
    public enum Order { RAW, PREPARED }
    private record Forms(Order order, Supplier<List<Item>> items) { }
    private static final Map<EntryKey, List<Forms>> FORMS = new LinkedHashMap<>();
    private static volatile Map<Item, EntryKey> byItem;

    private DiscoveryForms() { }

    public static void register(EntryKey key, Order order, Supplier<List<Item>> items) {
        FORMS.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new Forms(order, items));
        byItem = null;
    }

    public static List<EntryKey> keys() {
        return List.copyOf(FORMS.keySet());
    }

    public static List<Item> forms(EntryKey key) {
        return FORMS.getOrDefault(key, List.of()).stream().sorted(Comparator.comparing(Forms::order))
                .flatMap(f -> f.items().get().stream()).toList();
    }

    public static List<ItemStack> formStacks(EntryKey key) {
        return forms(key).stream().map(ItemStack::new).toList();
    }

    public static Optional<EntryKey> keyOf(Item item) {
        Map<Item, EntryKey> map = byItem;
        if (map == null) {
            Map<Item, EntryKey> built = new HashMap<>();
            FORMS.keySet().forEach(key -> forms(key).forEach(form -> built.put(form, key)));
            byItem = map = Map.copyOf(built);
        }
        return Optional.ofNullable(map.get(item));
    }
}
