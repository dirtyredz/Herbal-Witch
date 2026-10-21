package io.github.dirtyredz.herbalwitch.data.client.preparations;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.item.ProductTooltips;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.brewing.product.showcase.PreparationShowcaseSection;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

/** Bundle, powder and product names with their one-line tooltips, the oil lantern, effects and showcase labels. */
public final class PreparationTranslations {
    private PreparationTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            lang.add(PreparationBlocks.bundle(herb), "Dried " + herb.displayName() + " Bundle");
            lang.add(PreparationItems.powder(herb), "Ground " + herb.displayName());
        }
        for (ProductSpec product : EverydayProductCatalog.ALL) {
            Item item = PreparationItems.product(product);
            lang.add(item, product.displayName());
            lang.add(ProductTooltips.key(item), product.description());
        }
        lang.add(PreparationBlocks.OIL_LANTERN.get(), "Oil Lantern");
        lang.add(PreparationEffects.WARMTH.get(), "Warmth");
        lang.add(PreparationEffects.PEST_REPELLENT.get(), "Pest Repellent");
        lang.add(PreparationShowcaseSection.TITLE, "Preparations");
        lang.add(PreparationShowcaseSection.POWDERS, "Powders");
        lang.add(PreparationShowcaseSection.PRODUCTS, "Products");
        lang.add(PreparationShowcaseSection.LANTERN, "Oil lantern");
    }
}
