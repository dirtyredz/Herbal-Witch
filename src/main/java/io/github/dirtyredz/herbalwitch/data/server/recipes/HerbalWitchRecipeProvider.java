package io.github.dirtyredz.herbalwitch.data.server.recipes;

import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import io.github.dirtyredz.herbalwitch.data.server.grimoire.GrimoireRecipes;
import io.github.dirtyredz.herbalwitch.data.server.magic.ritual.AltarRecipes;
import io.github.dirtyredz.herbalwitch.data.server.preparations.PreparationCraftingProvider;
import io.github.dirtyredz.herbalwitch.data.server.stations.StationCraftingProvider;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.DryingRecipeProvider;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.GrindingRecipeProvider;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.InfusionRecipeProvider;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.SteepingRecipeProvider;
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
        StationCraftingProvider.addTo(writer);
        PreparationCraftingProvider.addTo(writer);
        DryingRecipeProvider.addTo(writer);
        GrindingRecipeProvider.addTo(writer);
        SteepingRecipeProvider.addTo(writer);
        InfusionRecipeProvider.addTo(writer);
        GrimoireRecipes.addTo(writer);
        AltarRecipes.addTo(writer);
    }
}
