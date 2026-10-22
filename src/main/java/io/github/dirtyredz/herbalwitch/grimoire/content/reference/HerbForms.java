package io.github.dirtyredz.herbalwitch.grimoire.content.reference;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The registered forms of each herb (seeds, fresh herb, dried bundle, powder), both ways: which herb
 * an item is a form of, and every form of a herb. A herb's discovery key is its plant's registry id.
 * The map is built on first use, after registration.
 */
public final class HerbForms {
    private static volatile Map<Item, HerbSpec> byItem;

    private HerbForms() {
    }

    public static EntryKey key(HerbSpec herb) {
        return EntryKey.of(EntryKey.Kind.HERB, HerbConditions.plantId(herb));
    }

    /** Seeds, fresh herb, bundle and powder, in that order. */
    public static List<Item> forms(HerbSpec herb) {
        return List.of(BotanyItems.seeds(herb), BotanyItems.fresh(herb), PreparationItems.bundle(herb), PreparationItems.powder(herb));
    }

    public static List<ItemStack> formStacks(HerbSpec herb) {
        return forms(herb).stream().map(ItemStack::new).toList();
    }

    /** The herb this item is a form of. */
    public static Optional<HerbSpec> herbOf(Item item) {
        Map<Item, HerbSpec> map = byItem;
        if (map == null) {
            Map<Item, HerbSpec> built = new HashMap<>();
            for (HerbSpec herb : HerbCatalog.ALL) {
                forms(herb).forEach(form -> built.put(form, herb));
            }
            byItem = map = Map.copyOf(built);
        }
        return Optional.ofNullable(map.get(item));
    }
}
