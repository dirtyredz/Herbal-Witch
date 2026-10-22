package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** Seating recipes: dark oak frames, wool upholstery, iron for the garden pieces (docs/catalogue/decor.md lists every grid). */
public final class SeatingRecipes {
    private SeatingRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        DecorRecipes.save(DecorRecipes.shaped(SeatingCatalog.ARMCHAIR, 1)
                .pattern("PWP").pattern("PPP").pattern("S S")
                .define('P', Items.DARK_OAK_PLANKS).define('W', ItemTags.WOOL).define('S', Items.STICK)
                .unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS)), SeatingCatalog.ARMCHAIR, writer);
        grid(writer, SeatingCatalog.DINING_CHAIR, "P  /PWP/T T", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'T', Items.STICK);
        grid(writer, SeatingCatalog.BENCH, "P P/WWW/T T", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'T', Items.STICK);
        grid(writer, SeatingCatalog.SOFA, "WWW/WWW/PPP", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL);
        grid(writer, SeatingCatalog.LOVESEAT, "W W/WWW/PPP", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL);
        grid(writer, SeatingCatalog.CHAISE_LOUNGE, "WW /WWW/PTP", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'T', Items.STICK);
        grid(writer, SeatingCatalog.VANITY_CHAIR, " W /PWP/T T", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'T', Items.STICK);
        grid(writer, SeatingCatalog.DESK_CHAIR, "P  /PWP/ I ", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'I', Items.IRON_INGOT);
        grid(writer, SeatingCatalog.READING_CHAIR, "WPW/WWW/T T", 'P', Items.DARK_OAK_PLANKS, 'W', ItemTags.WOOL, 'T', Items.STICK);
        grid(writer, SeatingCatalog.ENTRY_BENCH, "TGT/SSS/T T", 'S', Items.DARK_OAK_SLAB, 'G', Items.GOLD_NUGGET, 'T', Items.STICK);
        grid(writer, SeatingCatalog.GARDEN_BENCH, "SSS/I I", 'S', Items.DARK_OAK_SLAB, 'I', Items.IRON_INGOT);
        grid(writer, SeatingCatalog.OUTDOOR_CHAIR, "S  /SSS/I I", 'S', Items.DARK_OAK_SLAB, 'I', Items.IRON_INGOT);
        grid(writer, SeatingCatalog.GARDEN_SWING, "PCP/C C/SWS", 'P', Items.DARK_OAK_PLANKS, 'C', Items.CHAIN, 'S', Items.DARK_OAK_SLAB,
                'W', ItemTags.WOOL);
    }
}
