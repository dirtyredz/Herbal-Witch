package io.github.dirtyredz.herbalwitch.client.compat.jei;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.compat.ViewerLayout;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.registry.StationItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI support: the same four categories as EMI, from the same recipe descriptions, with each
 * station as catalyst. Loaded by JEI only when JEI is installed; no transfer handlers in Milestone 3.
 */
@JeiPlugin
public class HerbalWitchJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "jei");
    }

    static RecipeType<ProcessRecipe> type(ViewerLayout.Category category) {
        return RecipeType.create(HerbalWitch.MOD_ID, category.id(), ProcessRecipe.class);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (ViewerLayout.Category category : ViewerLayout.CATEGORIES) {
            registration.addRecipeCategories(new JeiProcessCategory(type(category), category,
                    registration.getJeiHelpers().getGuiHelper(), new ItemStack(StationItems.item(category.station()))));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        for (ViewerLayout.Category category : ViewerLayout.CATEGORIES) {
            List<ProcessRecipe> recipes = new ArrayList<>(
                    Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(category.type().get()));
            registration.addRecipes(type(category), recipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ViewerLayout.Category category : ViewerLayout.CATEGORIES) {
            registration.addRecipeCatalyst(new ItemStack(StationItems.item(category.station())), type(category));
        }
    }
}
