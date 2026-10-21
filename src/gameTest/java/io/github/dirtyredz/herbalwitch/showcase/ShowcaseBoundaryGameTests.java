package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.ORIGIN;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.SECTION;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.SECTION_X;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.SECTION_Z;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.as;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.run;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.stoneFloor;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Refusals before any write (occupied plot, no floor, outside the world, unloaded) and writes
 * confined to the planned box.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShowcaseBoundaryGameTests {
    private ShowcaseBoundaryGameTests() {
    }

    private static ShowcasePlan garden() throws Exception {
        return ShowcasePlan.of(List.of(ShowcaseRegistry.byId("garden").orElseThrow()));
    }

    @GameTest(template = SECTION)
    public static void writesStayInsideThePlannedBox(GameTestHelper helper) throws Exception {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ShowcasePlan plan = garden();
        // Markers hugging the box on every side, including above it.
        List<BlockPos> markers = List.of(ORIGIN.offset(-1, 1, 0), ORIGIN.offset(plan.width(), 1, 0),
                ORIGIN.offset(0, 1, plan.depth()), ORIGIN.offset(0, ShowcasePlan.HEIGHT, 0));
        markers.forEach(m -> helper.setBlock(m, Blocks.GLOWSTONE));
        helper.setBlock(ORIGIN.offset(1, 3, 1), Blocks.DIRT); // overhead clutter inside the box is cleared
        check(run(helper, as(standing(helper, "op_box", GameType.SURVIVAL), 2), "herbalwitch showcase garden") > 0, "build failed");
        markers.forEach(m -> check(helper.getBlockState(m).is(Blocks.GLOWSTONE), "write outside the box at " + m));
        check(helper.getBlockState(ORIGIN.offset(1, 3, 1)).isAir(), "overhead clutter not cleared");
        helper.succeed();
    }

    @GameTest(template = SECTION)
    public static void anOccupiedPlotIsRefusedWithoutChanges(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op_twice", GameType.SURVIVAL);
        check(run(helper, as(op, 2), "herbalwitch showcase garden") > 0, "first build failed");
        BlockPos probe = ORIGIN.offset(5, 2, 2);
        BlockState before = helper.getBlockState(probe);
        helper.setBlock(ORIGIN.offset(1, 4, 1), Blocks.DIRT);
        check(run(helper, as(op, 2), "herbalwitch showcase garden") == 0, "a re-run over signs was accepted");
        check(helper.getBlockState(probe).equals(before) && helper.getBlockState(ORIGIN.offset(1, 4, 1)).is(Blocks.DIRT),
                "a refused re-run changed the world");
        helper.succeed();
    }

    @GameTest(template = SECTION)
    public static void aChestOrMissingFloorRefusesBeforeAnyWrite(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op_chest", GameType.SURVIVAL);
        helper.setBlock(ORIGIN.offset(3, 1, 3), Blocks.CHEST);
        helper.setBlock(ORIGIN.offset(1, 2, 1), Blocks.DIRT);
        check(run(helper, as(op, 2), "herbalwitch showcase garden") == 0, "built over a chest");
        check(helper.getBlockState(ORIGIN.offset(1, 2, 1)).is(Blocks.DIRT), "refusal cleared blocks");
        helper.setBlock(ORIGIN.offset(3, 1, 3), Blocks.AIR);
        helper.setBlock(ORIGIN.offset(4, 0, 2), Blocks.AIR); // a hole in the floor
        check(run(helper, as(op, 2), "herbalwitch showcase garden") == 0, "built over a hole");
        check(helper.getBlockState(ORIGIN.offset(1, 2, 1)).is(Blocks.DIRT), "refusal cleared blocks");
        helper.succeed();
    }

    @GameTest(template = SECTION)
    public static void outsideTheWorldOrUnloadedIsRefused(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        ServerPlayer op = standing(helper, "op_far", GameType.SURVIVAL);
        ShowcasePlan plan = garden();
        BlockPos top = new BlockPos(helper.absolutePos(ORIGIN).getX(), level.getMaxBuildHeight() - 3, helper.absolutePos(ORIGIN).getZ());
        check(ShowcasePreflight.check(plan, level, op, top).isPresent(), "accepted a box above the build height");
        BlockPos far = helper.absolutePos(ORIGIN).offset(1_000_000, 0, 1_000_000);
        check(!level.isLoaded(far), "test assumption: far chunk unexpectedly loaded");
        check(ShowcasePreflight.check(plan, level, op, far).isPresent(), "accepted an unloaded box");
        check(!level.isLoaded(far), "the preflight loaded a chunk");
        helper.succeed();
    }
}
