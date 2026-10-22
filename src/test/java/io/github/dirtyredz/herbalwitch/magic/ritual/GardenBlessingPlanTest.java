package io.github.dirtyredz.herbalwitch.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.action.GardenBlessingPlan;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The garden blessing's target selection: a bounded 5 x 5 square at the bed's height, loaded, authorised, skipping enriched beds. */
class GardenBlessingPlanTest {
    private static final BlockPos BED = new BlockPos(10, 64, 10);

    @Test
    void theAreaIsTheFiveByFiveSquareAtTheBedsHeight() {
        List<BlockPos> area = GardenBlessingPlan.area(BED);
        assertEquals(GardenBlessingPlan.MAX, area.size());
        assertTrue(area.stream().allMatch(p -> p.getY() == 64 && Math.abs(p.getX() - 10) <= 2 && Math.abs(p.getZ() - 10) <= 2));
        assertEquals(25, Set.copyOf(area).size());
    }

    @Test
    void onlyEnrichableBedsChangeAndAtLeastOneMust() {
        Set<BlockPos> beds = Set.of(BED, BED.east(), BED.north(2).west(2));
        GardenBlessingPlan.Plan plan = GardenBlessingPlan.of(BED, p -> true, beds::contains, p -> true);
        assertTrue(plan.ok());
        assertEquals(beds, Set.copyOf(plan.changes()));
        assertEquals(GardenBlessingPlan.Outcome.NOTHING, GardenBlessingPlan.of(BED, p -> true, p -> false, p -> true).outcome(),
                "every bed already enriched (or none there): refused");
    }

    @Test
    void anUnloadedOrForbiddenPositionRefusesTheWholePlan() {
        Set<BlockPos> beds = Set.of(BED, BED.east());
        assertEquals(GardenBlessingPlan.Outcome.UNLOADED,
                GardenBlessingPlan.of(BED, p -> !p.equals(BED.south(2)), beds::contains, p -> true).outcome());
        assertEquals(GardenBlessingPlan.Outcome.NOT_ALLOWED,
                GardenBlessingPlan.of(BED, p -> true, beds::contains, p -> !p.equals(BED.east())).outcome());
        assertTrue(GardenBlessingPlan.of(BED, p -> true, beds::contains, p -> !p.equals(BED.west())).ok(),
                "a forbidden position that would not change does not matter");
    }
}
