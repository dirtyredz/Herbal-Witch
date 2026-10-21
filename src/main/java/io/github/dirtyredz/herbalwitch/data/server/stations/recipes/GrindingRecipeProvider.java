package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.FERTILIZER;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.FLOWER_DUST;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.MUSHROOM_DUST;
import static io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog.PLANT_OIL;

/**
 * Grinding, 100 ticks, no heat: every bundle into its powder, seeds pressed into plant oil, garden
 * dusts, four herb dyes and bone meal. Generated recipes are exact (every filled slot is used), so
 * none is ambiguous with another.
 */
public final class GrindingRecipeProvider {
    public static final int DURATION = 100;

    private GrindingRecipeProvider() {
    }

    private static ProcessingRecipeBuilder grind() {
        return ProcessingRecipeBuilder.of(StationRecipes.GRINDING_SERIALIZER.get(), DURATION);
    }

    private static ItemLike powder(HerbSpec herb) {
        return PreparationItems.powder(herb);
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            grind().input(PreparationItems.bundle(herb)).result(powder(herb))
                    .save(writer, "processing/grinding/herbs/" + herb.habitat().directory() + "/" + herb.id());
        }
        grind().input(Items.WHEAT_SEEDS, 4).input(Items.GLASS_BOTTLE).result(PreparationItems.product(PLANT_OIL))
                .save(writer, "processing/grinding/plant_oil");
        grind().input(powder(HerbCatalog.NETTLE)).input(Items.BONE_MEAL).input(Items.MOSS_BLOCK)
                .result(PreparationItems.product(FERTILIZER), 4).save(writer, "processing/grinding/fertilizer");
        grind().input(powder(HerbCatalog.CHAMOMILE)).input(Items.DANDELION).input(Items.BONE_MEAL)
                .result(PreparationItems.product(FLOWER_DUST), 2).save(writer, "processing/grinding/flower_dust");
        grind().input(powder(HerbCatalog.MUGWORT)).input(Ingredient.of(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM), 1)
                .input(Items.BONE_MEAL).result(PreparationItems.product(MUSHROOM_DUST), 2)
                .save(writer, "processing/grinding/mushroom_dust");
        grind().input(powder(HerbCatalog.LAVENDER)).result(Items.PURPLE_DYE).save(writer, "processing/grinding/purple_dye");
        grind().input(powder(HerbCatalog.SAGE)).result(Items.GREEN_DYE).save(writer, "processing/grinding/green_dye");
        grind().input(powder(HerbCatalog.CHAMOMILE)).result(Items.YELLOW_DYE).save(writer, "processing/grinding/yellow_dye");
        grind().input(powder(HerbCatalog.NIGHTSHADE)).result(Items.BLACK_DYE).save(writer, "processing/grinding/black_dye");
        grind().input(Items.BONE).result(Items.BONE_MEAL, 3).save(writer, "processing/grinding/bone_meal");
    }
}
