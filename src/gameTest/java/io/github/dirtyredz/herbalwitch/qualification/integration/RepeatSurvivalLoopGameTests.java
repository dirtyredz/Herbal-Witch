package io.github.dirtyredz.herbalwitch.qualification.integration;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** TEMPORARY diagnostic: repeats the survival loop to reproduce an intermittent missing harvest drop. */
@GameTestHolder("herbalwitch")
public final class RepeatSurvivalLoopGameTests {
    @GameTestGenerator
    public static Collection<TestFunction> repeat() {
        List<TestFunction> out = new ArrayList<>();
        for (int i = 0; i < 24; i++) out.add(new TestFunction("herbalwitch_debug_loop_" + i, "debugloop" + i, "herbalwitch:empty", 400, 0, true,
                SurvivalLoopGameTests::forageToShopToNonWitchUse));
        return out;
    }
}
