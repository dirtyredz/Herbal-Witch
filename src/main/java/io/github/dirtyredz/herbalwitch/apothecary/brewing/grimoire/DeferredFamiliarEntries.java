package io.github.dirtyredz.herbalwitch.apothecary.brewing.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/** M7-only fallback; never claims the familiar key after a bonding feature installs its source. */
public final class DeferredFamiliarEntries implements GrimoireContentSource {
    @Override public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        String prefix = WitchcraftKeys.PREFIX + "familiar.";
        out.accept(GrimoireEntry.page(WitchcraftKeys.key("familiar"), GrimoireContent.WITCHCRAFT,
                Component.translatable(prefix + "title"),
                AdvancedPreparations.item(FamiliarPreparationCatalog.GUARDIAN).getDefaultInstance(),
                List.of(Component.translatable(prefix + "0"), Component.translatable(prefix + "deferred")),
                WitchcraftKeys.PREPARATION_PAGES.stream().filter(p -> !p.equals("familiar")).map(WitchcraftKeys::key).toList()));
    }
}
