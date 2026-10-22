package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Cabinet and shelf recipes: a chest inside dark oak for a 27-slot cabinet, two for a 54-slot one;
 * slabs and sticks for open display shelves (no chest: they show, rather than hide, what they hold).
 */
public final class StorageRecipes {
    private static final Object P = Items.DARK_OAK_PLANKS;
    private static final Object S = Items.DARK_OAK_SLAB;
    private static final Object T = Items.STICK;
    private static final Object H = Items.CHEST;

    private StorageRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        DecorRecipes.save(DecorRecipes.shaped(StorageCatalog.SMALL_WALL_SHELF, 2)
                .pattern("SSS").pattern("T T")
                .define('S', Items.DARK_OAK_SLAB).define('T', Items.STICK)
                .unlockedBy("has_slab", has(Items.DARK_OAK_SLAB)), StorageCatalog.SMALL_WALL_SHELF, writer);
        grid(writer, StorageCatalog.LOWER_CABINET, "SSS/PHP/PPP", 'S', S, 'P', P, 'H', H);
        grid(writer, StorageCatalog.UPPER_CABINET, "PPP/PHP/T T", 'P', P, 'H', H, 'T', T);
        grid(writer, StorageCatalog.PANTRY, "PSP/PHP/PSP", 'S', S, 'P', P, 'H', H);
        grid(writer, StorageCatalog.SIDEBOARD, "SSS/PHP/PHP", 'S', S, 'P', P, 'H', H);
        grid(writer, StorageCatalog.CHINA_CABINET, "PGP/HGH/PPP", 'P', P, 'G', Items.GLASS, 'H', H);
        grid(writer, StorageCatalog.WINE_RACK, "PBP/BHB/PBP", 'P', P, 'B', Items.GLASS_BOTTLE, 'H', H);
        grid(writer, StorageCatalog.INGREDIENT_CABINET, "GPG/PHP/GPG", 'P', P, 'G', Items.GOLD_NUGGET, 'H', H);
        grid(writer, StorageCatalog.OPEN_SHELVES, "TST/TST/TST", 'S', S, 'T', T);
        grid(writer, StorageCatalog.POTION_SHELF, "TST/TBT/TST", 'S', S, 'T', T, 'B', Items.GLASS_BOTTLE);
        grid(writer, StorageCatalog.NIGHTSTAND, " S /PHP/T T", 'S', S, 'P', P, 'H', H, 'T', T);
        grid(writer, StorageCatalog.DRESSER, "SSS/HGH/PPP", 'S', S, 'P', P, 'H', H, 'G', Items.GOLD_NUGGET);
        grid(writer, StorageCatalog.WARDROBE, "PPP/HPH/PGP", 'P', P, 'H', H, 'G', Items.GOLD_NUGGET);
        grid(writer, StorageCatalog.TRUNK, "NPN/HGH/PPP", 'N', Items.IRON_NUGGET, 'P', P, 'H', H, 'G', Items.GOLD_NUGGET);
        grid(writer, StorageCatalog.ROLLTOP_DESK, "PPP/HBH/PSP", 'P', P, 'H', H, 'B', Items.BOOK, 'S', S);
        grid(writer, StorageCatalog.FILING_CABINET, "PNP/PHP/PNP", 'P', P, 'N', Items.IRON_NUGGET, 'H', H);
        grid(writer, StorageCatalog.SHOE_CABINET, "SSS/PHP", 'S', S, 'P', P, 'H', H);
        grid(writer, StorageCatalog.BOOKSHELF, "PBP/S S/PBP", 'P', P, 'B', Items.BOOK, 'S', S);
        grid(writer, StorageCatalog.LINEN_CABINET, "PWP/PHP/PWP", 'P', P, 'W', ItemTags.WOOL, 'H', H);
        grid(writer, StorageCatalog.STORAGE_CABINET, "PHP/PPP/PHP", 'P', P, 'H', H);
        grid(writer, StorageCatalog.WALL_SHELF_MEDIUM, "SSS/SSS/T T", 'S', S, 'T', T);
        grid(writer, StorageCatalog.WALL_SHELF_LARGE, "SSS/T T/SSS", 'S', S, 'T', T);
        grid(writer, StorageCatalog.CORNER_SHELF, "S  /SS /SSS", 'S', S);
        grid(writer, StorageCatalog.FREE_STANDING_SHELF_SHORT, "T T/SSS/T T", 'S', S, 'T', T);
        grid(writer, StorageCatalog.FREE_STANDING_SHELF_MEDIUM, "TST/T T/TST", 'S', S, 'T', T);
        grid(writer, StorageCatalog.FREE_STANDING_SHELF_LARGE, "TST/TST/T T", 'S', S, 'T', T);
        grid(writer, StorageCatalog.LADDER_SHELF, "TS /TS /TSS", 'S', S, 'T', T);
    }
}
