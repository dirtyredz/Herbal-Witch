package io.github.dirtyredz.herbalwitch.data.client.showcase;

import io.github.dirtyredz.herbalwitch.botany.showcase.GardenShowcaseSection;
import io.github.dirtyredz.herbalwitch.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight;
import net.minecraftforge.common.data.LanguageProvider;

/** Showcase command results and failures, section headings and sign labels. */
public final class ShowcaseTranslations {
    private ShowcaseTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        String k = ShowcasePreflight.KEY;
        lang.add(k + "success", "Built %s showcase section(s): %s blocks placed, %s cleared, from %s to %s");
        lang.add(k + "unknown_section", "No showcase section named %s");
        lang.add(k + "too_large", "The showcase layout is too large: %s");
        lang.add(k + "outside_world", "The showcase would leave the world at %s");
        lang.add(k + "unloaded", "The showcase area is not loaded at %s");
        lang.add(k + "denied", "You may not build at %s");
        lang.add(k + "not_flat", "The showcase needs flat solid ground; none at %s");
        lang.add(k + "block_entity", "A block with contents is in the way at %s; use an empty plot");
        lang.add(k + "unbreakable", "An unbreakable block is in the way at %s");
        lang.add(k + "entity", "%s is in the way at %s");

        lang.add(DecorShowcaseSection.TITLE, "Cottage decor");
        lang.add(HerbShowcaseSection.TITLE, "Herbs");
        lang.add(HerbShowcaseSection.AGES, "ages 0 to 3");
        lang.add(HerbShowcaseSection.SAMPLES, "barrel: seeds, fresh");
        lang.add(GardenShowcaseSection.TITLE, "Garden");
        lang.add(GardenShowcaseSection.BED_DRY, "Herb bed (dry)");
        lang.add(GardenShowcaseSection.BED_MOIST, "Herb bed (moist)");
        lang.add(GardenShowcaseSection.FARMLAND, "Farmland");
    }
}
