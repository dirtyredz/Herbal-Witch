package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** Each station's block item, under the block's id. */
public final class StationItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);

    static {
        for (StationSpec spec : StationCatalog.ALL) {
            ITEMS.register(spec.path(), () -> new BlockItem(StationBlocks.block(spec), new Item.Properties()));
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
