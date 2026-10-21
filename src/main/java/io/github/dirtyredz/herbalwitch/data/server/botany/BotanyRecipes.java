package io.github.dirtyredz.herbalwitch.data.server.botany;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Garden acquisition that works in any world: the herb bed (dirt over a slab trough, two beds), and
 * one shapeless seed-selection recipe per starter herb (wheat seeds plus a companion plant).
 */
public final class BotanyRecipes {
    /** Starter herb -> the companion plant crafted with wheat seeds. */
    public static final Map<HerbSpec, Item> STARTER_COMPANIONS = Map.of(
            HerbCatalog.LAVENDER, Items.LILAC,
            HerbCatalog.SAGE, Items.FERN,
            HerbCatalog.MINT, Items.SUGAR_CANE,
            HerbCatalog.CHAMOMILE, Items.DANDELION);

    private BotanyRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BotanyItems.HERB_BED.get(), 2)
                .pattern("D D").pattern("SSS")
                .define('D', Items.DIRT).define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_dirt", has(Items.DIRT)).save(writer);
        for (HerbSpec herb : HerbCatalog.STARTERS) {
            Item companion = STARTER_COMPANIONS.get(herb);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BotanyItems.seeds(herb))
                    .requires(Items.WHEAT_SEEDS).requires(companion)
                    .unlockedBy("has_" + herb.id() + "_companion", has(companion)).save(writer);
        }
    }
}
