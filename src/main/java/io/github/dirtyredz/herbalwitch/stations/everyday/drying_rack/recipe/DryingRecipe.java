package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.recipe;

import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
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
