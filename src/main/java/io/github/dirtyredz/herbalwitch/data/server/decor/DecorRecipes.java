package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Survival acquisition: one ordinary crafting recipe per piece, in dark oak (the design's dark wood).
 * With a single design there is no restyling.
 */
public final class DecorRecipes {
    private DecorRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        shaped(ArtSpikeCatalog.ARMCHAIR, 1)
                .pattern("PWP").pattern("PPP").pattern("S S")
                .define('P', Items.DARK_OAK_PLANKS).define('W', ItemTags.WOOL).define('S', Items.STICK)
                .unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS)).save(writer);
        shaped(ArtSpikeCatalog.APOTHECARY_COUNTER, 1)
                .pattern("SSS").pattern("PNP").pattern("PPP")
                .define('S', Items.DARK_OAK_SLAB).define('P', Items.DARK_OAK_PLANKS).define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS)).save(writer);
        shaped(ArtSpikeCatalog.SMALL_WALL_SHELF, 2)
                .pattern("SSS").pattern("T T")
                .define('S', Items.DARK_OAK_SLAB).define('T', Items.STICK)
                .unlockedBy("has_slab", has(Items.DARK_OAK_SLAB)).save(writer);
        shaped(ArtSpikeCatalog.TABLE_LAMP, 1)
                .pattern("W").pattern("T").pattern("S")
                .define('W', ItemTags.WOOL).define('T', Items.TORCH).define('S', Items.DARK_OAK_SLAB)
                .unlockedBy("has_torch", has(Items.TORCH)).save(writer);
        shaped(ArtSpikeCatalog.SMALL_RUG, 2)
                .pattern("CSC")
                .define('C', ItemTags.WOOL_CARPETS).define('S', Items.STRING)
                .unlockedBy("has_carpet", has(ItemTags.WOOL_CARPETS)).save(writer);
    }

    private static ShapedRecipeBuilder shaped(FurnitureSpec spec, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, DecorItems.item(spec), count);
    }
}
