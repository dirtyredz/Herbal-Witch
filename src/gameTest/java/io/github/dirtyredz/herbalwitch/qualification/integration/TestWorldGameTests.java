package io.github.dirtyredz.herbalwitch.qualification.integration;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Guards the world every other GameTest runs in (gradle/gametest-world.gradle): superflat with no
 * structures, so a plot holds only what its test builds. Normal terrain put the plots in caves, where
 * falling gravel, flowing water and cave mobs broke unrelated tests at random.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TestWorldGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_test_world")
    public static void theTestWorldIsSuperflatWithoutStructuresOrNaturalSpawning(GameTestHelper helper) {
        var level = helper.getLevel();
        check(level.getChunkSource().getGenerator() instanceof FlatLevelSource, "GameTest world is not superflat: "
                + level.getChunkSource().getGenerator().getClass().getSimpleName() + " (server.properties not written?)");
        var server = level.getServer();
        check(!server.getWorldData().worldGenOptions().generateStructures(), "GameTest world generates structures");
        check(!server.isSpawningMonsters() && !server.isSpawningAnimals(), "GameTest world spawns mobs naturally");
        helper.succeed();
    }
}
