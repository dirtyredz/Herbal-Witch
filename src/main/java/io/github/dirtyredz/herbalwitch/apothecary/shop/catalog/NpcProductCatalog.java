package io.github.dirtyredz.herbalwitch.apothecary.shop.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;

import java.util.List;

/**
 * Exactly what a villager customer buys: one of four everyday preparations, a one-item lot priced at
 * one emerald, with default prepared metadata only. The sale is a demand sink: the product never enters
 * the villager's inventory, a vanilla offer or any resale path.
 */
public final class NpcProductCatalog {
    public static final List<String> ITEMS = List.of(
            "herbalwitch:preparations/drinks/recovery_tea",
            "herbalwitch:preparations/drinks/warmth_remedy",
            "herbalwitch:preparations/drinks/cooling_remedy",
            "herbalwitch:preparations/salves/healing_salve");
    public static final int LOT = 1;
    public static final int PRICE = 1;

    private NpcProductCatalog() {
    }

    public static boolean eligible(ShopOffer offer) {
        return offer.enabled() && offer.lot() == LOT && offer.price() == PRICE && ITEMS.contains(offer.key().item())
                && ShopMetadataRules.defaultPrepared(offer.key().tag());
    }
}
