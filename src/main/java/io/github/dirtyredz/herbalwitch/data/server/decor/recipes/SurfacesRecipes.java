package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** Table, counter and desk recipes: dark oak slabs for tops, planks and sticks for bodies and legs, stone for worktops. */
public final class SurfacesRecipes {
    private static final Object P = Items.DARK_OAK_PLANKS;
    private static final Object S = Items.DARK_OAK_SLAB;
    private static final Object T = Items.STICK;

    private SurfacesRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        DecorRecipes.save(DecorRecipes.shaped(SurfacesCatalog.APOTHECARY_COUNTER, 1)
                .pattern("SSS").pattern("PNP").pattern("PPP")
                .define('S', Items.DARK_OAK_SLAB).define('P', Items.DARK_OAK_PLANKS).define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_planks", has(Items.DARK_OAK_PLANKS)), SurfacesCatalog.APOTHECARY_COUNTER, writer);
        grid(writer, SurfacesCatalog.KITCHEN_COUNTER, "XXX/PSP/PPP", 'X', Items.SMOOTH_STONE_SLAB, 'P', P, 'S', S);
        grid(writer, SurfacesCatalog.CORNER_COUNTER, "XXX/PPS/PPP", 'X', Items.SMOOTH_STONE_SLAB, 'P', P, 'S', S);
        grid(writer, SurfacesCatalog.KITCHEN_ISLAND, "LLL/T T/S S", 'L', Items.DARK_OAK_LOG, 'T', T, 'S', S);
        grid(writer, SurfacesCatalog.DINING_TABLE, "SSS/PCP/T T", 'S', S, 'P', P, 'C', ItemTags.WOOL_CARPETS, 'T', T);
        grid(writer, SurfacesCatalog.SERVING_CART, "NSN/NSN/I I", 'N', Items.GOLD_NUGGET, 'S', S, 'I', Items.IRON_INGOT);
        grid(writer, SurfacesCatalog.WORK_TABLE, "SSS/PIP/P P", 'S', S, 'P', P, 'I', Items.IRON_INGOT);
        grid(writer, SurfacesCatalog.COFFEE_TABLE, "SCS/T T", 'S', S, 'C', ItemTags.WOOL_CARPETS, 'T', T);
        grid(writer, SurfacesCatalog.SIDE_TABLE, " S / T /TTT", 'S', S, 'T', T);
        grid(writer, SurfacesCatalog.CONSOLE_TABLE, "SSS/T T/T T", 'S', S, 'T', T);
        grid(writer, SurfacesCatalog.TV_STAND, "SSS/GPG/P P", 'S', S, 'G', Items.GLASS_PANE, 'P', P);
        grid(writer, SurfacesCatalog.VANITY_TABLE, "PGP/SSS/P P", 'P', P, 'G', Items.GLASS, 'S', S);
        grid(writer, SurfacesCatalog.WRITING_DESK, "SFS/PSP/P P", 'S', S, 'F', Items.FEATHER, 'P', P);
        grid(writer, SurfacesCatalog.MAP_TABLE, "SMS/PSP/P P", 'S', S, 'M', Items.MAP, 'P', P);
        grid(writer, SurfacesCatalog.GARDEN_TABLE, "SSS/NIN/ I ", 'S', S, 'N', Items.IRON_NUGGET, 'I', Items.IRON_INGOT);
    }
}
