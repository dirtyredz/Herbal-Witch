package io.github.dirtyredz.herbalwitch.data.server.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * The altar's crafting, one per craft: {@code SAS / LGL / L L} with two smooth-stone slabs, an
 * amethyst shard, four dark-oak logs and a gold ingot (the stone top, the crystal, the dark-wood
 * frame and the gold hardware). Unlocked by holding an amethyst shard.
 */
public final class AltarRecipes {
    private AltarRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, RitualItems.ALTAR.get())
                .pattern("SAS").pattern("LGL").pattern("L L")
                .define('S', Items.SMOOTH_STONE_SLAB).define('A', Items.AMETHYST_SHARD).define('L', Items.DARK_OAK_LOG)
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD)).save(writer);
    }
}
