package io.github.dirtyredz.herbalwitch.apothecary.botany.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbFreshItem;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbSeedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Seeds and fresh herb per catalogue herb, and the herb bed's block item. The plant has no block item of its own. */
public final class BotanyItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);

    private static final Map<HerbSpec, RegistryObject<Item>> SEEDS = new LinkedHashMap<>();
    private static final Map<HerbSpec, RegistryObject<Item>> FRESH = new LinkedHashMap<>();

    public static final RegistryObject<Item> HERB_BED = ITEMS.register(BotanyBlocks.HERB_BED_PATH,
            () -> new BlockItem(BotanyBlocks.HERB_BED.get(), new Item.Properties()));

    static {
        for (HerbSpec herb : HerbCatalog.ALL) {
            SEEDS.put(herb, ITEMS.register(herb.seedsPath(),
                    () -> new HerbSeedItem(herb, BotanyBlocks.plant(herb), new Item.Properties())));
            FRESH.put(herb, ITEMS.register(herb.freshPath(), () -> new HerbFreshItem(herb, new Item.Properties())));
        }
    }

    private BotanyItems() {
    }

    public static Item seeds(HerbSpec herb) {
        return SEEDS.get(herb).get();
    }

    public static Item fresh(HerbSpec herb) {
        return FRESH.get(herb).get();
    }

    /** Gardening tab order: the bed, then per herb its seeds and fresh herb. */
    public static List<Item> all() {
        List<Item> items = new ArrayList<>();
        items.add(HERB_BED.get());
        for (HerbSpec herb : HerbCatalog.ALL) {
            items.add(seeds(herb));
            items.add(fresh(herb));
        }
        return items;
    }
}
