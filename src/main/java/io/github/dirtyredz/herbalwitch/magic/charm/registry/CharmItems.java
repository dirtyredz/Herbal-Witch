package io.github.dirtyredz.herbalwitch.magic.charm.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmSpec;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The charm items, ids {@code magic/charms/<charm>}. */
public final class CharmItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    private static final Map<CharmSpec, RegistryObject<Item>> BY_SPEC = new LinkedHashMap<>();

    static {
        for (CharmSpec spec : CharmCatalog.ALL) {
            BY_SPEC.put(spec, ITEMS.register(spec.path(), () -> new ProtectiveCharmItem(new Item.Properties(), spec)));
        }
    }

    private CharmItems() {
    }

    public static Item item(CharmSpec spec) {
        return BY_SPEC.get(spec).get();
    }

    public static List<Item> all() {
        return CharmCatalog.ALL.stream().map(CharmItems::item).toList();
    }
}
