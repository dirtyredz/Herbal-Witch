package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec.Kind;

import java.util.List;

/** The Milestone 3 everyday products (docs/catalogue/products.md). The only place they are defined. */
public final class EverydayProductCatalog {
    public static final ProductSpec RECOVERY_TEA = new ProductSpec("drinks", "recovery_tea", "Recovery Tea", Kind.DRINK,
            "Regeneration I, 1:30");
    public static final ProductSpec ENERGY_TEA = new ProductSpec("drinks", "energy_tea", "Energy Tea", Kind.DRINK,
            "Restores hunger, and a witch's energy; one cup at a time");
    public static final ProductSpec RESTFUL_INFUSION = new ProductSpec("drinks", "restful_infusion", "Restful Infusion", Kind.DRINK,
            "Rested: phantoms forget you; a witch sleeps deeper");
    public static final ProductSpec WARMTH_REMEDY = new ProductSpec("drinks", "warmth_remedy", "Warmth Remedy", Kind.DRINK,
            "Warmth, 1:30: no freezing");
    public static final ProductSpec COOLING_REMEDY = new ProductSpec("drinks", "cooling_remedy", "Cooling Remedy", Kind.DRINK,
            "Fire Resistance, 1:00");
    public static final ProductSpec MINING_TONIC = new ProductSpec("drinks", "mining_tonic", "Mining Tonic", Kind.DRINK,
            "Haste I, 1:30");
    public static final ProductSpec TRAVEL_TONIC = new ProductSpec("drinks", "travel_tonic", "Travel Tonic", Kind.DRINK,
            "Speed I, 1:30");
    public static final ProductSpec HEALING_SALVE = new ProductSpec("salves", "healing_salve", "Healing Salve", Kind.SALVE,
            "Restores 2 hearts when hurt");
    public static final ProductSpec LANTERN_OIL = new ProductSpec("oils", "lantern_oil", "Lantern Oil", Kind.OIL,
            "Fuels an oil lantern for 5 minutes");
    public static final ProductSpec FISHING_BLEND = new ProductSpec("oils", "fishing_blend", "Fishing Blend", Kind.OIL,
            "Luck I, 1:30");
    public static final ProductSpec FERTILIZER = new ProductSpec("garden", "fertilizer", "Garden Fertilizer", Kind.GARDEN_DUST,
            "Bone meal for one crop");
    public static final ProductSpec FLOWER_DUST = new ProductSpec("garden", "flower_dust", "Flower Growth Dust", Kind.GARDEN_DUST,
            "Grows a flower on grass");
    public static final ProductSpec MUSHROOM_DUST = new ProductSpec("garden", "mushroom_dust", "Mushroom Growth Dust", Kind.GARDEN_DUST,
            "Grows a mushroom where one can live");
    public static final ProductSpec GROWTH_INFUSION = new ProductSpec("garden", "growth_infusion", "Growth Infusion", Kind.GARDEN_TONIC,
            "Matures one herb");
    public static final ProductSpec PEST_REPELLENT = new ProductSpec("garden", "pest_repellent", "Pest Repellent", Kind.OIL,
            "Silverfish and endermites leave you be, 1:30");
    public static final ProductSpec SOIL_TONIC = new ProductSpec("garden", "soil_tonic", "Soil Tonic", Kind.GARDEN_TONIC,
            "Enriches a herb bed: one extra herb next harvest");
    public static final ProductSpec PLANT_OIL = new ProductSpec("ingredients", "plant_oil", "Bottled Plant Oil", Kind.INGREDIENT,
            "Pressed from seeds");
    public static final ProductSpec RESIN = new ProductSpec("ingredients", "resin", "Resin", Kind.INGREDIENT,
            "Found when breaking spruce leaves");

    public static final List<ProductSpec> DRINKS = List.of(RECOVERY_TEA, ENERGY_TEA, RESTFUL_INFUSION, WARMTH_REMEDY,
            COOLING_REMEDY, MINING_TONIC, TRAVEL_TONIC);

    /** Every product, in creative-tab and showcase order. */
    public static final List<ProductSpec> ALL = List.of(RECOVERY_TEA, ENERGY_TEA, RESTFUL_INFUSION, WARMTH_REMEDY,
            COOLING_REMEDY, MINING_TONIC, TRAVEL_TONIC, HEALING_SALVE, LANTERN_OIL, FISHING_BLEND, FERTILIZER, FLOWER_DUST,
            MUSHROOM_DUST, GROWTH_INFUSION, PEST_REPELLENT, SOIL_TONIC, PLANT_OIL, RESIN);

    private EverydayProductCatalog() {
    }
}
