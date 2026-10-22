package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.GardenBedConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.ORIGIN;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.SECTION;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.SECTION_X;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.SECTION_Z;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.as;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.run;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.stoneFloor;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Who may run {@code /herbalwitch showcase}: operators (level 2) as players only. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShowcaseCommandGameTests {
    /** The garden section's dry bed: section (1, 1, 1): a sign (x 0) beside it, the heading and an open row (2) before it. */
    private static final BlockPos DRY_BED = ORIGIN.offset(1, 1, 3);

    private ShowcaseCommandGameTests() {
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_an_operator_builds_a_section")
    public static void anOperatorBuildsASection(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op", GameType.SURVIVAL);
        int placed = run(helper, as(op, 2), "herbalwitch showcase garden");
        check(placed > 0, "operator command placed nothing");
        check(helper.getBlockState(DRY_BED).is(BotanyBlocks.herbBed()), "garden bed missing: " + helper.getBlockState(DRY_BED));
        check(helper.getBlockState(ORIGIN.offset(0, 1, 0)).is(Blocks.OAK_SIGN), "section heading sign missing");
        // the planted square's north-west bed (section 0, 1, 3 = box z 5) joined its neighbours as it was built
        BlockState square = helper.getBlockState(ORIGIN.offset(0, 1, 5));
        check(square.getValue(GardenBedConnections.EAST) && square.getValue(GardenBedConnections.SOUTH)
                && !square.getValue(GardenBedConnections.NORTH) && !square.getValue(GardenBedConnections.WEST), "showcase beds did not join: " + square);
        check(!helper.getBlockState(DRY_BED).getValue(GardenBedConnections.EAST), "the lone dry bed joined something");
        helper.succeed();
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_non_operators_are_refused_in_survival_and_creative")
    public static void nonOperatorsAreRefusedInSurvivalAndCreative(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        for (GameType mode : new GameType[] {GameType.SURVIVAL, GameType.CREATIVE}) {
            ServerPlayer player = standing(helper, "guest_" + mode.getName(), mode);
            check(run(helper, as(player, 0), "herbalwitch showcase garden") == 0, mode + " non-operator ran the showcase");
            check(run(helper, as(player, 1), "herbalwitch showcase") == 0, mode + " level-1 player ran the showcase");
        }
        check(helper.getBlockState(DRY_BED).isAir() && helper.getBlockState(ORIGIN).is(Blocks.STONE), "refused command changed the world");
        helper.succeed();
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_the_console_is_refused")
    public static void theConsoleIsRefused(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        int result = run(helper, helper.getLevel().getServer().createCommandSourceStack(), "herbalwitch showcase garden");
        check(result == 0, "the console ran the showcase");
        check(helper.getBlockState(DRY_BED).isAir(), "console command changed the world");
        helper.succeed();
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_an_unknown_section_is_refused")
    public static void anUnknownSectionIsRefused(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        ServerPlayer op = standing(helper, "op_unknown", GameType.SURVIVAL);
        check(run(helper, as(op, 2), "herbalwitch showcase nonsense") == 0, "unknown section accepted");
        helper.succeed();
    }
}
