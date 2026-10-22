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
 * Witch recipes on the everyday stations (their ordinary recipes are unchanged): the mortar's blinding
 * dust ({@code processing/grinding/blinding_dust}, 100 ticks, four doses) and the infusion pot's
 * oils ({@code processing/infusion/oils/<oil>}, 600 heated ticks, plant oil base, a glass bottle to
 * fill, the base's bottle back).
 */
public final class MagicalEverydayRecipes {
    private MagicalEverydayRecipes() {
    }

    private static void oil(Consumer<FinishedRecipe> writer, ProductSpec product, int energy, ItemLike first, ItemLike second) {
        ProcessingRecipeBuilder.of(StationRecipes.INFUSION_SERIALIZER.get(), InfusionRecipeProvider.DURATION)
                .input(first).input(second).base(Ingredient.of(PreparationItems.product(EverydayProductCatalog.PLANT_OIL)))
                .packaging(Items.GLASS_BOTTLE).result(AdvancedPreparations.item(product)).returns(Items.GLASS_BOTTLE).witch(energy)
                .save(writer, "processing/infusion/oils/" + product.id());
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        ProcessingRecipeBuilder.of(StationRecipes.GRINDING_SERIALIZER.get(), GrindingRecipeProvider.DURATION)
                .input(PreparationItems.powder(HerbCatalog.NIGHTSHADE)).input(Items.INK_SAC).input(Items.SAND)
                .result(AdvancedPreparations.item(AdvancedProductCatalog.BLINDING_DUST), 4).witch(5)
                .save(writer, "processing/grinding/blinding_dust");
        oil(writer, AdvancedProductCatalog.VENOM_OIL, 10, PreparationItems.powder(HerbCatalog.BELLADONNA), Items.SPIDER_EYE);
        oil(writer, AdvancedProductCatalog.THORN_OIL, 10, PreparationItems.powder(HerbCatalog.NETTLE), Items.CACTUS);
        oil(writer, AdvancedProductCatalog.UNDEAD_REPELLENT, 8, PreparationItems.powder(HerbCatalog.SAGE), Items.ROTTEN_FLESH);
        oil(writer, AdvancedProductCatalog.ARTHROPOD_REPELLENT, 8, PreparationItems.powder(HerbCatalog.LAVENDER), Items.SPIDER_EYE);
        oil(writer, FamiliarPreparationCatalog.EMBERGUARD, 10, PreparationItems.powder(HerbCatalog.ROSEMARY), Items.BLAZE_POWDER);
    }
}
