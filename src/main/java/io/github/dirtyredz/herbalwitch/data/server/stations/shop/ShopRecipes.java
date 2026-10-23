package io.github.dirtyredz.herbalwitch.data.server.stations.shop;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** The four shapeless commerce recipes, each yielding one piece, from the catalogue's ingredients. */
public final class ShopRecipes {
    private ShopRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> out) {
        for (ShopPieceSpec spec : ShopPieceCatalog.ALL) {
            ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ShopItems.item(spec));
            Item first = null;
            for (Map.Entry<String, Integer> ingredient : spec.ingredients().entrySet()) {
                Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft", ingredient.getKey()));
                if (item == null) throw new IllegalStateException("unknown ingredient " + ingredient.getKey());
                builder.requires(item, ingredient.getValue());
                if (first == null) first = item;
            }
            builder.unlockedBy("has_ingredient", has(first)).save(out);
        }
    }
}
