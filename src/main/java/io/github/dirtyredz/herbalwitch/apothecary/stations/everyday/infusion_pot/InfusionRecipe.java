package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Two ingredients infused into a base liquid over heat, packed into a container. Type {@code herbalwitch:processing/infusion}. */
public class InfusionRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(2, true, true, true, ProcessRequirements.Policy.EITHER);

    public InfusionRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.INFUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.INFUSION.get();
    }
}
