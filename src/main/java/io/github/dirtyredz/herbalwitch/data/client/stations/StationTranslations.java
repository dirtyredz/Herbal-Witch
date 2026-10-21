package io.github.dirtyredz.herbalwitch.data.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.stations.registry.StationCreativeTab;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRegistration;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Map;

/** Station names, menu status lines, recipe-viewer categories, the apothecary tab and showcase recipe lines. */
public final class StationTranslations {
    private static final Map<String, String> CATEGORIES = Map.of("drying", "Drying", "grinding", "Grinding",
            "steeping", "Steeping", "infusion", "Infusion");
    private static final Map<String, String> SHOWCASE = Map.of(
            "drying_rack", "Fresh herb -> bundle",
            "mortar", "Bundle -> powder",
            "kettle", "Powders + water -> tea",
            "hearth", "Burns fuel: heat above",
            "stove", "Burns fuel: two burners",
            "infusion_pot", "Powders + base -> salve");

    private StationTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(StationCreativeTab.TITLE_KEY, "The Herbal Witch: Apothecary");
        for (StationSpec spec : StationCatalog.ALL) {
            lang.add(StationBlocks.block(spec), spec.displayName());
            lang.add(StationRegistration.recipeKey(spec.directory()), SHOWCASE.get(spec.directory()));
        }
        lang.add(ProcessStatus.MISSING_INPUT.translationKey(), "Waiting");
        lang.add(ProcessStatus.NEEDS_HEAT.translationKey(), "Needs heat");
        lang.add(ProcessStatus.WORKING.translationKey(), "Working");
        lang.add(ProcessStatus.OUTPUT_BLOCKED.translationKey(), "Output full");
        CATEGORIES.forEach((id, name) -> {
            lang.add("category." + HerbalWitch.MOD_ID + ".processing." + id, name);
            lang.add("emi.category." + HerbalWitch.MOD_ID + ".processing." + id, name);
        });
        lang.add("category." + HerbalWitch.MOD_ID + ".processing.time", "%s s");
        lang.add("category." + HerbalWitch.MOD_ID + ".processing.heated", "%s s, needs heat");
    }
}
