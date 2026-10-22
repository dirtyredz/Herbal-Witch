package io.github.dirtyredz.herbalwitch.data.server.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Shapeless grimoire crafting, one item each: book + ink sac + any starter fresh herb make the
 * grimoire; three dark oak slabs + two sticks + a grimoire make the stand; six dark oak planks + two
 * books + a grimoire make the witch's bookshelf. Using a grimoire as an ingredient consumes the item,
 * never the crafter's knowledge.
 */
public final class GrimoireRecipes {
    private GrimoireRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        Item book = GrimoireItems.BOOK.get();
        Ingredient starters = Ingredient.of(HerbCatalog.STARTERS.stream().map(BotanyItems::fresh).toArray(Item[]::new));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, book)
                .requires(Items.BOOK).requires(Items.INK_SAC).requires(starters)
                .unlockedBy("has_fresh_herb", has(BotanyItems.fresh(HerbCatalog.LAVENDER)))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, GrimoireItems.item(GrimoireCatalog.STAND))
                .requires(Items.DARK_OAK_SLAB, 3).requires(Items.STICK, 2).requires(book)
                .unlockedBy("has_grimoire", has(book))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, GrimoireItems.item(GrimoireCatalog.BOOKSHELF))
                .requires(Items.DARK_OAK_PLANKS, 6).requires(Items.BOOK, 2).requires(book)
                .unlockedBy("has_grimoire", has(book))
                .save(writer);
    }
}
