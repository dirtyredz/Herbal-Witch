package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.OpeningsCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;

/** Door, window and archway recipes: a vanilla dark oak door with gold, glass panes in dark oak, stone bricks. */
public final class OpeningsRecipes {
    private OpeningsRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        grid(writer, OpeningsCatalog.DOOR, "D/G", 'D', Items.DARK_OAK_DOOR, 'G', Items.GOLD_NUGGET);
        grid(writer, OpeningsCatalog.WINDOW, "PGP/GTG/PGP", 'P', Items.DARK_OAK_PLANKS, 'G', Items.GLASS_PANE, 'T', Items.STICK);
        grid(writer, OpeningsCatalog.ARCHWAY, "XXX/X X/X X", 'X', Items.STONE_BRICKS);
    }
}
