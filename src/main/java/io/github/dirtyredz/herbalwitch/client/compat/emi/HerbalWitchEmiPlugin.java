package io.github.dirtyredz.herbalwitch.client.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.compat.ViewerLayout;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.registry.StationItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * EMI support: four categories (drying, grinding, steeping, infusion) with their station as the
 * workstation. Discovered by EMI only when EMI is installed; no transfer handlers in Milestone 3.
 */
@EmiEntrypoint
public class HerbalWitchEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (ViewerLayout.Category category : ViewerLayout.CATEGORIES) {
            EmiStack station = EmiStack.of(StationItems.item(category.station()));
            EmiRecipeCategory emi = new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "processing/" + category.id()), station);
            registry.addCategory(emi);
            registry.addWorkstation(emi, station);
            addRecipes(registry, emi, category.type().get());
        }
    }

    private static <R extends ProcessRecipe> void addRecipes(EmiRegistry registry, EmiRecipeCategory emi, RecipeType<R> type) {
        for (R recipe : registry.getRecipeManager().getAllRecipesFor(type)) {
            registry.addRecipe(new EmiProcessRecipe(emi, recipe));
        }
    }
}
