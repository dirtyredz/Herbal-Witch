package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.COOLING_REMEDY;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.ENERGY_TEA;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.MINING_TONIC;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.RECOVERY_TEA;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.RESTFUL_INFUSION;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.TRAVEL_TONIC;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog.WARMTH_REMEDY;

/**
 * Steeping, 200 heated ticks: two ingredients and one water bottle make one bottled drink; the
 * water's bottle becomes the drink, so nothing is returned ({@code processing/steeping/<drink>}).
 */
public final class SteepingRecipeProvider {
    public static final int DURATION = 200;

    private SteepingRecipeProvider() {
    }

    private static void tea(Consumer<FinishedRecipe> writer, ItemLike first, ItemLike second, ProductSpec drink) {
        ProcessingRecipeBuilder.of(StationRecipes.STEEPING_SERIALIZER.get(), DURATION)
                .input(first).input(second).base(ProcessingRecipeBuilder.water())
                .result(PreparationItems.product(drink))
                .save(writer, "processing/steeping/" + drink.id());
    }

    private static ItemLike powder(HerbSpec herb) {
        return PreparationItems.powder(herb);
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        tea(writer, powder(HerbCatalog.CHAMOMILE), powder(HerbCatalog.YARROW), RECOVERY_TEA);
        tea(writer, powder(HerbCatalog.MINT), Items.SUGAR, ENERGY_TEA);
        tea(writer, powder(HerbCatalog.LAVENDER), powder(HerbCatalog.VALERIAN), RESTFUL_INFUSION);
        tea(writer, powder(HerbCatalog.ROSEMARY), Items.BLAZE_POWDER, WARMTH_REMEDY);
        tea(writer, powder(HerbCatalog.MINT), Items.SNOWBALL, COOLING_REMEDY);
        tea(writer, powder(HerbCatalog.THYME), Items.IRON_NUGGET, MINING_TONIC);
        tea(writer, powder(HerbCatalog.MINT), Items.SUGAR_CANE, TRAVEL_TONIC);
    }
}
