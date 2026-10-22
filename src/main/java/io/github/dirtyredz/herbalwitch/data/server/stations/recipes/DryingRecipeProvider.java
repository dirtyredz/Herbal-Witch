package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

/** Drying: each fresh herb into its bundle, 1 200 ticks, no heat ({@code processing/drying/<group>/<herb>}). */
public final class DryingRecipeProvider {
    public static final int DURATION = 1_200;

    private DryingRecipeProvider() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            ProcessingRecipeBuilder.of(StationRecipes.DRYING_SERIALIZER.get(), DURATION)
                    .input(BotanyItems.fresh(herb))
                    .result(PreparationItems.bundle(herb))
                    .save(writer, "processing/drying/" + herb.habitat().directory() + "/" + herb.id());
        }
    }
}
