package io.github.dirtyredz.herbalwitch.stations.advanced.still.recipe;

import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Two ingredients distilled from a base liquid over heat into one bottle, by a witch. Type {@code herbalwitch:processing/distillation}. */
public class DistillationRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(2, true, true, true, ProcessRequirements.Policy.MAGICAL);

    public DistillationRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.DISTILLATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.DISTILLATION.get();
    }
}
