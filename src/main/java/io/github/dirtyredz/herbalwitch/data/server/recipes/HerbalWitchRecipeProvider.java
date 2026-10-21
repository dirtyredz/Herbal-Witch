package io.github.dirtyredz.herbalwitch.data.server.recipes;

import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * The only recipe provider: vanilla names every recipe provider "Recipes", and the data generator
 * refuses two providers with one name, so features contribute here. Also re-exports vanilla's
 * unlock triggers for the contributors.
 */
public class HerbalWitchRecipeProvider extends RecipeProvider {
    public HerbalWitchRecipeProvider(PackOutput output) {
        super(output);
    }

    public static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return RecipeProvider.has(item);
    }

    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return RecipeProvider.has(tag);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        DecorRecipes.addTo(writer);
        BotanyRecipes.addTo(writer);
    }
}
