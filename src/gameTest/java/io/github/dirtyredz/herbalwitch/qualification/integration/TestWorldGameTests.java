package io.github.dirtyredz.herbalwitch.qualification.integration;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Guards the world every other GameTest runs in (gradle/gametest-world.gradle): superflat with no
 * structures and no natural spawning, so a plot holds only what its test builds. Normal terrain put the plots in caves, where
 * falling gravel, flowing water and cave mobs broke unrelated tests at random.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = HerbalWitch.MOD_ID)
public final class TestWorldGameTests {
    /** Natural spawning off before any batch runs; tests spawn every mob they use. */
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void quiet(net.minecraftforge.event.server.ServerStartedEvent event) {
        event.getServer().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, event.getServer());
    }

    @GameTest(template = "empty", batch = "herbalwitch_test_world")
    public static void theTestWorldIsSuperflatWithoutStructuresOrNaturalSpawning(GameTestHelper helper) {
        var level = helper.getLevel();
        check(level.getChunkSource().getGenerator() instanceof FlatLevelSource, "GameTest world is not superflat: "
                + level.getChunkSource().getGenerator().getClass().getSimpleName() + " (server.properties not written?)");
        var server = level.getServer();
        check(!server.getWorldData().worldGenOptions().generateStructures(), "GameTest world generates structures");
        // The GameTest server keeps MinecraftServer's always-on spawn flags, so the game rule is what stops spawning.
        check(!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING), "GameTest world spawns mobs naturally ("
                + server.getClass().getSimpleName() + ")");
        helper.succeed();
    }
}
