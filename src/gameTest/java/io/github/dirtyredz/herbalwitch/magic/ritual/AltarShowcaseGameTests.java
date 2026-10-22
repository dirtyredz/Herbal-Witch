package io.github.dirtyredz.herbalwitch.magic.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarAccess;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingRecord;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
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

/**
 * The altar showcase as the real command builds it: a complete idle altar and a stocked supply
 * barrel, with nobody initiated, no energy granted, the founding record untouched and no ritual
 * running.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AltarShowcaseGameTests {
    private AltarShowcaseGameTests() {
    }

    @GameTest(template = SECTION, batch = "herbalwitch_showcase_the_altar_section_builds_a_working_idle_altar_and_supplies")
    public static void theAltarSectionBuildsAWorkingIdleAltarAndSupplies(GameTestHelper helper) {
        stoneFloor(helper, SECTION_X, SECTION_Z);
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO),
                helper.absolutePos(new BlockPos(SECTION_X, 10, SECTION_Z))).inflate(1)).forEach(ItemEntity::discard);
        ServerPlayer op = standing(helper, "op_altar", GameType.SURVIVAL);
        FoundingRecord.State before = FoundingService.data(helper.getLevel().getServer()).record().state();
        check(run(helper, as(op, 2), "herbalwitch showcase altar") > 0, "the altar section was not built");
        // the section's content starts one row behind its heading: the origin stands at section (1, 1, 1)
        BlockPos origin = helper.absolutePos(ORIGIN.offset(1, 1, 2));
        check(helper.getLevel().getBlockState(origin).getBlock() instanceof AltarBlock && AltarAccess.complete(helper.getLevel(), origin),
                "no complete altar at " + origin + ": " + helper.getLevel().getBlockState(origin));
        check(!helper.getLevel().getBlockState(origin).getValue(AltarBlock.ACTIVE), "the showcase altar glows");
        check(!AltarSessionService.busy(helper.getLevel(), origin), "the showcase started a ritual");
        if (helper.getLevel().getBlockEntity(helper.absolutePos(ORIGIN.offset(3, 1, 2))) instanceof Container barrel) {
            int stacks = 0;
            for (int i = 0; i < barrel.getContainerSize(); i++) {
                stacks += barrel.getItem(i).isEmpty() ? 0 : 1;
            }
            check(stacks == AltarShowcaseSection.supplies().size(), "supply barrel holds " + stacks + " stacks");
        } else {
            check(false, "no supply barrel");
        }
        check(!WitchStatusService.isWitch(op) && WitchStatusService.energyUnits(op) == 0, "building the showcase made a witch");
        check(FoundingService.data(helper.getLevel().getServer()).record().state() == before, "building the showcase touched founding");
        helper.succeed();
    }
}
