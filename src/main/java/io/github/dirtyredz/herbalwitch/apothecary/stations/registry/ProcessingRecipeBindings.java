package io.github.dirtyredz.herbalwitch.apothecary.stations.registry;

import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories.Category;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.Optional;

/** Resolves registered recipe types only when a runtime consumer asks. */
public final class ProcessingRecipeBindings {
    private ProcessingRecipeBindings() { }

    public static RecipeType<? extends ProcessRecipe> type(Category category) {
        return switch (category.id()) {
            case "drying" -> StationRecipes.DRYING.get();
            case "grinding" -> StationRecipes.GRINDING.get();
            case "steeping" -> StationRecipes.STEEPING.get();
            case "infusion" -> StationRecipes.INFUSION.get();
            case "cauldron" -> StationRecipes.CAULDRON.get();
            case "distillation" -> StationRecipes.DISTILLATION.get();
            case "bottling" -> StationRecipes.BOTTLING.get();
            case "charm_work" -> StationRecipes.CHARM_WORK.get();
            case "broom_work" -> StationRecipes.BROOM_WORK.get();
            case "wand_carving" -> StationRecipes.WAND_CARVING.get();
            default -> throw new IllegalArgumentException("Unknown processing category " + category.id());
        };
    }

    public static Optional<Category> byType(RecipeType<?> type) {
        return ProcessingCategories.ALL.stream().filter(c -> type(c) == type).findFirst();
    }
}
