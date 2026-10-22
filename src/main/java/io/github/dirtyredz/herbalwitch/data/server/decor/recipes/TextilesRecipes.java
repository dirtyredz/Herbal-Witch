package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.TextilesCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** Rug, curtain and screen recipes: carpet and wool, string, dark oak rods, chain for hanging pieces. */
public final class TextilesRecipes {
    private TextilesRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        DecorRecipes.save(DecorRecipes.shaped(TextilesCatalog.SMALL_RUG, 2)
                .pattern("CSC")
                .define('C', ItemTags.WOOL_CARPETS).define('S', Items.STRING)
                .unlockedBy("has_carpet", has(ItemTags.WOOL_CARPETS)), TextilesCatalog.SMALL_RUG, writer);
        grid(writer, TextilesCatalog.MEDIUM_RUG, "CCC/CSC", 'C', ItemTags.WOOL_CARPETS, 'S', Items.STRING);
        grid(writer, TextilesCatalog.LARGE_RUG, "CCC/CSC/CCC", 'C', ItemTags.WOOL_CARPETS, 'S', Items.STRING);
        grid(writer, TextilesCatalog.CURTAIN, "TTT/W W/W W", 'T', Items.STICK, 'W', ItemTags.WOOL);
        grid(writer, TextilesCatalog.ROOM_DIVIDER, "PWP/PWP/T T", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'T', Items.STICK);
        grid(writer, TextilesCatalog.HANGING_DIVIDER, "CCC/W W/W W", 'C', Items.CHAIN, 'W', ItemTags.WOOL);
    }
}
