package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.LightingCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** Lamp recipes: a torch, lantern or candles for the light, dark oak, iron chain and gold for hanging lamps. */
public final class LightingRecipes {
    private LightingRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        DecorRecipes.save(DecorRecipes.shaped(LightingCatalog.TABLE_LAMP, 1)
                .pattern("W").pattern("T").pattern("S")
                .define('W', ItemTags.WOOL).define('T', Items.TORCH).define('S', Items.DARK_OAK_SLAB)
                .unlockedBy("has_torch", has(Items.TORCH)), LightingCatalog.TABLE_LAMP, writer);
        grid(writer, LightingCatalog.WALL_SCONCE, " R /NPN", 'R', Items.TORCH, 'N', Items.GOLD_NUGGET, 'P', Items.DARK_OAK_PLANKS);
        grid(writer, LightingCatalog.HANGING_LANTERN, "GCG/ L ", 'G', Items.GOLD_NUGGET, 'C', Items.CHAIN, 'L', Items.LANTERN);
        grid(writer, LightingCatalog.CHANDELIER, "GCG/KKK", 'G', Items.GOLD_INGOT, 'C', Items.CHAIN, 'K', Items.CANDLE);
        grid(writer, LightingCatalog.CEILING_LIGHT, " C /GLG", 'C', Items.CHAIN, 'G', Items.GOLD_NUGGET, 'L', Items.LANTERN);
        grid(writer, LightingCatalog.FLOOR_LAMP, "WRW/ T / S ", 'W', ItemTags.WOOL, 'R', Items.TORCH, 'T', Items.STICK, 'S', Items.DARK_OAK_SLAB);
        grid(writer, LightingCatalog.POST_LANTERN, " L / T /SSS", 'L', Items.LANTERN, 'T', Items.STICK, 'S', Items.DARK_OAK_SLAB);
        grid(writer, LightingCatalog.PENDANT_LIGHT, " C / C / L ", 'C', Items.CHAIN, 'L', Items.LANTERN);
    }
}
