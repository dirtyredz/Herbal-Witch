package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Survival acquisition. Ordinary crafting makes each piece in the base (dark oak) finish; the
 * stonecutter then restyles any finish of a piece into any other finish of the same piece,
 * one for one. Each restyle recipe lists every other finish (never the result itself), so the
 * stonecutter offers exactly the 17 alternatives and no recipe can change the piece.
 */
public final class DecorRecipes {
    /** Suffix of the stonecutter restyling recipe ids. */
    public static final String RESTYLE_SUFFIX = "_from_restyling";

    private DecorRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        baseRecipes(writer);
        for (FurnitureSpec spec : ArtSpikeCatalog.ALL) {
            for (Finish target : FinishCatalog.ALL) {
                restyle(writer, spec, target);
            }
        }
    }

    private static void baseRecipes(Consumer<FinishedRecipe> writer) {
        Finish base = FinishCatalog.BASE;
        shaped(ArtSpikeCatalog.ARMCHAIR, base, 1)
                .pattern("PWP").pattern("PPP").pattern("S S")
                .define('P', Items.DARK_OAK_PLANKS).define('W', ItemTags.WOOL).define('S', Items.STICK)
                .unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS)).save(writer);
        shaped(ArtSpikeCatalog.APOTHECARY_COUNTER, base, 1)
                .pattern("SSS").pattern("PNP").pattern("PPP")
                .define('S', Items.DARK_OAK_SLAB).define('P', Items.DARK_OAK_PLANKS).define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS)).save(writer);
        shaped(ArtSpikeCatalog.SMALL_WALL_SHELF, base, 2)
                .pattern("SSS").pattern("T T")
                .define('S', Items.DARK_OAK_SLAB).define('T', Items.STICK)
                .unlockedBy("has_slab", has(Items.DARK_OAK_SLAB)).save(writer);
        shaped(ArtSpikeCatalog.TABLE_LAMP, base, 1)
                .pattern("W").pattern("T").pattern("S")
                .define('W', ItemTags.WOOL).define('T', Items.TORCH).define('S', Items.DARK_OAK_SLAB)
                .unlockedBy("has_torch", has(Items.TORCH)).save(writer);
        shaped(ArtSpikeCatalog.SMALL_RUG, base, 2)
                .pattern("CSC")
                .define('C', ItemTags.WOOL_CARPETS).define('S', Items.STRING)
                .unlockedBy("has_carpet", has(ItemTags.WOOL_CARPETS)).save(writer);
    }

    private static ShapedRecipeBuilder shaped(FurnitureSpec spec, Finish finish, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, DecorItems.item(spec, finish), count)
                .group(HerbalWitch.MOD_ID + ":" + spec.id());
    }

    private static void restyle(Consumer<FinishedRecipe> writer, FurnitureSpec spec, Finish target) {
        Item[] sources = FinishCatalog.ALL.stream()
                .filter(finish -> !finish.equals(target))
                .map(finish -> DecorItems.item(spec, finish))
                .toArray(Item[]::new);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(sources), RecipeCategory.DECORATIONS,
                        DecorItems.item(spec, target), 1)
                .unlockedBy("has_" + spec.id(), has(DecorTags.pieceItems(spec)))
                .save(writer, ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, spec.path(target) + RESTYLE_SUFFIX));
    }
}
