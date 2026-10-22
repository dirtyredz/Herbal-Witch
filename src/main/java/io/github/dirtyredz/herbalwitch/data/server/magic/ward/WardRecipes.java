package io.github.dirtyredz.herbalwitch.data.server.magic.ward;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** The ward stone, shapeless: four stone bricks, an amethyst shard and a gold ingot; and the stone's mining tag. */
public final class WardRecipes {
    private WardRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, WardRegistration.STONE_ITEM.get())
                .requires(Items.STONE_BRICKS, 4).requires(Items.AMETHYST_SHARD).requires(Items.GOLD_INGOT)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD)).save(writer);
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(WardRegistration.STONE.get());
    }
}
