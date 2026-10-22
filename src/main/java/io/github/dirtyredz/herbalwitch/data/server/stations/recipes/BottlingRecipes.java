package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * The bottling bench, 100 ticks, ordinary work (no witch, no energy, automation welcome): one whole
 * mixture and four glass bottles become four bottles of its preparation; a throwable mixture also
 * takes one gunpowder and becomes four vials. Each has a labelled variant taking one paper more,
 * whose results carry the generated label ({@code processing/bottling/<group>/<product>/{plain,labelled}}).
 */
public final class BottlingRecipes {
    public static final int DURATION = 100;

    private BottlingRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        for (MixtureCatalog.Mixture mixture : MixtureCatalog.ALL) {
            String group = mixture.spec().category().substring("mixtures/".length());
            String path = "processing/bottling/" + group + "/" + mixture.product().id() + "/";
            ItemStack doses = new ItemStack(AdvancedPreparations.item(mixture.product()), MixtureCatalog.DOSES);
            bottle(writer, mixture, doses, false, path + "plain");
            bottle(writer, mixture, PreparationMetadata.labelled(doses), true, path + "labelled");
        }
    }

    private static void bottle(Consumer<FinishedRecipe> writer, MixtureCatalog.Mixture mixture, ItemStack result, boolean labelled, String path) {
        ProcessingRecipeBuilder builder = ProcessingRecipeBuilder.of(StationRecipes.BOTTLING_SERIALIZER.get(), DURATION)
                .input(AdvancedPreparations.item(mixture.spec()));
        if (mixture.throwable()) {
            builder.input(Items.GUNPOWDER);
        }
        if (labelled) {
            builder.input(Items.PAPER);
        }
        builder.packaging(Items.GLASS_BOTTLE, MixtureCatalog.DOSES).result(result).save(writer, path);
    }
}
