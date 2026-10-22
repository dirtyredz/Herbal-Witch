package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec.Kind;

import java.util.List;

/**
 * The familiar preparations of the catalogue (docs/catalogue/products.md): all eleven recipes exist
 * from Milestone 7 (ten here plus the existing Healing Salve, which keeps its player use). Their
 * familiar applications arrive with familiar bonding (Milestone 9), which installs a consumer keyed by
 * these permanent products without changing their recipes. Ids {@code preparations/familiar/<group>/<id>}.
 */
public final class FamiliarPreparationCatalog {
    public static final ProductSpec GUARDIAN = tonic("guardian", "Guardian's Brew", "For a bonded familiar: steadfast in a fight");
    public static final ProductSpec SWIFTPAW = tonic("swiftpaw", "Swiftpaw Tonic", "For a bonded familiar: quick on its feet");
    public static final ProductSpec NIGHTWATCH = tonic("nightwatch", "Nightwatch Infusion", "For a bonded familiar: sharp eyes after dark");
    public static final ProductSpec MOONVEIL = tonic("moonveil", "Moonveil Elixir", "For a bonded familiar: hidden in moonlight");
    public static final ProductSpec SPIRITBOND = tonic("spiritbond", "Spiritbond Potion", "For a bonded familiar: a deeper bond");
    public static final ProductSpec HEARTHKEEPER = tonic("hearthkeeper", "Hearthkeeper's Brew", "For a bonded familiar: a homebody's comfort");
    public static final ProductSpec SHADOWSTEP = tonic("shadowstep", "Shadowstep Draught", "For a bonded familiar: a step through shadow");
    public static final ProductSpec WARDING = new ProductSpec("familiar/essences", "warding", "Warding Essence", Kind.FAMILIAR,
            "For a bonded familiar: a ward against harm");
    public static final ProductSpec VENOMLEAF = new ProductSpec("familiar/essences", "venomleaf", "Venomleaf Extract", Kind.FAMILIAR,
            "For a bonded familiar: a venomous bite");
    public static final ProductSpec EMBERGUARD = new ProductSpec("familiar/oils", "emberguard", "Emberguard Oil", Kind.FAMILIAR,
            "For a bonded familiar: proof against flame");

    public static final List<ProductSpec> TONICS = List.of(GUARDIAN, SWIFTPAW, NIGHTWATCH, MOONVEIL, SPIRITBOND, HEARTHKEEPER, SHADOWSTEP);

    /** The ten new familiar preparations, in creative-tab order. */
    public static final List<ProductSpec> ALL = List.of(GUARDIAN, SWIFTPAW, NIGHTWATCH, MOONVEIL, SPIRITBOND, HEARTHKEEPER, SHADOWSTEP,
            WARDING, VENOMLEAF, EMBERGUARD);

    /** The catalogue's eleven familiar preparations: the ten above and the existing Healing Salve. */
    public static final List<ProductSpec> CATALOGUE = List.of(GUARDIAN, SWIFTPAW, NIGHTWATCH, MOONVEIL, SPIRITBOND, HEARTHKEEPER,
            SHADOWSTEP, WARDING, VENOMLEAF, EMBERGUARD, EverydayProductCatalog.HEALING_SALVE);

    private FamiliarPreparationCatalog() {
    }

    private static ProductSpec tonic(String id, String name, String description) {
        return new ProductSpec("familiar/tonics", id, name, Kind.FAMILIAR, description);
    }
}
