package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.FixturesRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.LightingRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.OpeningsRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.SeatingRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.StorageRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.SurfacesRecipes;
import io.github.dirtyredz.herbalwitch.data.server.decor.recipes.TextilesRecipes;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Survival acquisition: one ordinary crafting recipe per piece, written through the sole recipe
 * provider. Each family contributes its own (data/server/decor/recipes/); only delivered pieces are
 * written. Wood is dark oak (the design's dark wood), upholstery wool or carpet, fittings iron and
 * glass, stonework the appropriate stone. With a single design there is no restyling.
 */
public final class DecorRecipes {
    private DecorRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        SeatingRecipes.addTo(writer);
        SurfacesRecipes.addTo(writer);
        StorageRecipes.addTo(writer);
        LightingRecipes.addTo(writer);
        TextilesRecipes.addTo(writer);
        FixturesRecipes.addTo(writer);
        OpeningsRecipes.addTo(writer);
    }

    /** Whether a piece is registered in this build (later waves are described but not delivered). */
    public static boolean delivered(FurnitureSpec spec) {
        return FurnitureCatalog.ALL.contains(spec);
    }

    /** A shaped recipe for {@code count} of a piece; the caller adds its unlock criterion. */
    public static ShapedRecipeBuilder shaped(FurnitureSpec spec, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, DecorItems.item(spec), count);
    }

    /** One of a piece, unlocked by having dark oak planks (every Milestone 5 piece). */
    public static ShapedRecipeBuilder piece(FurnitureSpec spec) {
        return shaped(spec, 1).unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS));
    }

    /**
     * One of a piece from a compact grid: rows separated by {@code /}, then key/ingredient pairs
     * (a {@code Character} followed by an {@code ItemLike} or an item {@code TagKey}). Unlocked by
     * dark oak planks; written only when the piece is delivered.
     */
    @SuppressWarnings("unchecked")
    public static void grid(Consumer<FinishedRecipe> writer, FurnitureSpec spec, String rows, Object... keys) {
        if (!delivered(spec)) {
            return;
        }
        ShapedRecipeBuilder recipe = piece(spec);
        for (String row : rows.split("/")) {
            recipe.pattern(row);
        }
        for (int i = 0; i < keys.length; i += 2) {
            char key = (Character) keys[i];
            if (keys[i + 1] instanceof TagKey<?> tag) {
                recipe.define(key, (TagKey<Item>) tag);
            } else {
                recipe.define(key, (ItemLike) keys[i + 1]);
            }
        }
        save(recipe, spec, writer);
    }

    /** Saves under the piece's recipe id ({@link FurnitureSpec#recipePath()}), when the piece is delivered. */
    public static void save(ShapedRecipeBuilder recipe, FurnitureSpec spec, Consumer<FinishedRecipe> writer) {
        if (delivered(spec)) {
            recipe.save(writer, ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, spec.recipePath()));
        }
    }
}
