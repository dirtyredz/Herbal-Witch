package io.github.dirtyredz.herbalwitch.botany.worldgen;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Wild herb patches: one configured and one placed feature per herb (ids grouped like the herb
 * ids), and one Forge biome modifier per group adding its herbs to the group's habitat biome tag.
 * Biome and soil only: weather and moon belong to live foraging, not chunk generation. No retrogen.
 */
public final class HerbWorldgen {
    /** Placement attempts per patch. */
    public static final int PATCH_TRIES = 8;
    /** Horizontal spread of a patch. */
    public static final int PATCH_SPREAD = 4;

    private HerbWorldgen() {
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(HerbSpec herb) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, id(herb.directory()));
    }

    public static ResourceKey<PlacedFeature> placedKey(HerbSpec herb) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id(herb.directory()));
    }

    public static ResourceKey<BiomeModifier> biomeModifierKey(HerbHabitat habitat) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, id("botany/" + habitat.directory()));
    }

    /** A patch of mature plants, each on natural soil in open air. */
    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            BlockStateProvider mature = BlockStateProvider.simple(BotanyBlocks.plant(herb).getStateForAge(HerbSpec.MAX_AGE));
            BlockPredicate spot = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE,
                    BlockPredicate.matchesTag(new Vec3i(0, -1, 0), BlockTags.DIRT));
            context.register(configuredKey(herb), new ConfiguredFeature<>(Feature.RANDOM_PATCH,
                    new RandomPatchConfiguration(PATCH_TRIES, PATCH_SPREAD, 1,
                            PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(mature), spot))));
        }
    }

    /** One patch attempt per {@link HerbHabitat#rarityChunks()} chunks, on the surface. */
    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        for (HerbSpec herb : HerbCatalog.ALL) {
            context.register(placedKey(herb), new PlacedFeature(configured.getOrThrow(configuredKey(herb)), List.of(
                    RarityFilter.onAverageOnceEvery(herb.habitat().rarityChunks()),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP,
                    BiomeFilter.biome())));
        }
    }

    public static void bootstrapBiomeModifiers(BootstapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        for (HerbHabitat habitat : HerbHabitat.values()) {
            List<HerbSpec> herbs = HerbCatalog.ALL.stream().filter(h -> h.habitat() == habitat).toList();
            context.register(biomeModifierKey(habitat), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(BotanyTags.habitat(habitat)),
                    HolderSet.direct(herbs.stream().map(h -> placed.getOrThrow(placedKey(h))).toList()),
                    GenerationStep.Decoration.VEGETAL_DECORATION));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
    }
}
