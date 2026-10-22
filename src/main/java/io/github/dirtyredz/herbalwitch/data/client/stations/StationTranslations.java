package io.github.dirtyredz.herbalwitch.data.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.StationOperations;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRegistration;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Station names, menu status lines, the witch's Start/Resume/Cancel panel and its outcomes,
 * recipe-viewer categories, the apothecary tab and showcase recipe lines.
 */
public final class StationTranslations {
    private static final Map<String, String> CATEGORIES = Map.of("drying", "Drying", "grinding", "Grinding",
            "steeping", "Steeping", "infusion", "Infusion", "cauldron", "Cauldron Brewing", "distillation", "Distillation",
            "bottling", "Bottling", "charm_work", "Charm Work");
    private static final Map<String, String> SHOWCASE = Map.ofEntries(
            Map.entry("drying_rack", "Fresh herb -> bundle"),
            Map.entry("mortar", "Bundle -> powder"),
            Map.entry("kettle", "Powders + water -> tea"),
            Map.entry("hearth", "Burns fuel: heat above"),
            Map.entry("stove", "Burns fuel: two burners"),
            Map.entry("infusion_pot", "Powders + base -> salve"),
            Map.entry("cauldron", "A witch brews: four doses"),
            Map.entry("still", "A witch distils an essence"),
            Map.entry("bottling_bench", "Mixture -> four bottles"),
            Map.entry("charm_workbench", "A witch makes a charm"));
    private static final String PANEL = "screen." + HerbalWitch.MOD_ID + ".station.magic.";

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
        lang.add(ProcessStatus.NEEDS_WITCH.translationKey(), "Needs a witch");
        lang.add(ProcessStatus.NEEDS_OPERATOR.translationKey(), "Needs its witch nearby");
        lang.add(ProcessStatus.NEEDS_ENERGY.translationKey(), "Needs energy");
        outcomes().forEach((suffix, text) -> lang.add(StationOperations.KEY + suffix, text));
        lang.add(PANEL + "title", "Witch's work");
        lang.add(PANEL + "cost", "Costs %s energy a batch");
        lang.add(PANEL + "unstarted", "Not started");
        lang.add(PANEL + "yours", "Your batch");
        lang.add(PANEL + "theirs", "Another witch's batch");
        lang.add(PANEL + "start", "Start");
        lang.add(PANEL + "resume", "Resume");
        lang.add(PANEL + "cancel", "Cancel");
        CATEGORIES.forEach((id, name) -> {
            lang.add("category." + HerbalWitch.MOD_ID + ".processing." + id, name);
            lang.add("emi.category." + HerbalWitch.MOD_ID + ".processing." + id, name);
        });
        lang.add("category." + HerbalWitch.MOD_ID + ".processing.time", "%s s");
        lang.add("category." + HerbalWitch.MOD_ID + ".processing.heated", "%s s, needs heat");
        lang.add(ProcessingCategories.REQUIREMENTS_KEY, "A witch starts it: %s energy");
    }

    /** What Start, Resume and Cancel did ({@code status.herbalwitch.station.<outcome>}). */
    public static Map<String, String> outcomes() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("nothing", "Nothing to do here.");
        m.put("ordinary", "This recipe needs no witch: it runs by itself.");
        m.put("already", "This batch is already yours.");
        m.put("busy", "Another witch has started this batch.");
        m.put("not_witch", "Only a witch can start this.");
        m.put("too_far", "Stand closer to the station.");
        m.put("not_enough_energy", "Not enough energy for this batch.");
        m.put("started", "You started the batch; stay near it.");
        m.put("not_yours", "This batch belongs to another witch.");
        m.put("resumed", "You took up your batch again.");
        m.put("cancelled", "The batch was cancelled; nothing was used.");
        return m;
    }
}
