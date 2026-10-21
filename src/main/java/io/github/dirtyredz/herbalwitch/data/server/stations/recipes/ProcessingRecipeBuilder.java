package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeSerializer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Writes a processing recipe through the runtime format ({@link ProcessRecipeJson}), validated by
 * the same bounds the server applies when loading. No recipe-book advancements: stations are
 * unlocked by their own crafting advancements.
 */
public final class ProcessingRecipeBuilder {
    private final ProcessRecipeSerializer<?> serializer;
    private final List<ProcessRecipeDescription.Counted> ingredients = new ArrayList<>();
    private ProcessRecipeDescription.Counted base;
    private ProcessRecipeDescription.Counted packaging;
    private final List<ItemStack> returns = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    private int duration;

    private ProcessingRecipeBuilder(ProcessRecipeSerializer<?> serializer, int duration) {
        this.serializer = serializer;
        this.duration = duration;
    }

    public static ProcessingRecipeBuilder of(ProcessRecipeSerializer<?> serializer, int duration) {
        return new ProcessingRecipeBuilder(serializer, duration);
    }

    /** A vanilla water bottle exactly (other potions do not match). */
    public static Ingredient water() {
        ItemStack water = net.minecraft.world.item.alchemy.PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        return PartialNBTIngredient.of(Items.POTION, water.getTag());
    }

    public ProcessingRecipeBuilder input(Ingredient ingredient, int count) {
        ingredients.add(new ProcessRecipeDescription.Counted(ingredient, count));
        return this;
    }

    public ProcessingRecipeBuilder input(ItemLike item, int count) {
        return input(Ingredient.of(item), count);
    }

    public ProcessingRecipeBuilder input(ItemLike item) {
        return input(item, 1);
    }

    public ProcessingRecipeBuilder base(Ingredient ingredient) {
        base = new ProcessRecipeDescription.Counted(ingredient, 1);
        return this;
    }

    public ProcessingRecipeBuilder packaging(ItemLike item) {
        packaging = new ProcessRecipeDescription.Counted(Ingredient.of(item), 1);
        return this;
    }

    public ProcessingRecipeBuilder returns(ItemLike item) {
        returns.add(new ItemStack(item));
        return this;
    }

    public ProcessingRecipeBuilder result(ItemLike item, int count) {
        result = new ItemStack(item, count);
        return this;
    }

    public ProcessingRecipeBuilder result(ItemLike item) {
        return result(item, 1);
    }

    /** Saves as {@code herbalwitch:<path>}. */
    public void save(Consumer<FinishedRecipe> writer, String path) {
        ProcessRecipeDescription description = ProcessRecipeJson.validate(new ProcessRecipeDescription(ingredients,
                Optional.ofNullable(base), Optional.ofNullable(packaging), result, returns, duration,
                serializer.bounds().heated()), serializer.bounds());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
        writer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                ProcessRecipeJson.write(description).entrySet().forEach(e -> json.add(e.getKey(), e.getValue()));
            }

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return serializer;
            }

            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}
