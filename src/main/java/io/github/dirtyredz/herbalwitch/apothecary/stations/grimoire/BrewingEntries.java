package io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.ProcessingRules;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Consumer;

/** Station-owned guidance for magical processing; finished preparation guidance belongs to brewing. */
public final class BrewingEntries implements GrimoireContentSource {
    /** Where this source reads in the index (lower first); spaced so a later feature slots between. */
    public static final int PRIORITY = 200;

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        String prefix = WitchcraftKeys.PREFIX;
        out.accept(GrimoireEntry.page(WitchcraftKeys.key("brewing"), GrimoireContent.WITCHCRAFT,
                Component.translatable(prefix + "brewing.title"),
                AdvancedPreparations.item(MixtureCatalog.ALL.get(0).spec()).getDefaultInstance(), List.of(
                        Component.translatable(prefix + "brewing.0"),
                        Component.translatable(prefix + "brewing.start"),
                        Component.translatable(prefix + "brewing.payer", (int) ProcessingRules.RANGE),
                        Component.translatable(prefix + "brewing.cost", WitchRules.current().workings().processingPercent()),
                        Component.translatable(prefix + "brewing.bottling", MixtureCatalog.DOSES)),
                WitchcraftKeys.PREPARATION_PAGES.stream().filter(p -> !p.equals("brewing")).map(WitchcraftKeys::key).toList()));
    }
}
