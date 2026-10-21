package io.github.dirtyredz.herbalwitch.stations.everyday.kettle.recipe;

import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Two ingredients steeped in one water bottle over heat; the bottle becomes the drink. Type {@code herbalwitch:processing/steeping}. */
public class SteepingRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(2, true, false, true);

    public SteepingRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.STEEPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.STEEPING.get();
    }
}
