package io.github.dirtyredz.herbalwitch.apothecary.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Each station's block item, under the block's id. The production stations place through the shared
 * checked, previewed structure placement (the whole station or nothing); the everyday ones stay plain
 * block items.
 */
public final class StationItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);

    static {
        for (StationSpec spec : StationCatalog.ALL) {
            ITEMS.register(spec.path(), () -> StationCatalog.ADVANCED.contains(spec)
                    ? new StructurePlacementItem(StationBlocks.block(spec), new Item.Properties())
                    : new BlockItem(StationBlocks.block(spec), new Item.Properties()));
        }
    }

    private StationItems() {
    }

    public static Item item(StationSpec spec) {
        return StationBlocks.block(spec).asItem();
    }

    public static List<Item> all() {
        return StationCatalog.ALL.stream().map(StationItems::item).toList();
    }
}
