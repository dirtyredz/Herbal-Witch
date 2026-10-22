package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** One fresh herb dries into its bundle, without heat. Type {@code herbalwitch:processing/drying}. */
public class DryingRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(1, false, false, false);

    public DryingRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.DRYING.get();
    }
}
