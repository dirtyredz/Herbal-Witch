package io.github.dirtyredz.herbalwitch.apothecary.brewing.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.ThrowablePreparationItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationImpact;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/** Public guidance for finished preparations and the deferred familiar preparations. */
public final class AdvancedPreparationEntries implements GrimoireContentSource {
    /** Where this source reads in the index (lower first); spaced so a later feature slots between. */
    public static final int PRIORITY = 300;

    public static final String PREFIX = WitchcraftKeys.PREFIX;
    /** The pages, in reading order. */
    public static final List<String> PAGES = List.of("preparations", "familiar");

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        for (String page : PAGES) {
            List<Component> body = switch (page) {
                case "preparations" -> List.of(
                        Component.translatable(PREFIX + "preparations.0"),
                        Component.translatable(PREFIX + "preparations.drinks", TickTime.ceilingSeconds(DrinkApplication.HEALING_COOLDOWN)),
                        Component.translatable(PREFIX + "preparations.throwing", (int) PreparationImpact.RADIUS,
                                TickTime.ceilingSeconds(ThrowablePreparationItem.COOLDOWN)),
                        Component.translatable(PREFIX + "preparations.control", TickTime.ceilingSeconds(ControlEffects.IMMUNITY_TICKS)),
                        Component.translatable(PREFIX + "preparations.sight", (int) TargetFilters.NEAR),
                        Component.translatable(PREFIX + "preparations.oils"),
                        Component.translatable(PREFIX + "preparations.consent"));
                case "familiar" -> List.of(
                        Component.translatable(PREFIX + "familiar.0"),
                        Component.translatable(PREFIX + "familiar.deferred"));
                default -> throw new IllegalStateException(page);
            };
            ItemStack icon = switch (page) {
                case "preparations" -> AdvancedPreparations.item(AdvancedProductCatalog.POISON).getDefaultInstance();
                default -> AdvancedPreparations.item(FamiliarPreparationCatalog.GUARDIAN).getDefaultInstance();
            };
            List<EntryKey> links = WitchcraftKeys.PREPARATION_PAGES.stream().filter(p -> !p.equals(page)).map(WitchcraftKeys::key).toList();
            out.accept(GrimoireEntry.page(WitchcraftKeys.key(page), GrimoireContent.WITCHCRAFT, Component.translatable(PREFIX + page + ".title"), icon, body,
                    links));
        }
    }
}
