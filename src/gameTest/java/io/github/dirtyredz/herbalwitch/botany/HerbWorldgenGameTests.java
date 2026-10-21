package io.github.dirtyredz.herbalwitch.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyTags;
import io.github.dirtyredz.herbalwitch.botany.worldgen.HerbWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * The worldgen data as the server loaded it: one configured and placed feature per herb and the
 * habitat biome tags; a patch placed on grass grows mature plants, a patch over stone places none.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HerbWorldgenGameTests {
    private HerbWorldgenGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void everyHerbHasItsFeaturesAndHabitat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<ConfiguredFeature<?, ?>> configured = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        Registry<PlacedFeature> placed = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        for (HerbSpec herb : HerbCatalog.ALL) {
            check(configured.containsKey(HerbWorldgen.configuredKey(herb).location()), herb.id() + " configured feature");
            check(placed.containsKey(HerbWorldgen.placedKey(herb).location()), herb.id() + " placed feature");
        }
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        check(biomes.getHolderOrThrow(Biomes.PLAINS).is(BotanyTags.habitat(HerbHabitat.TEMPERATE)), "plains not temperate");
        check(biomes.getHolderOrThrow(Biomes.SWAMP).is(BotanyTags.habitat(HerbHabitat.WET)), "swamp not wet");
        check(biomes.getHolderOrThrow(Biomes.DARK_FOREST).is(BotanyTags.habitat(HerbHabitat.WOODLAND)), "dark forest not woodland");
        check(!biomes.getHolderOrThrow(Biomes.DESERT).is(BotanyTags.habitat(HerbHabitat.DRY)), "desert must not be a herb habitat");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void patchesGrowMaturePlantsOnNaturalSoilOnly(GameTestHelper helper) {
        int onGrass = patch(helper, HerbCatalog.VALERIAN, Blocks.GRASS_BLOCK);
        check(onGrass > 0, "no valerian placed on grass");
        int onStone = patch(helper, HerbCatalog.VALERIAN, Blocks.STONE);
        check(onStone == 0, onStone + " valerian placed on stone");
        helper.succeed();
    }

    /** Fills the floor with {@code ground}, places the herb's configured patch a few times, counts plants. */
    private static int patch(GameTestHelper helper, HerbSpec herb, Block ground) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(new BlockPos(x, 0, z), ground);
                helper.setBlock(new BlockPos(x, 1, z), Blocks.AIR);
            }
        }
        ConfiguredFeature<?, ?> feature = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(HerbWorldgen.configuredKey(herb));
        RandomSource random = RandomSource.create(42);
        for (int i = 0; i < 6; i++) {
            feature.place(level, level.getChunkSource().getGenerator(), random, helper.absolutePos(new BlockPos(4, 1, 4)));
        }
        int plants = 0;
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                var state = helper.getBlockState(new BlockPos(x, 1, z));
                if (state.is(BotanyBlocks.plant(herb))) {
                    check(state.getValue(HerbCropBlock.AGE) == HerbSpec.MAX_AGE, "wild plant not mature");
                    plants++;
                }
            }
        }
        return plants;
    }
}
