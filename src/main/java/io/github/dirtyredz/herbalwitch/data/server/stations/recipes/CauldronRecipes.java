package io.github.dirtyredz.herbalwitch.data.server.stations.recipes;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * The cauldron, 600 heated ticks, witch recipes: up to three ingredients and one water bottle brew one
 * four-dose mixture, and the bottle comes back ({@code processing/cauldron/<group>/<product>}).
 * Herb names mean the herb's powder. Energy per batch is listed with each (before the server's
 * processing multiplier).
 */
public final class CauldronRecipes {
    public static final int DURATION = 600;

    private CauldronRecipes() {
    }

    private static ItemLike powder(HerbSpec herb) {
        return PreparationItems.powder(herb);
    }

    private static void brew(Consumer<FinishedRecipe> writer, ProductSpec product, int energy, ItemLike... inputs) {
        MixtureCatalog.Mixture mixture = MixtureCatalog.of(product).orElseThrow();
        ProcessingRecipeBuilder builder = ProcessingRecipeBuilder.of(StationRecipes.CAULDRON_SERIALIZER.get(), DURATION);
        for (ItemLike input : inputs) {
            builder.input(input);
        }
        String group = mixture.spec().category().substring("mixtures/".length());
        builder.base(ProcessingRecipeBuilder.water()).result(AdvancedPreparations.item(mixture.spec())).returns(Items.GLASS_BOTTLE)
                .witch(energy).save(writer, "processing/cauldron/" + group + "/" + product.id());
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        brew(writer, AdvancedProductCatalog.NIGHT_VISION, 5, powder(HerbCatalog.MUGWORT), Items.CARROT);
        brew(writer, AdvancedProductCatalog.WATER_BREATHING, 5, powder(HerbCatalog.MINT), Items.KELP);
        brew(writer, AdvancedProductCatalog.RESISTANCE, 10, powder(HerbCatalog.SAGE), Items.IRON_INGOT);
        brew(writer, AdvancedProductCatalog.HEALING, 10, powder(HerbCatalog.YARROW), powder(HerbCatalog.CHAMOMILE), Items.GLISTERING_MELON_SLICE);
        brew(writer, AdvancedProductCatalog.POISON, 10, powder(HerbCatalog.BELLADONNA), Items.SPIDER_EYE);
        brew(writer, AdvancedProductCatalog.CONCEALMENT, 10, powder(HerbCatalog.NIGHTSHADE), Items.INK_SAC);
        brew(writer, AdvancedProductCatalog.SLOWING, 10, powder(HerbCatalog.VALERIAN), Items.SLIME_BALL);
        brew(writer, AdvancedProductCatalog.BINDING, 15, powder(HerbCatalog.MANDRAKE), Items.STRING, Items.SLIME_BALL);
        brew(writer, AdvancedProductCatalog.WEAKENING, 10, powder(HerbCatalog.WOLFSBANE), Items.FERMENTED_SPIDER_EYE);
        brew(writer, AdvancedProductCatalog.SLEEP, 15, powder(HerbCatalog.VALERIAN), powder(HerbCatalog.LAVENDER), Items.RED_MUSHROOM);
        brew(writer, FamiliarPreparationCatalog.GUARDIAN, 10, powder(HerbCatalog.SAGE), powder(HerbCatalog.YARROW), Items.IRON_NUGGET);
        brew(writer, FamiliarPreparationCatalog.SWIFTPAW, 10, powder(HerbCatalog.MINT), Items.SUGAR, Items.RABBIT_HIDE);
        brew(writer, FamiliarPreparationCatalog.NIGHTWATCH, 10, powder(HerbCatalog.MUGWORT), Items.CARROT, Items.FEATHER);
        brew(writer, FamiliarPreparationCatalog.MOONVEIL, 10, powder(HerbCatalog.NIGHTSHADE), powder(HerbCatalog.LAVENDER), Items.INK_SAC);
        brew(writer, FamiliarPreparationCatalog.SPIRITBOND, 15, powder(HerbCatalog.MANDRAKE), Items.AMETHYST_SHARD,
                AdvancedPreparations.item(AdvancedProductCatalog.MOON_ESSENCE));
        brew(writer, FamiliarPreparationCatalog.HEARTHKEEPER, 10, powder(HerbCatalog.ROSEMARY), powder(HerbCatalog.SAGE), Items.CHARCOAL);
        brew(writer, FamiliarPreparationCatalog.SHADOWSTEP, 15, powder(HerbCatalog.NIGHTSHADE), Items.ENDER_PEARL,
                AdvancedPreparations.item(AdvancedProductCatalog.MOON_ESSENCE));
    }
}
