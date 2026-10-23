package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.wand_bench;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.*;
import io.github.dirtyredz.herbalwitch.platform.recipe.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
public final class WandCarvingRecipe extends ProcessRecipe {
 public static final ProcessRecipeJson.Bounds BOUNDS = new ProcessRecipeJson.Bounds(3, false, false, false, ProcessRequirements.Policy.MAGICAL);
 public WandCarvingRecipe(ResourceLocation id, ProcessRecipeDescription description) { super(id,description); }
 @Override public RecipeSerializer<?> getSerializer() { return StationRecipes.WAND_CARVING_SERIALIZER.get(); }
 @Override public RecipeType<?> getType() { return StationRecipes.WAND_CARVING.get(); }
}
