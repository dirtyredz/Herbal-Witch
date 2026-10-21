package io.github.dirtyredz.herbalwitch.stations.everyday.mortar.recipe;

import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Up to three counted ingredients ground into a powder, dye or dust. Type {@code herbalwitch:processing/grinding}. */
public class GrindingRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(3, false, false, false);

    public GrindingRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.GRINDING.get();
    }
}
