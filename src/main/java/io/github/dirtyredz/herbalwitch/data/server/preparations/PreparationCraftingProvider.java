package io.github.dirtyredz.herbalwitch.data.server.preparations;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/** The oil lantern: four iron nuggets round a torch over a glass bottle. */
public final class PreparationCraftingProvider {
    private PreparationCraftingProvider() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, PreparationItems.OIL_LANTERN.get())
                .pattern("N N").pattern("NTN").pattern(" G ")
                .define('N', Tags.Items.NUGGETS_IRON).define('T', Items.TORCH).define('G', Items.GLASS_BOTTLE)
                .unlockedBy("has_torch", has(Items.TORCH)).save(writer);
    }
}
