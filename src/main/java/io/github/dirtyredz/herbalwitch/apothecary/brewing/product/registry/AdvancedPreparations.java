package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication.Drink;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.OilApplication.Oil;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications.Burst;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HerbalDrinkItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HerbalOilItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.FamiliarPreparationItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.PreparedMixtureItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.ThrowablePreparationItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The Milestone 7 preparations: advanced player preparations, familiar preparations and the cauldron
 * mixtures, each with the item class its kind needs (one class per delivery, never one per product).
 * Everything stacks to 16.
 */
public final class AdvancedPreparations {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    private static final Map<ProductSpec, RegistryObject<Item>> PRODUCTS = new LinkedHashMap<>();

    static {
        for (ProductSpec spec : AdvancedProductCatalog.ALL) {
            PRODUCTS.put(spec, ITEMS.register(spec.path(), factory(spec)));
        }
        for (ProductSpec spec : FamiliarPreparationCatalog.ALL) {
            PRODUCTS.put(spec, ITEMS.register(spec.path(), () -> new FamiliarPreparationItem(props(), spec)));
        }
        for (ProductSpec spec : MixtureCatalog.specs()) {
            PRODUCTS.put(spec, ITEMS.register(spec.path(), () -> new PreparedMixtureItem(props())));
        }
    }

    private AdvancedPreparations() {
    }

    private static Item.Properties props() {
        return new Item.Properties().stacksTo(16);
    }

    private static Supplier<Item> factory(ProductSpec spec) {
        return switch (spec.id()) {
            case "night_vision" -> () -> new HerbalDrinkItem(props(), Drink.NIGHT_VISION, true);
            case "water_breathing" -> () -> new HerbalDrinkItem(props(), Drink.WATER_BREATHING, true);
            case "resistance" -> () -> new HerbalDrinkItem(props(), Drink.RESISTANCE, true);
            case "healing" -> () -> new HerbalDrinkItem(props(), Drink.HEALING, true);
            case "poison" -> throwable(Burst.POISON);
            case "concealment" -> throwable(Burst.CONCEALMENT);
            case "slowing" -> throwable(Burst.SLOWING);
            case "binding" -> throwable(Burst.BINDING);
            case "weakening" -> throwable(Burst.WEAKENING);
            case "sleep" -> throwable(Burst.SLEEP);
            case "blinding" -> throwable(Burst.BLINDING);
            case "venom" -> () -> new HerbalOilItem(props(), Oil.VENOM, true);
            case "thorn" -> () -> new HerbalOilItem(props(), Oil.THORN, true);
            case "undead_repellent" -> () -> new HerbalOilItem(props(), Oil.UNDEAD_REPELLENT, true);
            case "arthropod_repellent" -> () -> new HerbalOilItem(props(), Oil.ARTHROPOD_REPELLENT, true);
            case "moon" -> () -> new Item(props());
            default -> throw new IllegalStateException("no item behaviour for advanced product " + spec.id());
        };
    }

    private static Supplier<Item> throwable(Burst burst) {
        return () -> new ThrowablePreparationItem(props(), burst, AdvancedPreparations::throwables);
    }

    public static Item item(ProductSpec spec) {
        RegistryObject<Item> item = PRODUCTS.get(spec);
        if (item == null) {
            throw new IllegalArgumentException("not a Milestone 7 preparation: " + spec.path());
        }
        return item.get();
    }

    /** Every thrown preparation (they share one throwing cooldown). */
    public static List<Item> throwables() {
        List<Item> out = new ArrayList<>();
        AdvancedProductCatalog.VIALS.forEach(v -> out.add(item(v)));
        out.add(item(AdvancedProductCatalog.BLINDING_DUST));
        return out;
    }

    /** Tab order: advanced preparations, familiar preparations, mixtures. */
    public static List<Item> all() {
        return PRODUCTS.values().stream().map(RegistryObject::get).toList();
    }

    /** Every Milestone 7 product spec, in registration order. */
    public static List<ProductSpec> specs() {
        return List.copyOf(PRODUCTS.keySet());
    }
}
