package io.github.dirtyredz.herbalwitch.stations.description;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.advanced.bottling_bench.recipe.BottlingRecipe;
import io.github.dirtyredz.herbalwitch.stations.advanced.cauldron.recipe.CauldronRecipe;
import io.github.dirtyredz.herbalwitch.stations.advanced.charm_workbench.recipe.CharmRecipe;
import io.github.dirtyredz.herbalwitch.stations.advanced.still.recipe.DistillationRecipe;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.recipe.DryingRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.recipe.InfusionRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.recipe.SteepingRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.recipe.GrindingRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The processing categories and the station that runs each (Milestone 3's four, Milestone 7's four): the one mapping the grimoire, EMI
 * and JEI share, so a recipe is described by the same station everywhere. The hearth and stove are
 * heat sources, not categories. Holds registry suppliers, so it lives outside the registry-free
 * {@code catalog} package; nothing here resolves a registry until a caller asks.
 */
public final class ProcessingCategories {
    /** One processing category: its id segment, recipe type, station, and whether its recipes need heat. */
    public record Category(String id, Supplier<? extends RecipeType<? extends ProcessRecipe>> type, StationSpec station,
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

    public static final Category DRYING = category("drying", StationRecipes.DRYING, StationCatalog.DRYING_RACK,
            DryingRecipe.BOUNDS);
    public static final Category GRINDING = category("grinding", StationRecipes.GRINDING, StationCatalog.MORTAR,
            GrindingRecipe.BOUNDS);
    public static final Category STEEPING = category("steeping", StationRecipes.STEEPING, StationCatalog.KETTLE,
            SteepingRecipe.BOUNDS);
    public static final Category INFUSION = category("infusion", StationRecipes.INFUSION, StationCatalog.INFUSION_POT,
            InfusionRecipe.BOUNDS);

    public static final Category CAULDRON = category("cauldron", StationRecipes.CAULDRON, StationCatalog.CAULDRON, CauldronRecipe.BOUNDS);
    public static final Category DISTILLATION = category("distillation", StationRecipes.DISTILLATION, StationCatalog.STILL,
            DistillationRecipe.BOUNDS);
    public static final Category BOTTLING = category("bottling", StationRecipes.BOTTLING, StationCatalog.BOTTLING_BENCH,
            BottlingRecipe.BOUNDS);
    public static final Category CHARM_WORK = category("charm_work", StationRecipes.CHARM_WORK, StationCatalog.CHARM_WORKBENCH,
            CharmRecipe.BOUNDS);

    /** Station order. */
    public static final List<Category> ALL = List.of(DRYING, GRINDING, STEEPING, INFUSION, CAULDRON, DISTILLATION, BOTTLING, CHARM_WORK);

    private ProcessingCategories() {
    }

    private static Category category(String id, RegistryObject<? extends RecipeType<? extends ProcessRecipe>> type,
                                     StationSpec station, ProcessRecipeJson.Bounds bounds) {
        return new Category(id, type, station, bounds.heated());
    }

    /** The category of a recipe type, if it is one of ours. */
    public static Optional<Category> byType(RecipeType<?> type) {
        return ALL.stream().filter(c -> c.type().get() == type).findFirst();
    }

    /** The category a station runs, if it processes recipes (the hearth and stove do not). */
    public static Optional<Category> byStation(StationSpec station) {
        return ALL.stream().filter(c -> c.station() == station).findFirst();
    }

    /** Seconds of a duration in ticks, as the viewers and the grimoire print it ({@code 60}, {@code 2.5}). */
    public static String seconds(int ticks) {
        return String.valueOf(ticks / 20.0).replaceAll("\\.0$", "");
    }

    /** The witch-recipe line shared by viewers and the grimoire: {@code A witch starts it: %s energy}. */
    public static final String REQUIREMENTS_KEY = "category." + HerbalWitch.MOD_ID + ".processing.witch";

    /** Footer key shared by viewers and the grimoire: {@code %s s} or {@code %s s, needs heat}. */
    public static String footerKey(boolean heated) {
        return "category." + HerbalWitch.MOD_ID + ".processing." + (heated ? "heated" : "time");
    }
}
