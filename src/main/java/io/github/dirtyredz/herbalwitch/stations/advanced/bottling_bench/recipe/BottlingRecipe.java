package io.github.dirtyredz.herbalwitch.stations.advanced.bottling_bench.recipe;

import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** A whole mixture portioned into counted bottles or vials, optionally labelled; ordinary work. Type {@code herbalwitch:processing/bottling}. */
public class BottlingRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(3, false, true, false, ProcessRequirements.Policy.ORDINARY);

    public BottlingRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.BOTTLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.BOTTLING.get();
    }
}
