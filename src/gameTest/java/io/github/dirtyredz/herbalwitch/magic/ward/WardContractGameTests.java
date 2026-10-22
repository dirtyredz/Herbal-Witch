package io.github.dirtyredz.herbalwitch.magic.ward;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardFocus;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardControls;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardActivation;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardIndex;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;

import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.energy;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.practice;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.setEnergy;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

/** Contracts captured before the ward responsibility split. Each charged test owns its batch. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WardContractGameTests {
    private static final BlockPos STONE = new BlockPos(4, 1, 4);

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_focus_contract")
    public static void unlitFocusGainsAndLosesItsRadius(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var pos = helper.absolutePos(STONE);
        helper.setBlock(STONE, WardRegistration.STONE.get());
        var owner = witch(helper, "ward_focus_contract", STONE.north());
        var rules = WitchRules.server().workings();
        // Foreign wards left in the shared index by other tests would make the wider focused box overlap.
        for (var stale : WardIndex.loaded().all()) WardIndex.loaded().remove(stale.id());
        check(!WardFocus.focused(level, pos), "unexpected initial focus");
        check(WardFocus.maxRadius(level, pos) == rules.wardRadius(), "base maximum changed");
        for (var focus : new net.minecraft.world.level.block.Block[]{StationBlocks.HEARTH.get(), StationBlocks.STOVE.get()}) {
            helper.setBlock(STONE.east(), focus.defaultBlockState());
            check(WardFocus.focused(level, pos), "an unlit functional focus was rejected");
            check(WardFocus.maxRadius(level, pos) == rules.wardFocusRadius(), "focused maximum changed");
        }
        check(WardActivation.activate(owner, level, pos).equals("activated"), "activation");
        // Activating re-states the stone; a bare multiblock focus cell drops on that shape update, so the focus returns after it.
        helper.setBlock(STONE.east(), StationBlocks.STOVE.get().defaultBlockState());
        check(WardFocus.focused(level, pos), "focus lost after activation");
        var record = WardLifecycle.record(level, pos).orElseThrow();
        String chosen = WardControls.radius(owner, level, pos, 1000);
        check(record.radius() == rules.wardFocusRadius(), "chosen focused radius: " + chosen + " radius=" + record.radius()
                + " index=" + WardIndex.loaded().all());
        helper.setBlock(STONE.east(), Blocks.AIR);
        WardLifecycle.loaded(level, pos, (WardStoneBlockEntity) level.getBlockEntity(pos));
        helper.runAfterDelay(1, () -> {
            check(WardIndex.loaded().entry(record.id()).radius() == rules.wardRadius(), "lost focus did not clamp effective radius");
            check(record.radius() == rules.wardFocusRadius(), "focus loss erased the chosen radius");
            WardControls.deactivate(owner, level, pos);
            check(WardActivation.activate(owner, level, pos).equals("activated"), "reactivation");
            check(record.radius() == rules.wardRadius(), "charging did not clamp chosen radius");
            WardControls.release(owner, level, pos);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_route_contract")
    public static void ritualActivationAddsNeitherDirectReachNorAnotherSpend(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var pos = helper.absolutePos(STONE);
        helper.setBlock(STONE, WardRegistration.STONE.get());
        var owner = witch(helper, "ward_route_contract", STONE.north());
        for (var stale : WardIndex.loaded().all()) WardIndex.loaded().remove(stale.id()); // other tests' wards must not overlap this one
        setEnergy(owner, 0);
        owner.setPos(pos.getX() + 100, pos.getY(), pos.getZ());
        int before = practice(owner);
        String direct = WardActivation.activate(owner, level, pos);
        check(direct.equals("too_far"), "reach must precede energy: " + direct);
        String ritual = WardActivation.activateByRitual(owner, level, pos);
        check(ritual.equals("activated"), "already-paid ritual checked reach/energy: " + ritual);
        check(energy(owner) == 0 && practice(owner) == before, "ritual charged/practiced twice");
        var stranger = player(helper, "ward_route_stranger", STONE.north());
        check(WardActivation.refusal(stranger, level, pos).orElseThrow().equals("already_charged"), "charged must precede owner/witch checks");
        WardControls.deactivate(owner, level, pos);
        check(WardActivation.refusal(stranger, level, pos).orElseThrow().equals("not_owner"), "owner must precede witch check");
        WardControls.release(owner, level, pos);
        check(WardActivation.refusal(stranger, level, pos).orElseThrow().equals("not_witch"), "unbound witch check");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_caps_contract")
    public static void ownerAndChunkCapsPrecedeOverlap(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var pos = helper.absolutePos(STONE);
        helper.setBlock(STONE, WardRegistration.STONE.get());
        var owner = witch(helper, "ward_caps_contract", STONE.north());
        var data = WardSavedData.get(level);
        var records = new ArrayList<WardRecord>();
        try {
            for (int i = 0; i < WitchRules.server().workings().wardsPerOwner(); i++) {
                var record = data.create(level, pos.above(100 + i), 1).orElseThrow();
                data.bind(record, owner.getUUID());
                records.add(record);
            }
            check(WardActivation.refusal(owner, level, pos).orElseThrow().equals("owner_limit"), "owner cap");
            records.forEach(r -> data.retire(r.id()));
            for (int i = 0; i < WardCatalog.CENTRES_PER_CHUNK; i++) {
                WardIndex.loaded().add(new WardIndex.Entry(-100 - i, level.dimension().location().toString(), pos,
                        1, 1, WardCatalog.Profile.BOTH, owner.getUUID()));
            }
            check(WardActivation.refusal(owner, level, pos).orElseThrow().equals("chunk_limit"), "chunk cap must precede overlap");
        } finally {
            records.forEach(r -> data.retire(r.id()));
            for (int i = 0; i < WardCatalog.CENTRES_PER_CHUNK; i++) WardIndex.loaded().remove(-100 - i);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_unload_contract")
    public static void unloadedChargeIsNotCaughtUpAndExpiryRemovesProtection(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var pos = helper.absolutePos(STONE);
        helper.setBlock(STONE, WardRegistration.STONE.get());
        var owner = witch(helper, "ward_unload_contract", STONE.north());
        check(WardActivation.activate(owner, level, pos).equals("activated"), "activation");
        var record = WardLifecycle.record(level, pos).orElseThrow();
        var state = level.getBlockState(pos);
        int charge = record.charge();
        WardLifecycle.unloaded(level, pos, record.id());
        level.removeBlockEntity(pos);
        check(!WardIndex.loaded().contains(record.id()), "unloaded ward still protects");
        helper.runAfterDelay(20, () -> {
            check(record.charge() == charge, "unloaded charge ran down");
            var restored = new WardStoneBlockEntity(pos, state);
            restored.setRecordId(record.id());
            level.setBlockEntity(restored);
            WardLifecycle.loaded(level, pos, restored);
            helper.runAfterDelay(1, () -> {
                check(record.charge() >= charge - 1, "reload caught up unloaded ticks");
                check(WardIndex.loaded().contains(record.id()), "reload did not index");
                WardSavedData.get(level).charge(record, 1);
                WardService.tick(level, pos, restored);
                check(!record.charged() && !WardIndex.loaded().contains(record.id()), "expiry left protection");
                WardControls.release(owner, level, pos);
                helper.succeed();
            });
        });
    }
}
