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
 * Refusals before any write (occupied plot, block entity, outside the world, unloaded), natural terrain
 * replaced by the showcase floor, and writes
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

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_writes_stay_inside_the_planned_box")
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

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_an_occupied_plot_is_refused_without_changes")
    public static void anOccupiedPlotIsRefusedWithoutChanges(GameTestHelper helper) throws Exception {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op_twice", GameType.SURVIVAL);
        // a neighbouring test's dropped item can drift into the plot and is (rightly) refused as an entity
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(
                helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(SECTION_X, 10, SECTION_Z))).inflate(1))
                .forEach(net.minecraft.world.entity.Entity::discard);
        if (run(helper, as(op, 2), "herbalwitch showcase garden") == 0) {
            var why = ShowcasePreflight.check(garden(), helper.getLevel(), op, helper.absolutePos(ORIGIN));
            check(false, "first build failed: " + why.map(c -> c.getString() + " " + c).orElse("preflight passed"));
        }
        BlockPos probe = ORIGIN.offset(5, 2, 2);
        BlockState before = helper.getBlockState(probe);
        helper.setBlock(ORIGIN.offset(1, 4, 1), Blocks.DIRT);
        check(run(helper, as(op, 2), "herbalwitch showcase garden") == 0, "a re-run over signs was accepted");
        check(helper.getBlockState(probe).equals(before) && helper.getBlockState(ORIGIN.offset(1, 4, 1)).is(Blocks.DIRT),
                "a refused re-run changed the world");
        helper.succeed();
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_a_chest_refuses_before_any_write")
    public static void aChestRefusesBeforeAnyWrite(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op_chest", GameType.SURVIVAL);
        helper.setBlock(ORIGIN.offset(3, 1, 3), Blocks.CHEST);
        helper.setBlock(ORIGIN.offset(1, 2, 1), Blocks.DIRT);
        check(run(helper, as(op, 2), "herbalwitch showcase garden") == 0, "built over a chest");
        check(helper.getBlockState(ORIGIN.offset(1, 2, 1)).is(Blocks.DIRT), "refusal cleared blocks");
        check(helper.getBlockState(ORIGIN.offset(3, 1, 3)).is(Blocks.CHEST), "refusal removed the chest");
        helper.succeed();
    }

    /** Natural terrain: no flat floor, holes, water, plants, leaves and logs in the box. */
    @GameTest(template = SECTION, batch = "herbalwitch_showcase_uneven_natural_terrain_is_replaced_by_the_showcase_floor")
    public static void unevenNaturalTerrainIsReplacedByTheShowcaseFloor(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op_rough", GameType.SURVIVAL);
        helper.setBlock(ORIGIN.offset(4, 0, 2), Blocks.AIR); // a hole in the floor layer
        helper.setBlock(ORIGIN.offset(2, 0, 2), Blocks.WATER); // water at floor level
        helper.setBlock(ORIGIN.offset(1, 0, 1), Blocks.GRASS_BLOCK);
        helper.setBlock(ORIGIN.offset(1, 1, 1), Blocks.GRASS);
        helper.setBlock(ORIGIN.offset(3, 1, 2), Blocks.OAK_LOG);
        helper.setBlock(ORIGIN.offset(3, 2, 2), Blocks.OAK_LEAVES);
        helper.setBlock(ORIGIN.offset(5, 1, 3), Blocks.WATER);
        helper.setBlock(ORIGIN.offset(0, 1, 0), Blocks.STONE);
        check(run(helper, as(op, 2), "herbalwitch showcase garden") > 0, "rough terrain was refused");
        for (int x : new int[] {0, 1, 2, 4, 5}) {
            check(helper.getBlockState(ORIGIN.offset(x, 0, 2)).is(Blocks.GRASS_BLOCK), "no grass floor at x " + x);
        }
        check(helper.getBlockState(ORIGIN.offset(3, 2, 2)).isAir(), "leaves left in the box");
        check(helper.getBlockState(ORIGIN.offset(5, 1, 3)).isAir(), "stray water left in the box");
        check(helper.getBlockState(ORIGIN.offset(1, 1, 1)).isAir(), "stray plant left in the box");
        helper.succeed();
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_outside_the_world_or_unloaded_is_refused")
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
