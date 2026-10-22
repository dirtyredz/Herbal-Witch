package io.github.dirtyredz.herbalwitch.apothecary.brewing;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.CharmProtection;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Milestone 7's product identities (an independent list), the four-dose mixtures, the eleven familiar preparations and charm strength. */
class PreparationCatalogTest {
    @Test
    void permanentIdsAreExactly() {
        assertEquals(List.of("preparations/advanced/drinks/night_vision", "preparations/advanced/drinks/water_breathing",
                "preparations/advanced/drinks/resistance", "preparations/advanced/drinks/healing",
                "preparations/advanced/throwables/poison", "preparations/advanced/throwables/concealment",
                "preparations/advanced/throwables/slowing", "preparations/advanced/throwables/binding",
                "preparations/advanced/throwables/weakening", "preparations/advanced/throwables/sleep",
                "preparations/advanced/oils/venom", "preparations/advanced/oils/thorn", "preparations/advanced/oils/undead_repellent",
                "preparations/advanced/oils/arthropod_repellent", "preparations/advanced/dusts/blinding", "preparations/advanced/essences/moon"),
                AdvancedProductCatalog.ALL.stream().map(ProductSpec::path).toList());
        assertEquals(List.of("preparations/familiar/tonics/guardian", "preparations/familiar/tonics/swiftpaw",
                "preparations/familiar/tonics/nightwatch", "preparations/familiar/tonics/moonveil", "preparations/familiar/tonics/spiritbond",
                "preparations/familiar/tonics/hearthkeeper", "preparations/familiar/tonics/shadowstep", "preparations/familiar/essences/warding",
                "preparations/familiar/essences/venomleaf", "preparations/familiar/oils/emberguard"),
                FamiliarPreparationCatalog.ALL.stream().map(ProductSpec::path).toList());
        assertEquals(List.of("magic/charms/protective_sachet", "magic/charms/undead_amulet", "magic/charms/arthropod_amulet"),
                CharmCatalog.ALL.stream().map(c -> c.path()).toList());
    }

    @Test
    void everyCauldronProductHasOneFourDoseMixture() {
        assertEquals(4, MixtureCatalog.DOSES);
        assertEquals(17, MixtureCatalog.ALL.size());
        Set<String> paths = new HashSet<>();
        for (MixtureCatalog.Mixture m : MixtureCatalog.ALL) {
            assertTrue(paths.add(m.spec().path()));
            assertTrue(m.spec().path().equals("preparations/mixtures/" + group(m) + "/" + m.product().id()), m.spec().path());
            assertEquals(m.product().kind() == ProductSpec.Kind.VIAL, m.throwable());
        }
        assertEquals(6, MixtureCatalog.ALL.stream().filter(MixtureCatalog.Mixture::throwable).count());
    }

    private static String group(MixtureCatalog.Mixture m) {
        if (AdvancedProductCatalog.DRINKS.contains(m.product())) {
            return "utility";
        }
        return AdvancedProductCatalog.VIALS.contains(m.product()) ? "combat" : "familiar";
    }

    @Test
    void elevenFamiliarPreparationsIncludingTheSalve() {
        assertEquals(11, FamiliarPreparationCatalog.CATALOGUE.size());
        assertTrue(FamiliarPreparationCatalog.CATALOGUE.contains(EverydayProductCatalog.HEALING_SALVE));
        assertTrue(FamiliarPreparationCatalog.ALL.stream().allMatch(p -> p.kind() == ProductSpec.Kind.FAMILIAR));
    }

    @Test
    void charmsSoftenByShareUpToTheirCap() {
        assertEquals(8.5F, CharmProtection.reduce(CharmCatalog.PROTECTIVE_SACHET, 10F), 1e-4, "15 percent of 10");
        assertEquals(18F, CharmProtection.reduce(CharmCatalog.PROTECTIVE_SACHET, 20F), 1e-4, "capped at 2 health");
        assertEquals(7F, CharmProtection.reduce(CharmCatalog.UNDEAD_AMULET, 10F), 1e-4, "30 percent");
        assertEquals(17F, CharmProtection.reduce(CharmCatalog.UNDEAD_AMULET, 20F), 1e-4, "capped at 3 health");
        assertEquals(0F, CharmProtection.reduce(CharmCatalog.UNDEAD_AMULET, 0F), 1e-4);
    }
}
