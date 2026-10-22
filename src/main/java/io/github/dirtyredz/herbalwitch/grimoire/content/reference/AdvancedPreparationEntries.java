package io.github.dirtyredz.herbalwitch.grimoire.content.reference;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.advanced.DrinkApplications;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.ThrowablePreparationItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationImpact;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.MagicalProcessPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Milestone 7's guidance pages, public and advisory: how a witch runs magical processing (starting
 * and resuming a batch, who pays, the server's cost multiplier), what the finished preparations do and
 * how throwing works, and the familiar preparations whose use waits for familiar bonding. Recipe pages
 * themselves come from the live recipes; these pages quote numbers only as arguments.
 */
public final class AdvancedPreparationEntries implements GrimoireContentSource {
    public static final String PREFIX = WitchEntries.PREFIX;
    /** The pages, in reading order. */
    public static final List<String> PAGES = List.of("brewing", "preparations", "familiar");

    public static EntryKey key(String page) {
        return EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":witchcraft/" + page);
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        for (String page : PAGES) {
            List<Component> body = switch (page) {
                case "brewing" -> List.of(
                        Component.translatable(PREFIX + "brewing.0"),
                        Component.translatable(PREFIX + "brewing.start"),
                        Component.translatable(PREFIX + "brewing.payer", (int) MagicalProcessPolicy.RANGE),
                        Component.translatable(PREFIX + "brewing.cost", WitchRules.current().workings().processingPercent()),
                        Component.translatable(PREFIX + "brewing.bottling", MixtureCatalog.DOSES));
                case "preparations" -> List.of(
                        Component.translatable(PREFIX + "preparations.0"),
                        Component.translatable(PREFIX + "preparations.drinks", WitchEntries.seconds(DrinkApplications.HEALING_COOLDOWN)),
                        Component.translatable(PREFIX + "preparations.throwing", (int) PreparationImpact.RADIUS,
                                WitchEntries.seconds(ThrowablePreparationItem.COOLDOWN)),
                        Component.translatable(PREFIX + "preparations.control", WitchEntries.seconds(ControlEffects.IMMUNITY_TICKS)),
                        Component.translatable(PREFIX + "preparations.sight", (int) TargetFilters.NEAR),
                        Component.translatable(PREFIX + "preparations.oils"),
                        Component.translatable(PREFIX + "preparations.consent"));
                case "familiar" -> List.of(
                        Component.translatable(PREFIX + "familiar.0"),
                        Component.translatable(PREFIX + "familiar.deferred"));
                default -> throw new IllegalStateException(page);
            };
            ItemStack icon = switch (page) {
                case "brewing" -> AdvancedPreparations.item(MixtureCatalog.ALL.get(0).spec()).getDefaultInstance();
                case "preparations" -> AdvancedPreparations.item(AdvancedProductCatalog.POISON).getDefaultInstance();
                default -> AdvancedPreparations.item(FamiliarPreparationCatalog.GUARDIAN).getDefaultInstance();
            };
            List<EntryKey> links = PAGES.stream().filter(p -> !p.equals(page)).map(AdvancedPreparationEntries::key).toList();
            out.accept(new GrimoireEntry(key(page), GrimoireContent.WITCHCRAFT, Component.translatable(PREFIX + page + ".title"), icon, body,
                    links, Optional.empty(), true, Optional.empty()));
        }
    }
}
