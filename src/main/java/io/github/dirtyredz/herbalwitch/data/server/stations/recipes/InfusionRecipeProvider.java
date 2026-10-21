package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.FISHING_BLEND;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.GROWTH_INFUSION;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.HEALING_SALVE;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.LANTERN_OIL;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.PEST_REPELLENT;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.PLANT_OIL;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.RESIN;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.SOIL_TONIC;

/**
 * Infusion, 600 heated ticks: two ingredients, a base liquid and a package make one product, and
 * the base's glass bottle comes back ({@code processing/infusion/<product>}).
 */
public final class InfusionRecipeProvider {
    public static final int DURATION = 600;

    private InfusionRecipeProvider() {
    }

    private static void infuse(Consumer<FinishedRecipe> writer, ItemLike first, ItemLike second, Ingredient base,
                               ItemLike packaging, ProductSpec product) {
        ProcessingRecipeBuilder.of(StationRecipes.INFUSION_SERIALIZER.get(), DURATION)
                .input(first).input(second).base(base).packaging(packaging)
                .result(PreparationItems.product(product)).returns(Items.GLASS_BOTTLE)
                .save(writer, "processing/infusion/" + product.id());
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        Ingredient honey = Ingredient.of(Items.HONEY_BOTTLE);
        Ingredient oil = Ingredient.of(PreparationItems.product(PLANT_OIL));
        Ingredient water = ProcessingRecipeBuilder.water();
        infuse(writer, PreparationItems.powder(HerbCatalog.YARROW), PreparationItems.powder(HerbCatalog.CHAMOMILE), honey,
                Items.BOWL, HEALING_SALVE);
        infuse(writer, PreparationItems.powder(HerbCatalog.ROSEMARY), PreparationItems.product(RESIN), oil,
                Items.GLASS_BOTTLE, LANTERN_OIL);
        infuse(writer, PreparationItems.powder(HerbCatalog.NETTLE), Items.WHEAT_SEEDS, oil, Items.GLASS_BOTTLE, FISHING_BLEND);
        infuse(writer, PreparationItems.powder(HerbCatalog.NETTLE), Items.BONE_MEAL, water, Items.GLASS_BOTTLE, GROWTH_INFUSION);
        infuse(writer, PreparationItems.powder(HerbCatalog.LAVENDER), PreparationItems.powder(HerbCatalog.SAGE), oil,
                Items.GLASS_BOTTLE, PEST_REPELLENT);
        infuse(writer, Items.MOSS_BLOCK, Items.HANGING_ROOTS, water, Items.GLASS_BOTTLE, SOIL_TONIC);
    }
}
