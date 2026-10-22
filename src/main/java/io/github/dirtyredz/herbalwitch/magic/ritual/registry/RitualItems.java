package io.github.dirtyredz.herbalwitch.magic.ritual.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The altar's item: one places the whole structure through the shared checked, previewed placement. */
public final class RitualItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);

    public static final RegistryObject<Item> ALTAR = ITEMS.register(AltarCatalog.PATH,
            () -> new StructurePlacementItem(RitualBlocks.ALTAR.get(), new Item.Properties()));

    private RitualItems() {
    }
}
