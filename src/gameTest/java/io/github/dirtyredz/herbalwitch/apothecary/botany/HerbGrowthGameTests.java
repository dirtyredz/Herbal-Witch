package io.github.dirtyredz.herbalwitch.apothecary.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.plantOnFarmland;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** Random growth advances one age at a time; bone meal one or two, capped at maturity. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HerbGrowthGameTests {
    private HerbGrowthGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void randomGrowthAdvancesOneAgeAtATime(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos plant = plantOnFarmland(helper, HerbCatalog.CHAMOMILE, new BlockPos(4, 1, 4), 0);
        BlockPos abs = helper.absolutePos(plant);
        // Growth needs crop light; the test area has little sky light, so a lamp stands beside the plant.
        helper.setBlock(plant.east(), Blocks.GLOWSTONE);
        helper.startSequence()
                .thenWaitUntil(() -> check(level.getRawBrightness(abs, 0) >= 9, "no light yet"))
                .thenExecute(() -> {
                    int age = 0;
                    for (int tick = 0; tick < 4000 && age < HerbSpec.MAX_AGE; tick++) {
                        BlockState state = level.getBlockState(abs);
                        state.randomTick(level, abs, level.random);
                        int next = level.getBlockState(abs).getValue(HerbCropBlock.AGE);
                        check(next == age || next == age + 1, "grew from " + age + " to " + next + " in one tick");
                        age = next;
                    }
                    check(age == HerbSpec.MAX_AGE, "never matured, stuck at " + age);
                    level.getBlockState(abs).randomTick(level, abs, level.random);
                    check(level.getBlockState(abs).getValue(HerbCropBlock.AGE) == HerbSpec.MAX_AGE, "grew past maturity");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void boneMealAdvancesOneOrTwoAgesCapped(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "gardener", GameType.SURVIVAL, Direction.NORTH);
        for (int trial = 0; trial < 6; trial++) {
            BlockPos plant = plantOnFarmland(helper, HerbCatalog.ALL.get(trial), new BlockPos(1 + trial, 1, 2), 0);
            ItemStack meal = new ItemStack(Items.BONE_MEAL);
            useItemOn(helper, player, meal, plant, Direction.UP);
            int age = helper.getBlockState(plant).getValue(HerbCropBlock.AGE);
            check(age == 1 || age == 2, "bone meal moved a sprout to age " + age);
            check(meal.isEmpty(), "bone meal not consumed");
        }
        BlockPos nearly = plantOnFarmland(helper, HerbCatalog.SAGE, new BlockPos(4, 1, 6), 2);
        useItemOn(helper, player, new ItemStack(Items.BONE_MEAL), nearly, Direction.UP);
        check(helper.getBlockState(nearly).getValue(HerbCropBlock.AGE) == HerbSpec.MAX_AGE, "not capped at maturity");
        check(!BotanyBlocks.plant(HerbCatalog.SAGE).isValidBonemealTarget(helper.getLevel(), helper.absolutePos(nearly),
                helper.getBlockState(nearly), false), "mature plants still accept bone meal");
        helper.succeed();
    }
}
