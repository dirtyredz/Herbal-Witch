package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.recipe;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Up to three ingredients brewed into a water base over heat, by a witch; one four-dose mixture. Type {@code herbalwitch:processing/cauldron}. */
public class CauldronRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(3, true, false, true, ProcessRequirements.Policy.MAGICAL);

    public CauldronRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.CAULDRON_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.CAULDRON.get();
    }
}
