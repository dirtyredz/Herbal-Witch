package io.github.dirtyredz.herbalwitch.apothecary.stations.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * The processing categories and the station that runs each (Milestone 3's four, Milestone 7's four): the one mapping the grimoire, EMI
 * and JEI share, so a recipe is described by the same station everywhere. The hearth and stove are
 * heat sources, not categories. Registry bindings live separately in ProcessingRecipeBindings; this catalogue never resolves registries.
 */
public final class ProcessingCategories {
    /** One processing category: its id segment, station, and whether its recipes need heat. */
    public record Category(String id, StationSpec station,
                           boolean heated) {
        /** {@code herbalwitch:processing/<id>}, the recipe type and viewer category id. */
        public ResourceLocation typeId() {
            return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "processing/" + id);
        }

        /** Shared viewer and grimoire title, {@code category.herbalwitch.processing.<id>}. */
        public String titleKey() {
            return "category." + HerbalWitch.MOD_ID + ".processing." + id;
        }
    }

    public static final Category DRYING = new Category("drying", StationCatalog.DRYING_RACK, false);
    public static final Category GRINDING = new Category("grinding", StationCatalog.MORTAR, false);
    public static final Category STEEPING = new Category("steeping", StationCatalog.KETTLE, true);
    public static final Category INFUSION = new Category("infusion", StationCatalog.INFUSION_POT, true);

    public static final Category CAULDRON = new Category("cauldron", StationCatalog.CAULDRON, true);
    public static final Category DISTILLATION = new Category("distillation", StationCatalog.STILL, true);
    public static final Category BOTTLING = new Category("bottling", StationCatalog.BOTTLING_BENCH, false);
    public static final Category CHARM_WORK = new Category("charm_work", StationCatalog.CHARM_WORKBENCH, false);

    /** Station order. */
    public static final List<Category> ALL = List.of(DRYING, GRINDING, STEEPING, INFUSION, CAULDRON, DISTILLATION, BOTTLING, CHARM_WORK);

    private ProcessingCategories() {
    }

    /** The category a station runs, if it processes recipes (the hearth and stove do not). */
    public static Optional<Category> byStation(StationSpec station) {
        return ALL.stream().filter(c -> c.station() == station).findFirst();
    }

    /** The witch-recipe line shared by viewers and the grimoire: {@code A witch starts it: %s energy}. */
    public static final String REQUIREMENTS_KEY = "category." + HerbalWitch.MOD_ID + ".processing.witch";

    /** Footer key shared by viewers and the grimoire: {@code %s s} or {@code %s s, needs heat}. */
    public static String footerKey(boolean heated) {
        return "category." + HerbalWitch.MOD_ID + ".processing." + (heated ? "heated" : "time");
    }
}
