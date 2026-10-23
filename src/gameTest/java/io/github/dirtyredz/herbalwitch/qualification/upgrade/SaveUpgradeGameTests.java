package io.github.dirtyredz.herbalwitch.qualification.upgrade;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.gametest.GameTestHolder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Save-upgrade qualification (Milestone 12). A historical world prepared by
 * {@code tools/qualification/save-upgrade/fixture.mjs} is opened by the candidate in three separate
 * dedicated JVMs: {@code upgrade} loads every listed chunk and player file and re-saves them through
 * the candidate's codecs; {@code reload} does the same again (the world must not change); {@code behave}
 * runs the first tick of every saved processing job and checks nothing is lost. The world files
 * themselves are compared by {@code compare.mjs}; these tests only open, touch and save them, and
 * record what the candidate observed. Inert unless {@code herbalwitch.upgrade.phase} is set.
 */
@GameTestHolder(SaveUpgradeGameTests.NAMESPACE)
public final class SaveUpgradeGameTests {
    static final String NAMESPACE = HerbalWitch.MOD_ID + "_save_upgrade";
    private static final Set<String> PHASES = Set.of("upgrade", "reload", "behave");

    private SaveUpgradeGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> saveUpgrade() {
        String phase = System.getProperty("herbalwitch.upgrade.phase", "");
        if (phase.isEmpty()) return List.of();
        if (!PHASES.contains(phase)) throw new IllegalArgumentException("Unknown save-upgrade phase " + phase);
        return List.of(new TestFunction("save_upgrade", "save_upgrade." + phase, NAMESPACE + ":empty", 2400, 0, true,
                helper -> run(helper, phase)));
    }

    private static void run(GameTestHelper helper, String phase) {
        Path fixture = Path.of(System.getProperty("herbalwitch.upgrade.fixture"));
        Path evidence = Path.of(System.getProperty("herbalwitch.upgrade.evidence"));
        var level = helper.getLevel();
        // No random ticks: herb growth, bed moisture and bed joining stay exactly as saved.
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());
        var chunks = UpgradeChunks.load(level, fixture);
        var report = new UpgradeReport(phase, chunks.size());
        if (phase.equals("behave")) {
            SavedJobs.firstTick(level, chunks, report);
        } else {
            BlockEntityTouch.touch(level, chunks, report);
            UpgradePlayers.reload(level.getServer(), report);
            chunks.forEach(chunk -> chunk.setUnsaved(true));
            level.getServer().saveEverything(true, true, true);
        }
        report.write(evidence);
        if (!report.failures().isEmpty()) helper.fail(phase + ": " + String.join("; ", report.failures()));
        helper.succeed();
    }
}
