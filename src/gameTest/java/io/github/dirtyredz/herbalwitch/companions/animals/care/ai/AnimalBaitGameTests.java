package io.github.dirtyredz.herbalwitch.companions.animals.care.ai;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalBaitGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_bait_navigation")
    public static void baitRequestsRealNavigationWithoutTrustAndStopsWhenHolderLosesBait(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_bait_nav"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN))); BaitInterestIndex.update(player);
        AnimalGoalInstaller.reconcile(cow);
        var goal = (HerbalBaitGoal) cow.goalSelector.getAvailableGoals().stream().filter(g -> g.getGoal() instanceof HerbalBaitGoal).findFirst().orElseThrow().getGoal();
        try {
            goal.query(); check(goal.canUse(), "indexed bait not selected"); goal.start(); goal.tick();
            check(!cow.getNavigation().isDone(), "real native navigation received no path");
            check(AnimalCapability.of(cow).orElseThrow().state().isEmpty(), "attraction granted trust/state");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); BaitInterestIndex.update(player);
            check(!goal.canContinueToUse(), "lost bait kept attraction"); goal.stop(); check(cow.getNavigation().isDone(), "stopped bait kept path");
        } finally { player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); BaitInterestIndex.update(player); goal.stop(); }
        h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_bait_fairness")
    public static void holderCursorReachesNinthCandidateWithoutExaminingMoreThanEightPerQuery(GameTestHelper h) {
        floor(h); var cow = AnimalFixtures.animal(h, EntityType.COW); var players = new java.util.ArrayList<net.minecraft.server.level.ServerPlayer>();
        try {
            for (int i = 0; i < 9; i++) {
                var p = AnimalFixtures.player(h, "m8_fair_" + i); p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(i == 8 ? Bait.GRAIN : Bait.FISH)));
                BaitInterestIndex.update(p); players.add(p);
            }
            var budget = BaitInterestIndex.budget(h.getLevel()); int before = budget.checks();
            check(BaitInterestIndex.find(cow, Bait.GRAIN, 0, p -> true) == null, "first query exceeded candidate bound");
            check(budget.checks() - before == 8, "wrong query count");
            check(BaitInterestIndex.find(cow, Bait.GRAIN, 0, p -> true) == players.get(8), "cursor starved ninth holder");
            check(budget.checks() - before == 9, "unbounded second query");
        } finally { for (var p : players) { p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); BaitInterestIndex.update(p); } }
        h.succeed();
    }
}
