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
        for (int i = 0; i < 48; i++) out.add(new TestFunction("herbalwitch_debug_loop_" + i, "debugloop" + i, "herbalwitch:empty", 400, 0, true,
                SurvivalLoopGameTests::forageToShopToNonWitchUse));
        for (int i = 0; i < 24; i++) out.add(new TestFunction("herbalwitch_debug_card_" + i, "debugcard" + i, "herbalwitch:empty", 100, 0, true,
                io.github.dirtyredz.herbalwitch.companions.animals.AnimalInspectionGameTests::privateCardAndSessionValidationPreventReplayAndCloseWhenBookIsLost));
        for (int i = 0; i < 24; i++) out.add(new TestFunction("herbalwitch_debug_release_" + i, "debugrelease" + i, "herbalwitch:empty", 100, 0, true,
                io.github.dirtyredz.herbalwitch.companions.animals.AnimalCaptureGameTests::realActorCaptureReleaseAndCopiedTokenHaveOneAnimalEvenWithFullInventory));
        for (int i = 0; i < 24; i++) out.add(new TestFunction("herbalwitch_debug_load_" + i, "debugload" + i, "herbalwitch:empty", 400, 0, true,
                io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarLoadGameTests::paginatedRosterAndManyOwnersShareOneWorkBudget));
        return out;
    }
}
