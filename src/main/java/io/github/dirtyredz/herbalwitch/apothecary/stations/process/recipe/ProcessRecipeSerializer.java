package io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * One serializer per processing type, sharing {@link ProcessRecipeJson} and differing only in the
 * type's bounds and recipe class.
 */
public final class ProcessRecipeSerializer<R extends ProcessRecipe> implements RecipeSerializer<R> {
    /** Builds the type's recipe class. */
    @FunctionalInterface
    public interface Factory<R extends ProcessRecipe> {
        R create(ResourceLocation id, ProcessRecipeDescription description);
    }

    private final ProcessRecipeJson.Bounds bounds;
    private final Factory<R> factory;

    public ProcessRecipeSerializer(ProcessRecipeJson.Bounds bounds, Factory<R> factory) {
        this.bounds = bounds;
        this.factory = factory;
    }

    public ProcessRecipeJson.Bounds bounds() {
        return bounds;
    }

    @Override
    public R fromJson(ResourceLocation id, JsonObject json) {
        return factory.create(id, ProcessRecipeJson.read(json, bounds));
    }

    @Override
    public R fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        return factory.create(id, ProcessRecipeJson.fromNetwork(buf, bounds));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, R recipe) {
        ProcessRecipeJson.toNetwork(buf, recipe.description());
    }
}
