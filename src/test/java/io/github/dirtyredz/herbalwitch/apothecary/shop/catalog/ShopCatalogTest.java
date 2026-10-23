package io.github.dirtyredz.herbalwitch.apothecary.shop.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Independent inventory of the four commerce identities, their recipes and the reused counter. */
class ShopCatalogTest {
    @Test
    void exactlyFourPiecesWithPermanentPathsMountsAndSlots() {
        assertEquals(List.of("shop/register", "shop/stock_shelf", "shop/stock_jar", "shop/order_board"),
                ShopPieceCatalog.ALL.stream().map(ShopPieceSpec::path).toList());
        assertEquals(List.of(ShopPieceSpec.Mount.COUNTER_TOP, ShopPieceSpec.Mount.WALL, ShopPieceSpec.Mount.SURFACE, ShopPieceSpec.Mount.WALL),
                ShopPieceCatalog.ALL.stream().map(ShopPieceSpec::mount).toList());
        assertEquals(List.of(0, 3, 1, 0), ShopPieceCatalog.ALL.stream().map(ShopPieceSpec::displaySlots).toList());
        assertEquals("decor/surfaces/apothecary_counter", ShopPieceCatalog.COUNTER_PATH);
        assertEquals("shop", ShopPieceCatalog.SHOWCASE);
    }

    @Test
    void shapelessRecipesYieldOnePieceFromThePlannedIngredients() {
        Map<String, Map<String, Integer>> expected = new LinkedHashMap<>();
        expected.put("register", Map.of("dark_oak_planks", 4, "iron_ingot", 2, "gold_ingot", 1, "paper", 1, "redstone", 1));
        expected.put("stock_shelf", Map.of("dark_oak_slab", 3, "stick", 2, "gold_nugget", 1));
        expected.put("stock_jar", Map.of("glass", 3, "dark_oak_slab", 1, "gold_nugget", 1));
        expected.put("order_board", Map.of("dark_oak_planks", 3, "paper", 2, "ink_sac", 1));
        for (ShopPieceSpec spec : ShopPieceCatalog.ALL) assertEquals(expected.get(spec.key()), Map.copyOf(spec.ingredients()), spec.key());
        for (ShopPieceSpec spec : ShopPieceCatalog.ALL) assertTrue(spec.ingredients().values().stream().mapToInt(Integer::intValue).sum() <= 9, spec.key());
    }

    @Test
    void metadataRulesKeepExactPurityLabelsAndChargeAndRefuseEverythingElse() {
        assertTrue(ShopMetadataRules.accepts(false, null));
        assertTrue(ShopMetadataRules.accepts(false, new CompoundTag()));
        CompoundTag purity = new CompoundTag();
        purity.putInt("Purity", 2);
        assertTrue(ShopMetadataRules.accepts(false, purity));
        purity.putInt("Purity", 9);
        assertFalse(ShopMetadataRules.accepts(false, purity));
        CompoundTag label = new CompoundTag();
        label.putBoolean("HerbalWitchLabel", true);
        assertTrue(ShopMetadataRules.accepts(false, label));
        CompoundTag charge = new CompoundTag();
        CompoundTag used = new CompoundTag();
        used.putInt("Used", 12);
        charge.put("HerbalWitchCharm", used);
        assertTrue(ShopMetadataRules.accepts(true, charge), "a partly used charm keeps its charge");
        assertFalse(ShopMetadataRules.accepts(false, charge), "charge data only on charms");
        used.putString("Owner", "x");
        assertFalse(ShopMetadataRules.accepts(true, charge), "unknown charge fields are refused, not stripped");
        CompoundTag named = new CompoundTag();
        named.put("display", new CompoundTag());
        assertFalse(ShopMetadataRules.accepts(false, named), "custom names are refused");
        CompoundTag nested = new CompoundTag();
        CompoundTag deep = nested;
        for (int i = 0; i < 10; i++) {
            CompoundTag next = new CompoundTag();
            deep.put("Purity", next);
            deep = next;
        }
        assertFalse(ShopMetadataRules.accepts(false, nested));
        CompoundTag big = new CompoundTag();
        big.putString("Purity", "x".repeat(5000));
        assertFalse(ShopMetadataRules.accepts(false, big), "oversized payloads are refused");
    }

    @Test
    void villagersBuyOnlyFourDefaultPreparationsAsOneItemOneEmeraldLots() {
        assertEquals(4, NpcProductCatalog.ITEMS.size());
        ShopStackKey tea = ShopStackKey.of("herbalwitch:preparations/drinks/recovery_tea");
        assertTrue(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, tea, 1, 1, true)));
        assertFalse(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, tea, 2, 1, true)));
        assertFalse(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, tea, 1, 2, true)));
        assertFalse(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, tea, 1, 1, false)));
        assertFalse(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, ShopStackKey.of("herbalwitch:preparations/drinks/energy_tea"), 1, 1, true)));
        CompoundTag pristine = new CompoundTag();
        pristine.putInt("Purity", 3);
        assertTrue(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, new ShopStackKey(tea.item(), pristine), 1, 1, true)));
        CompoundTag dirty = new CompoundTag();
        dirty.putInt("Purity", 1);
        assertFalse(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, new ShopStackKey(tea.item(), dirty), 1, 1, true)));
        CompoundTag labelled = new CompoundTag();
        labelled.putBoolean("HerbalWitchLabel", true);
        assertFalse(NpcProductCatalog.eligible(new ShopOffer(UUID.randomUUID(), 1, new ShopStackKey(tea.item(), labelled), 1, 1, true)));
    }
}
