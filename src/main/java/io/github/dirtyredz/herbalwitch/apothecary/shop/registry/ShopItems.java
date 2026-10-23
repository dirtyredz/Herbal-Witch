package io.github.dirtyredz.herbalwitch.apothecary.shop.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain placement items: the shared checked placement never applies item block-entity data, so an
 * item (crafted, dropped or creative-copied) can never carry a shop binding.
 */
public final class ShopItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    private static final Map<ShopPieceSpec, RegistryObject<Item>> ALL = new LinkedHashMap<>();

    static {
        for (ShopPieceSpec spec : ShopPieceCatalog.ALL) {
            ALL.put(spec, ITEMS.register(spec.path(), () -> new StructurePlacementItem(ShopBlocks.block(spec), new Item.Properties())));
        }
    }

    private ShopItems() {
    }

    public static Item item(ShopPieceSpec spec) {
        return ALL.get(spec).get();
    }

    public static List<Item> all() {
        return ALL.values().stream().map(RegistryObject::get).toList();
    }
}
