package io.github.dirtyredz.herbalwitch.client.compat.jei;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.compat.ritual.RitualCardLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualItems;
import io.github.dirtyredz.herbalwitch.stations.description.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
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
 * JEI support: the same categories as EMI (processing types and the rituals), from the same recipe descriptions and
 * {@link ProcessingCategories}, with each station as catalyst. Loaded by JEI only when JEI is
 * installed; no transfer handlers. Every recipe is shown whatever the player has discovered.
 */
@JeiPlugin
public class HerbalWitchJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "jei");
    }

    static RecipeType<ProcessRecipe> type(ProcessingCategories.Category category) {
        return RecipeType.create(HerbalWitch.MOD_ID, category.id(), ProcessRecipe.class);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            registration.addRecipeCategories(new JeiProcessCategory(type(category), category,
                    registration.getJeiHelpers().getGuiHelper(), new ItemStack(StationItems.item(category.station()))));
        }
        registration.addRecipeCategories(new JeiRitualCategory(registration.getJeiHelpers().getGuiHelper(), new ItemStack(RitualItems.ALTAR.get())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            List<ProcessRecipe> recipes = new ArrayList<>(
                    Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(category.type().get()));
            registration.addRecipes(type(category), recipes);
        }
        registration.addRecipes(JeiRitualCategory.TYPE, RitualCardLayout.actions());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            registration.addRecipeCatalyst(new ItemStack(StationItems.item(category.station())), type(category));
        }
        registration.addRecipeCatalyst(new ItemStack(RitualItems.ALTAR.get()), JeiRitualCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(RitualItems.CIRCLE.get()), JeiRitualCategory.TYPE);
    }
}
