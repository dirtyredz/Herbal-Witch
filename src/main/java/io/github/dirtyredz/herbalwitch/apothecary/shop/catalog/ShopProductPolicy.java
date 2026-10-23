package io.github.dirtyredz.herbalwitch.apothecary.shop.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * What may be traded, built only from the existing explicit catalogues (never a namespace prefix):
 * seeds and fresh herbs, dried bundles and powders, every everyday product, the finished advanced and
 * familiar preparations and the protective charms. Four-dose mixtures, currency, furniture, stations,
 * tools, capture charms, creature tokens and anything with a nested inventory or unknown capability
 * data are refused. A stack is accepted only if its tag passes {@link ShopMetadataRules} unchanged.
 */
public final class ShopProductPolicy {
    public enum Refusal { NONE, EMPTY, NOT_TRADEABLE, METADATA, CAPABILITY, DAMAGED }

    private static volatile List<Item> items;

    private ShopProductPolicy() {
    }

    /** Every tradeable item, in catalogue order (registries must be frozen). */
    public static List<Item> items() {
        List<Item> cached = items;
        if (cached != null) return cached;
        Set<Item> out = new LinkedHashSet<>();
        for (HerbSpec herb : HerbCatalog.ALL) {
            out.add(BotanyItems.seeds(herb));
            out.add(BotanyItems.fresh(herb));
        }
        for (HerbSpec herb : HerbCatalog.ALL) {
            out.add(PreparationItems.bundle(herb));
            out.add(PreparationItems.powder(herb));
        }
        for (ProductSpec spec : EverydayProductCatalog.ALL) out.add(PreparationItems.product(spec));
        for (ProductSpec spec : AdvancedProductCatalog.ALL) out.add(AdvancedPreparations.item(spec));
        for (ProductSpec spec : FamiliarPreparationCatalog.ALL) out.add(AdvancedPreparations.item(spec));
        out.addAll(CharmItems.all());
        items = cached = Collections.unmodifiableList(new ArrayList<>(out));
        return cached;
    }

    public static boolean tradeable(Item item) {
        return items().contains(item);
    }

    /** Registry ids of every tradeable item, the order board's request catalogue. */
    public static List<String> ids() {
        return items().stream().map(item -> String.valueOf(ForgeRegistries.ITEMS.getKey(item))).toList();
    }

    public static boolean tradeable(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key != null && ForgeRegistries.ITEMS.containsKey(key) && tradeable(ForgeRegistries.ITEMS.getValue(key));
    }

    /** Whether this exact stack (item, tag and attached data) may be stocked and sold unchanged. */
    public static Refusal check(ItemStack stack) {
        if (stack.isEmpty()) return Refusal.EMPTY;
        if (!tradeable(stack.getItem())) return Refusal.NOT_TRADEABLE;
        if (stack.isDamaged()) return Refusal.DAMAGED;
        CompoundTag saved = stack.save(new CompoundTag());
        if (saved.contains("ForgeCaps") && !saved.getCompound("ForgeCaps").isEmpty()) return Refusal.CAPABILITY;
        if (!ShopMetadataRules.accepts(stack.getItem(), stack.getTag())) return Refusal.METADATA;
        return Refusal.NONE;
    }
}
