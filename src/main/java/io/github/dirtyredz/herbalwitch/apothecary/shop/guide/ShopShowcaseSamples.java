package io.github.dirtyredz.herbalwitch.apothecary.shop.guide;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Fixed demonstration projections: pictures of offers, never stock, never a shop record. */
public final class ShopShowcaseSamples {
    private ShopShowcaseSamples() {
    }

    public static List<ShopDisplayBlockEntity.Sample> shelf(boolean stocked) {
        return List.of(
                sample(EverydayProductCatalog.RECOVERY_TEA, stocked, 1),
                sample(EverydayProductCatalog.WARMTH_REMEDY, stocked, 2),
                sample(EverydayProductCatalog.HEALING_SALVE, stocked, 3));
    }

    public static List<ShopDisplayBlockEntity.Sample> jar(boolean stocked) {
        return List.of(sample(EverydayProductCatalog.COOLING_REMEDY, stocked, 1));
    }

    private static ShopDisplayBlockEntity.Sample sample(io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec spec, boolean available, int price) {
        return new ShopDisplayBlockEntity.Sample(new ItemStack(PreparationItems.product(spec)), available, 1, price);
    }
}
