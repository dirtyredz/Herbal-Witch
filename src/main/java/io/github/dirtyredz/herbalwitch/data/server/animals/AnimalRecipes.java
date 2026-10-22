package io.github.dirtyredz.herbalwitch.data.server.animals;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.ProcessingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.function.Consumer;

/** Ordinary mortar recipes; viewers and grimoire consume their live processing descriptions. */
public final class AnimalRecipes {
    private AnimalRecipes() {}
    public static void addTo(Consumer<FinishedRecipe> writer) {
        ProcessingRecipeBuilder.of(StationRecipes.CHARM_WORK_SERIALIZER.get(), 200).witch(10)
                .input(net.minecraft.world.item.Items.STRING, 2).input(net.minecraft.world.item.Items.LEATHER).input(net.minecraft.world.item.Items.AMETHYST_SHARD)
                .result(AnimalItems.CHARM.get()).save(writer, "processing/animals/charm_work/capture_charm");
        for (Bait bait : Bait.values()) ProcessingRecipeBuilder.of(StationRecipes.GRINDING_SERIALIZER.get(), 100)
                .input(Ingredient.of(AnimalTags.food(bait)), 2).input(PreparationItems.powder(HerbCatalog.VALERIAN))
                .result(AnimalItems.bait(bait), 4).save(writer, "processing/animals/grinding/" + bait.id());
        ProcessingRecipeBuilder.of(StationRecipes.GRINDING_SERIALIZER.get(), 100)
                .input(PreparationItems.powder(HerbCatalog.LAVENDER)).input(PreparationItems.powder(HerbCatalog.CHAMOMILE))
                .result(AnimalItems.CALMING.get(), 4).save(writer, "processing/animals/grinding/calming_powder");
    }
}
