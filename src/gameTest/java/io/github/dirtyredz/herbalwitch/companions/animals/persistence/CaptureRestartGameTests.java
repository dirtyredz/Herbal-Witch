package io.github.dirtyredz.herbalwitch.companions.animals.persistence;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * M8 capture restart qualification (run by {@code captureRestartTest} on ANU only): real captures
 * and releases stopped at every ledger boundary in one JVM, reconciled by another. Inert unless
 * {@code herbalwitch.capture.restart.phase} is set.
 */
@GameTestHolder("herbalwitch_capture_restart")
public final class CaptureRestartGameTests {
    private CaptureRestartGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> captureRestart() {
        String phase = System.getProperty("herbalwitch.capture.restart.phase", "");
        if (phase.isEmpty()) return List.of();
        Path evidence = Path.of(System.getProperty("herbalwitch.capture.restart.evidence"));
        return switch (phase) {
            case "prepare" -> List.of(new TestFunction("capture_restart", "capture_restart.prepare", "herbalwitch_capture_restart:empty", 2400, 0, true,
                    helper -> new CaptureRestartPrepare(helper, evidence).start()));
            case "verify" -> List.of(new TestFunction("capture_restart", "capture_restart.verify", "herbalwitch_capture_restart:empty", 2400, 0, true,
                    helper -> new CaptureRestartVerify(helper, evidence).start()));
            default -> throw new IllegalArgumentException("Unknown capture restart phase " + phase);
        };
    }
}
