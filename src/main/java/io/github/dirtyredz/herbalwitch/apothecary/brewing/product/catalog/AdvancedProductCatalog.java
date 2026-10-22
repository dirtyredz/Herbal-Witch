package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec.Kind;

import java.util.List;

/**
 * The Milestone 7 player preparations (docs/catalogue/products.md): utility drinks, thrown vials,
 * weapon and body oils, blinding dust and the still's moon essence. Ids
 * {@code preparations/advanced/<group>/<product>}; permanent once a world uses them. Finished
 * preparations work for anyone: only making them needs a witch.
 */
public final class AdvancedProductCatalog {
    public static final ProductSpec NIGHT_VISION = drink("night_vision", "Night Vision Draught", "Night Vision, 2:00");
    public static final ProductSpec WATER_BREATHING = drink("water_breathing", "Water Breathing Draught", "Water Breathing, 2:00");
    public static final ProductSpec RESISTANCE = drink("resistance", "Resistance Draught", "Resistance I, 1:30");
    public static final ProductSpec HEALING = drink("healing", "Healing Draught", "Restores 3 hearts when hurt; one every 10 seconds");

    public static final ProductSpec POISON = vial("poison", "Poison Vial", "Thrown: Poison I, 0:08");
    public static final ProductSpec CONCEALMENT = vial("concealment", "Concealment Vial",
            "Thrown: a smoke burst; foes cannot find those in it beyond 4 blocks, 0:08");
    public static final ProductSpec SLOWING = vial("slowing", "Slowing Vial", "Thrown: Slowness II, 0:08");
    public static final ProductSpec BINDING = vial("binding", "Binding Vial", "Thrown: holds a creature in place, 0:05");
    public static final ProductSpec WEAKENING = vial("weakening", "Weakening Vial", "Thrown: Weakness I, 0:08");
    public static final ProductSpec SLEEP = vial("sleep", "Sleep Vial", "Thrown: a creature sleeps until struck, 0:05");

    public static final ProductSpec VENOM_OIL = oil("venom", "Venom Oil", "Coats the blade in your off hand: 8 poisoning hits or 1:00");
    public static final ProductSpec THORN_OIL = oil("thorn", "Thorn Oil", "Thorns: whoever strikes you is pricked, 1:30");
    public static final ProductSpec UNDEAD_REPELLENT = oil("undead_repellent", "Undead Repellent", "The undead will not seek you, 1:30");
    public static final ProductSpec ARTHROPOD_REPELLENT = oil("arthropod_repellent", "Arthropod Repellent",
            "Spiders and crawling pests will not seek you, 1:30");

    public static final ProductSpec BLINDING_DUST = new ProductSpec("advanced/dusts", "blinding", "Blinding Dust", Kind.THROWN_DUST,
            "Thrown: blinds; creatures cannot find anyone beyond 4 blocks, 0:05");
    public static final ProductSpec MOON_ESSENCE = new ProductSpec("advanced/essences", "moon", "Moon Essence", Kind.INGREDIENT,
            "Distilled moonlight for the strongest brews");

    public static final List<ProductSpec> DRINKS = List.of(NIGHT_VISION, WATER_BREATHING, RESISTANCE, HEALING);
    public static final List<ProductSpec> VIALS = List.of(POISON, CONCEALMENT, SLOWING, BINDING, WEAKENING, SLEEP);
    public static final List<ProductSpec> OILS = List.of(VENOM_OIL, THORN_OIL, UNDEAD_REPELLENT, ARTHROPOD_REPELLENT);

    /** Every advanced preparation, in creative-tab and showcase order. */
    public static final List<ProductSpec> ALL = List.of(NIGHT_VISION, WATER_BREATHING, RESISTANCE, HEALING, POISON, CONCEALMENT, SLOWING,
            BINDING, WEAKENING, SLEEP, VENOM_OIL, THORN_OIL, UNDEAD_REPELLENT, ARTHROPOD_REPELLENT, BLINDING_DUST, MOON_ESSENCE);

    private AdvancedProductCatalog() {
    }

    private static ProductSpec drink(String id, String name, String description) {
        return new ProductSpec("advanced/drinks", id, name, Kind.DRINK, description);
    }

    private static ProductSpec vial(String id, String name, String description) {
        return new ProductSpec("advanced/throwables", id, name, Kind.VIAL, description);
    }

    private static ProductSpec oil(String id, String name, String description) {
        return new ProductSpec("advanced/oils", id, name, Kind.OIL, description);
    }
}
