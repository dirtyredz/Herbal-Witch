package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Up to three counted ingredients ground into a powder, dye or dust. Type {@code herbalwitch:processing/grinding}. */
public class GrindingRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(3, false, false, false, ProcessRequirements.Policy.EITHER);

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
