package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication.Drink;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.GardenApplication.Garden;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.OilApplication.Oil;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.GardenProductItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HealingSalveItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HerbalDrinkItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HerbalOilItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
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
 * Prepared items: per herb its dried bundle (the hanging block's item) and powder; every everyday
 * product with the item class its kind needs; the oil lantern's item.
 */
public final class PreparationItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    private static final Map<HerbSpec, RegistryObject<Item>> BUNDLES = new LinkedHashMap<>();
    private static final Map<HerbSpec, RegistryObject<Item>> POWDERS = new LinkedHashMap<>();
    private static final Map<ProductSpec, RegistryObject<Item>> PRODUCTS = new LinkedHashMap<>();

    public static final RegistryObject<Item> OIL_LANTERN = ITEMS.register(PreparationBlocks.OIL_LANTERN_PATH,
            () -> new BlockItem(PreparationBlocks.OIL_LANTERN.get(), new Item.Properties()));

    static {
        for (HerbSpec herb : HerbCatalog.ALL) {
            BUNDLES.put(herb, ITEMS.register(HerbalMaterialCatalog.bundlePath(herb),
                    () -> new BlockItem(PreparationBlocks.bundle(herb), new Item.Properties())));
            POWDERS.put(herb, ITEMS.register(HerbalMaterialCatalog.powderPath(herb), () -> new Item(new Item.Properties())));
        }
        for (ProductSpec spec : EverydayProductCatalog.ALL) {
            PRODUCTS.put(spec, ITEMS.register(spec.path(), factory(spec)));
        }
    }

    private PreparationItems() {
    }

    /** The item class and behaviour each product gets. */
    private static Supplier<Item> factory(ProductSpec spec) {
        Item.Properties bottle = new Item.Properties().stacksTo(16);
        return switch (spec.id()) {
            case "recovery_tea" -> () -> new HerbalDrinkItem(bottle, Drink.RECOVERY);
            case "energy_tea" -> () -> new HerbalDrinkItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(DrinkApplication.ENERGY_NUTRITION).saturationMod(DrinkApplication.ENERGY_SATURATION)
                    .alwaysEat().build()), Drink.ENERGY);
            case "restful_infusion" -> () -> new HerbalDrinkItem(bottle, Drink.RESTFUL);
            case "warmth_remedy" -> () -> new HerbalDrinkItem(bottle, Drink.WARMTH);
            case "cooling_remedy" -> () -> new HerbalDrinkItem(bottle, Drink.COOLING);
            case "mining_tonic" -> () -> new HerbalDrinkItem(bottle, Drink.MINING);
            case "travel_tonic" -> () -> new HerbalDrinkItem(bottle, Drink.TRAVEL);
            case "healing_salve" -> () -> new HealingSalveItem(bottle);
            case "lantern_oil" -> () -> new HerbalOilItem(bottle, Oil.LANTERN);
            case "fishing_blend" -> () -> new HerbalOilItem(bottle, Oil.FISHING);
            case "pest_repellent" -> () -> new HerbalOilItem(bottle, Oil.PEST);
            case "fertilizer" -> () -> new GardenProductItem(new Item.Properties(), Garden.FERTILIZER, false);
            case "flower_dust" -> () -> new GardenProductItem(new Item.Properties(), Garden.FLOWER, false);
            case "mushroom_dust" -> () -> new GardenProductItem(new Item.Properties(), Garden.MUSHROOM, false);
            case "growth_infusion" -> () -> new GardenProductItem(bottle, Garden.GROWTH, true);
            case "soil_tonic" -> () -> new GardenProductItem(bottle, Garden.SOIL, true);
            case "plant_oil" -> () -> new Item(bottle);
            case "resin" -> () -> new Item(new Item.Properties());
            default -> throw new IllegalStateException("no item behaviour for product " + spec.id());
        };
    }

    public static Item bundle(HerbSpec herb) {
        return BUNDLES.get(herb).get();
    }

    public static Item powder(HerbSpec herb) {
        return POWDERS.get(herb).get();
    }

    public static Item product(ProductSpec spec) {
        return PRODUCTS.get(spec).get();
    }

    /** Tab order: bundles, powders, products, the oil lantern. */
    public static List<Item> all() {
        List<Item> items = new ArrayList<>();
        HerbCatalog.ALL.forEach(h -> items.add(bundle(h)));
        HerbCatalog.ALL.forEach(h -> items.add(powder(h)));
        EverydayProductCatalog.ALL.forEach(p -> items.add(product(p)));
        items.add(OIL_LANTERN.get());
        return items;
    }
}
