package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchPackets;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WitchSpendGameTests {
    private static final BlockPos FEET = new BlockPos(4, 1, 4);

    @GameTest(template = TEMPLATE)
    public static void successfulSpendChargesPracticesAndUpdatesItsOwnerOnce(GameTestHelper helper) {
        ServerPlayer witch = witch(helper, "spend_success", FEET);
        WitchState before = WitchStatusService.snapshot(witch).orElseThrow();
        List<Object> sent = new ArrayList<>();
        WitchPackets.observe((to, packet) -> { if (to == witch) { sent.add(packet); } });
        try {
            check(WitchStatusService.spend(witch, 250, () -> true), "spend refused");
        } finally {
            WitchPackets.observe(null);
        }
        WitchState after = WitchStatusService.snapshot(witch).orElseThrow();
        check(after.energyUnits() == before.energyUnits() - 250 && after.practiceSpent() == before.practiceSpent() + 250,
                "incorrect charge/practice: " + after);
        check(sent.size() == 1 && sent.get(0) instanceof WitchSnapshot snapshot && snapshot.energy() == after.energyUnits()
                && snapshot.practice() == after.practiceSpent(), "success did not send one final private snapshot");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void refusalPreservesUnknownSavedDataAndNeverCallsTheParticipant(GameTestHelper helper) {
        ServerPlayer player = player(helper, "spend_unknown", FEET);
        WitchCapability cap = WitchCapability.of(player).orElseThrow();
        CompoundTag unknown = new CompoundTag();
        unknown.putInt(WitchCodec.SCHEMA_KEY, WitchCodec.SCHEMA + 1);
        unknown.putString("future", "preserve");
        cap.load(unknown);
        check(!WitchStatusService.spend(player, 0, () -> { throw new AssertionError("read-only callback"); }), "read-only spend passed");
        check(cap.save().equals(unknown), "spend rewrote unknown data");
        ServerPlayer plain = player(helper, "spend_plain", FEET.east());
        check(!WitchStatusService.spend(plain, 0, () -> { throw new AssertionError("non-witch callback"); }), "non-witch zero spend passed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void thrownParticipantRestoresDurableStateAndKeepsTransientDisturbance(GameTestHelper helper) {
        ServerPlayer witch = witch(helper, "spend_throw", FEET);
        WitchCapability cap = WitchCapability.of(witch).orElseThrow();
        cap.takeDisturbed();
        WitchState before = cap.state().copy();
        RuntimeException failure = new IllegalStateException("participant failed");
        int[] rollback = {0};
        try {
            WitchStatusService.spend(witch, 250, new WitchStatusService.Change() {
                public boolean apply() { throw failure; }
                public void rollback() { rollback[0]++; }
            });
            check(false, "exception was swallowed");
        } catch (RuntimeException thrown) {
            check(thrown == failure, "exception replaced");
        }
        check(cap.state().sameAs(before) && rollback[0] == 1, "failed transaction was not restored once");
        check(cap.takeDisturbed(), "rollback erased the original transient disturbance behavior");
        helper.succeed();
    }
}
