package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * The still, 800 heated ticks, witch recipes: two ingredients distilled from a base into one empty
 * glass bottle; the base's container comes back ({@code processing/distillation/<product>}).
 */
public final class DistillationRecipes {
    public static final int DURATION = 800;

    private DistillationRecipes() {
    }

    private static void distil(Consumer<FinishedRecipe> writer, ProductSpec product, int energy, Ingredient base, ItemLike first,
                               ItemLike second) {
        ProcessingRecipeBuilder.of(StationRecipes.DISTILLATION_SERIALIZER.get(), DURATION)
                .input(first).input(second).base(base).packaging(Items.GLASS_BOTTLE)
                .result(AdvancedPreparations.item(product)).returns(Items.GLASS_BOTTLE).witch(energy)
                .save(writer, "processing/distillation/" + product.id());
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        Ingredient water = ProcessingRecipeBuilder.water();
        Ingredient oil = Ingredient.of(PreparationItems.product(EverydayProductCatalog.PLANT_OIL));
        distil(writer, AdvancedProductCatalog.MOON_ESSENCE, 10, water, PreparationItems.powder(HerbCatalog.MUGWORT), Items.AMETHYST_SHARD);
        distil(writer, FamiliarPreparationCatalog.WARDING, 15, water, PreparationItems.powder(HerbCatalog.SAGE), Items.QUARTZ);
        distil(writer, FamiliarPreparationCatalog.VENOMLEAF, 15, oil, PreparationItems.powder(HerbCatalog.BELLADONNA), Items.SPIDER_EYE);
    }
}
