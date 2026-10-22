package io.github.dirtyredz.herbalwitch.brewing.product.catalog;

import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The cauldron's outputs: one registered mixture item per finished product, each exactly
 * {@link #DOSES} doses and stacking to 16. The bottling bench portions a whole mixture into that many
 * bottles (drinks and familiar tonics) or vials (throwables, with gunpowder); nothing is ever left
 * half-poured. Ids {@code preparations/mixtures/<group>/<product>}. Separate items rather than one
 * item with arbitrary data keep recipes, viewers and automation exact.
 */
public final class MixtureCatalog {
    /** Doses in one mixture, and bottles or vials it fills. */
    public static final int DOSES = 4;

    /** One mixture: its own item spec and the product it is bottled into. */
    public record Mixture(ProductSpec spec, ProductSpec product) {
        /** Whether bottling makes thrown vials (with gunpowder) rather than drinks. */
        public boolean throwable() {
            return product.kind() == Kind.VIAL;
        }
    }

    public static final List<Mixture> ALL;

    static {
        List<Mixture> all = new ArrayList<>();
        for (ProductSpec p : AdvancedProductCatalog.DRINKS) {
            all.add(mixture("utility", p, name(p.displayName(), " Draught")));
        }
        for (ProductSpec p : AdvancedProductCatalog.VIALS) {
            all.add(mixture("combat", p, name(p.displayName(), " Vial")));
        }
        for (ProductSpec p : FamiliarPreparationCatalog.TONICS) {
            all.add(mixture("familiar", p, p.displayName()));
        }
        ALL = List.copyOf(all);
    }

    private MixtureCatalog() {
    }

    private static Mixture mixture(String group, ProductSpec product, String base) {
        return new Mixture(new ProductSpec("mixtures/" + group, product.id(), base + " Mixture", Kind.MIXTURE,
                "Four doses: bottle it at the bottling bench"), product);
    }

    private static String name(String full, String suffix) {
        return full.endsWith(suffix) ? full.substring(0, full.length() - suffix.length()) : full;
    }

    /** Mixture specs, in catalogue order. */
    public static List<ProductSpec> specs() {
        return ALL.stream().map(Mixture::spec).toList();
    }

    /** The mixture a product is bottled from, if it has one. */
    public static Optional<Mixture> of(ProductSpec product) {
        return ALL.stream().filter(m -> m.product().equals(product)).findFirst();
    }
}
