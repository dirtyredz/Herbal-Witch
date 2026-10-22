package io.github.dirtyredz.herbalwitch.magic.ward;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardActivation;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** Reproduces the original direct-activation exception path that leaked an energy debit. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WardSpendGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_spend_exception")
    public static void aThrownChargeRestoresEnergyAndPractice(GameTestHelper helper) {
        floor(helper);
        BlockPos relative = new BlockPos(4, 1, 4), pos = helper.absolutePos(relative);
        var state = WardRegistration.STONE.get().defaultBlockState();
        helper.setBlock(relative, state);
        RuntimeException failure = new IllegalStateException("ward record assignment failed");
        WardStoneBlockEntity broken = new WardStoneBlockEntity(pos, state) {
            @Override
            public void setRecordId(long id) {
                super.setRecordId(id);
                throw failure;
            }
        };
        helper.getLevel().setBlockEntity(broken);
        ServerPlayer owner = witch(helper, "ward_spend_exception", new BlockPos(4, 1, 2));
        WitchState before = WitchStatusService.snapshot(owner).orElseThrow();
        try {
            try {
                WardActivation.activate(owner, helper.getLevel(), pos);
                check(false, "charge did not reach the throwing stone");
            } catch (RuntimeException thrown) {
                check(thrown == failure, "activation replaced the original exception");
            }
            check(before.sameAs(WitchStatusService.snapshot(owner).orElseThrow()), "failed charge leaked energy or practice");
        } finally {
            WardLifecycle.removed(helper.getLevel(), pos, broken);
            helper.getLevel().removeBlockEntity(pos);
        }
        helper.succeed();
    }
}
