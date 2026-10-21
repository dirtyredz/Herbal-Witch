package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbTrait;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbTooltips;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyCreativeTab;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import net.minecraftforge.common.data.LanguageProvider;

/** Herb plant, seed and fresh herb names, the herb bed, the gardening tab, traits and acquisition tooltips. */
public final class BotanyTranslations {
    private BotanyTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(BotanyCreativeTab.TITLE_KEY, "The Herbal Witch: Gardening");
        lang.add(BotanyBlocks.herbBed(), "Herb Bed");
        for (HerbSpec herb : HerbCatalog.ALL) {
            lang.add(BotanyBlocks.plant(herb), herb.displayName() + " Plant");
            lang.add(BotanyItems.seeds(herb), herb.displayName() + " Seeds");
            lang.add(BotanyItems.fresh(herb), "Fresh " + herb.displayName());
        }
        for (HerbTrait trait : HerbTrait.values()) {
            lang.add(HerbTooltips.traitKey(trait), trait.displayName());
        }
        lang.add(HerbTooltips.habitatKey(HerbHabitat.TEMPERATE), "temperate plains and forests");
        lang.add(HerbTooltips.habitatKey(HerbHabitat.DRY), "dry savannas");
        lang.add(HerbTooltips.habitatKey(HerbHabitat.WET), "swamps and rivers");
        lang.add(HerbTooltips.habitatKey(HerbHabitat.SHADE), "shaded taigas and dark forests");
        lang.add(HerbTooltips.habitatKey(HerbHabitat.WOODLAND), "dark woodland");
        lang.add(HerbTooltips.TRAITS, "Traits: %s");
        lang.add(HerbTooltips.STARTER, "Found in tall grass anywhere, wild in %s; or crafted from wheat seeds");
        lang.add(HerbTooltips.WILD, "Found wild and in tall grass in %s");
    }
}
