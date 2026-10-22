package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmSpec;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * The charm workbench, 200 ticks, witch recipes: three ingredient stacks make one charm
 * ({@code processing/charm_work/<charm>}).
 */
public final class CharmRecipes {
    public static final int DURATION = 200;

    private CharmRecipes() {
    }

    private static ProcessingRecipeBuilder charm(CharmSpec charm, int energy) {
        return ProcessingRecipeBuilder.of(StationRecipes.CHARM_WORK_SERIALIZER.get(), DURATION).result(CharmItems.item(charm)).witch(energy);
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        charm(CharmCatalog.PROTECTIVE_SACHET, 8).input(PreparationItems.powder(HerbCatalog.SAGE))
                .input(PreparationItems.powder(HerbCatalog.LAVENDER)).input(Items.STRING, 2)
                .save(writer, "processing/charm_work/" + CharmCatalog.PROTECTIVE_SACHET.id());
        charm(CharmCatalog.UNDEAD_AMULET, 10).input(PreparationItems.powder(HerbCatalog.SAGE)).input(Items.GOLD_INGOT)
                .input(Items.ROTTEN_FLESH).save(writer, "processing/charm_work/" + CharmCatalog.UNDEAD_AMULET.id());
        charm(CharmCatalog.ARTHROPOD_AMULET, 10).input(PreparationItems.powder(HerbCatalog.LAVENDER)).input(Items.GOLD_INGOT)
                .input(Items.SPIDER_EYE).save(writer, "processing/charm_work/" + CharmCatalog.ARTHROPOD_AMULET.id());
    }
}
