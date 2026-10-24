package io.github.dirtyredz.herbalwitch.qualification.performance;

import com.google.gson.GsonBuilder;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.gametest.GameTestHolder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Performance qualification (Milestone 12, {@code qualifyPerformance}, ANU only). {@code measure} runs
 * the frozen workloads and writes tick-time, population and latency evidence; {@code restart} is a
 * second JVM on the same world that times startup indexing of the 4,096 persisted bonds. Wall-clock
 * thresholds are judged by tools/qualification/performance/report.mjs, never inside ordinary tests.
 */
@GameTestHolder("herbalwitch_performance")
public final class PerformanceGameTests {
    private PerformanceGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> performance() {
        String phase = System.getProperty("herbalwitch.performance.phase", "");
        if (phase.isEmpty()) return List.of();
        Path evidence = Path.of(System.getProperty("herbalwitch.performance.evidence"));
        return switch (phase) {
            case "measure" -> List.of(new TestFunction("performance", "performance.measure", "herbalwitch_performance:empty", 60_000, 0, true,
                    helper -> {
                        quiet(helper);
                        new PerformanceRun(helper, evidence).start();
                    }));
            case "restart" -> List.of(new TestFunction("performance", "performance.restart", "herbalwitch_performance:empty", 6_000, 0, true,
                    helper -> restart(helper, evidence, helper.getLevel().getServer().getTickCount(), System.nanoTime())));
            default -> throw new IllegalArgumentException("Unknown performance phase " + phase);
        };
    }

    /** No natural spawning: every entity in a workload is one the workload placed. */
    private static void quiet(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        if (!(helper.getLevel().getChunkSource().getGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource))
            throw new IllegalStateException("The frozen protocol measures a superflat world (gradle/gametest-world.gradle)");
        helper.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
    }

    /** Waits for the persisted bond index to finish and records how long startup indexing took. */
    private static void restart(GameTestHelper helper, Path evidence, int firstTick, long firstNanos) {
        var server = helper.getLevel().getServer();
        var store = FamiliarStore.get(server);
        if (!store.ready()) {
            helper.runAfterDelay(1, () -> restart(helper, evidence, firstTick, firstNanos));
            return;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("process", ProcessHandle.current().pid());
        out.put("jvmUptimeAtFirstTestMs", ManagementFactory.getRuntimeMXBean().getUptime());
        out.put("indexTicks", server.getTickCount() - firstTick);
        out.put("indexMs", (System.nanoTime() - firstNanos) / 1_000_000);
        long t0 = System.nanoTime();
        java.util.UUID owner = java.util.UUID.nameUUIDFromBytes("perf_roster_owner".getBytes());
        var page = store.roster().page(owner, null, 32);
        out.put("firstPageMs", (System.nanoTime() - t0) / 1_000_000.0);
        out.put("firstPageSize", page.size());
        try {
            Files.createDirectories(evidence);
            Files.writeString(evidence.resolve("restart.json"), new GsonBuilder().setPrettyPrinting().create().toJson(out));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        helper.succeed();
    }
}
