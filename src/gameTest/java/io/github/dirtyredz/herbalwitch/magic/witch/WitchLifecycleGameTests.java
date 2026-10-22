package io.github.dirtyredz.herbalwitch.magic.witch;

import io.github.dirtyredz.herbalwitch.magic.witch.status.StatusFixtures;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchPackets;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchLifecycleEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * The witch capability on real server players: attachment and saving, cloning on death and
 * non-death replacement (status and energy copied once, never refilled), private snapshots to their
 * owner only with a new epoch after a replacement, and cooldown counters that advance only on online
 * ticks and survive cloning.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WitchLifecycleGameTests {
    private static final BlockPos FEET = new BlockPos(4, 1, 4);

    private WitchLifecycleGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void aFreshPlayerIsANonWitchAndAWitchSurvivesASaveRoundTrip(GameTestHelper helper) {
        ServerPlayer fresh = player(helper, "lc_fresh", FEET);
        WitchCapability cap = WitchCapability.of(fresh).orElseThrow();
        check(!cap.state().initiated() && cap.state().energyUnits() == 0 && cap.writable(), "fresh player: " + cap.state());
        ServerPlayer w = witch(helper, "lc_saved", FEET);
        StatusFixtures.debit(w, 2_500);
        CompoundTag saved = WitchCapability.of(w).orElseThrow().save();
        WitchCapability reloaded = new WitchCapability();
        reloaded.load(saved);
        check(reloaded.state().sameAs(WitchCapability.of(w).orElseThrow().state()), "round trip: " + reloaded.state());
        check(reloaded.state().energyUnits() == 7_500, "energy after the round trip: " + reloaded.state());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void deathAndReturnFromTheEndCopyStatusOnceWithoutRefill(GameTestHelper helper) {
        for (boolean died : new boolean[] {true, false}) {
            ServerPlayer old = witch(helper, "lc_old_" + died, FEET);
            StatusFixtures.debit(old, 4_000);
            StatusFixtures.practice(old, 6_000);
            StatusFixtures.tea(WitchCapability.of(old).orElseThrow().state(), 300);
            WitchState before = WitchCapability.of(old).orElseThrow().state().copy();
            ServerPlayer replacement = player(helper, "lc_new_" + died, FEET);
            WitchLifecycleEvents.clone(new PlayerEvent.Clone(replacement, old, died));
            WitchState after = WitchCapability.of(replacement).orElseThrow().state();
            check(after.sameAs(before), (died ? "death" : "end return") + " changed the state: " + before + " -> " + after);
            check(after.initiated() && after.energyUnits() == 6_000, "status kept and energy not refilled: " + after);
            StatusFixtures.energy(after, 1);
            check(before.energyUnits() == 6_000, "the copy is independent");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aDimensionChangeSendsANewEpochToItsOwnerOnly(GameTestHelper helper) {
        ServerPlayer w = witch(helper, "lc_traveller", FEET);
        ServerPlayer other = player(helper, "lc_bystander", FEET.east());
        WitchStatusService.flush(w);
        long epoch = WitchCapability.of(w).orElseThrow().epoch();
        List<Object[]> sent = new ArrayList<>();
        WitchPackets.observe((to, message) -> sent.add(new Object[] {to, message}));
        try {
            WitchLifecycleEvents.changedDimension(new PlayerEvent.PlayerChangedDimensionEvent(w, Level.OVERWORLD, Level.NETHER));
        } finally {
            WitchPackets.observe(null);
        }
        check(!sent.isEmpty(), "no snapshot after the dimension change");
        for (Object[] s : sent) {
            check(s[0] == w, "a snapshot went to " + ((ServerPlayer) s[0]).getGameProfile().getName());
            check(s[0] != other, "another player received witch status");
        }
        WitchSnapshot snapshot = (WitchSnapshot) sent.get(sent.size() - 1)[1];
        check(snapshot.epoch() > epoch && snapshot.initiated() && snapshot.energy() == 10_000, "snapshot " + snapshot);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void cooldownsAdvanceOnlyOnOnlineTicks(GameTestHelper helper) {
        ServerPlayer w = witch(helper, "lc_cooling", FEET);
        WitchState state = WitchCapability.of(w).orElseThrow().state();
        StatusFixtures.tea(state, 50);
        StatusFixtures.restful(state, 30);
        for (int i = 0; i < 20; i++) {
            EnergyEvents.tick(w);
        }
        check(state.teaCooldownTicks() == 30 && state.restfulTicks() == 10, "after 20 ticks: " + state);
        CompoundTag saved = WitchCapability.of(w).orElseThrow().save(); // a logout saves; no time passes offline
        WitchCapability back = new WitchCapability();
        back.load(saved);
        check(back.state().teaCooldownTicks() == 30 && back.state().restfulTicks() == 10, "the save kept the counters: " + back.state());
        helper.succeed();
    }
}
