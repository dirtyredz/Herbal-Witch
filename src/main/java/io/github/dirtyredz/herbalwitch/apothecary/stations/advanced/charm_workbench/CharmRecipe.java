package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Up to three ingredients worked into one protective charm, by a witch. Type {@code herbalwitch:processing/charm_work}. */
public class CharmRecipe extends ProcessRecipe {
    public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(3, false, false, false, ProcessRequirements.Policy.MAGICAL);

    public CharmRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        super(id, description);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StationRecipes.CHARM_WORK_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StationRecipes.CHARM_WORK.get();
    }
}
