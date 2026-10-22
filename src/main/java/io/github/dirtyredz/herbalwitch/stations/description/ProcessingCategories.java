package io.github.dirtyredz.herbalwitch.stations.description;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.recipe.DryingRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.recipe.InfusionRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.recipe.SteepingRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.recipe.GrindingRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The four processing categories and the station that runs each: the one mapping the grimoire, EMI
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

    /** Station order. */
    public static final List<Category> ALL = List.of(DRYING, GRINDING, STEEPING, INFUSION);

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

    /** Footer key shared by viewers and the grimoire: {@code %s s} or {@code %s s, needs heat}. */
    public static String footerKey(boolean heated) {
        return "category." + HerbalWitch.MOD_ID + ".processing." + (heated ? "heated" : "time");
    }
}
