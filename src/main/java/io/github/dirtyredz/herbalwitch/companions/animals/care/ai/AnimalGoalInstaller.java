package io.github.dirtyredz.herbalwitch.companions.animals.care.ai;

import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Retains every native goal and priority. A bounded FIFO gives candidate queries a fair turn. */
public final class AnimalGoalInstaller {
    private record Goals(long generation, HerbalBaitGoal bait, AnimalCalmGoal calm) {}
    private static final Map<Mob, Goals> INSTALLED = new WeakHashMap<>();
    private static final Map<ServerLevel, ArrayDeque<HerbalBaitGoal>> WAITING = new HashMap<>();
    private AnimalGoalInstaller() {}

    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel)) return;
        reconcile(mob);
        if (AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.calmRemaining() > 0).orElse(false)
                && !AnimalCalmGoal.safe(mob)) AnimalTrustService.endCalm(mob);
        if (AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.calmRemaining() == 0).orElse(false))
            BaitInterestIndex.budget((ServerLevel) mob.level()).release(mob.getUUID(), null);
    }
    public static void reconcile(Mob mob) {
        long generation = AnimalServerConfig.generation();
        var previous = INSTALLED.get(mob);
        if (previous != null && previous.generation == generation) return;
        if (previous != null) remove(mob, previous);
        var profile = AnimalEligibility.profile(mob); var rules = AnimalServerConfig.current();
        HerbalBaitGoal bait = null; AnimalCalmGoal calm = null;
        if (profile.isPresent() && profile.get().goalAi() && rules.relationships()) {
            if (rules.attraction() && mob.getType().is(AnimalTags.ATTRACTABLE)) { bait = new HerbalBaitGoal(mob); mob.goalSelector.addGoal(9, bait); }
            if (rules.calming() && mob.getType().is(AnimalTags.CALMABLE)) { calm = new AnimalCalmGoal(mob); mob.goalSelector.addGoal(0, calm); }
        }
        INSTALLED.put(mob, new Goals(generation, bait, calm));
    }
    public static boolean prepareCalm(Mob mob) {
        if (mob.isNoAi()) return false;
        reconcile(mob);
        return INSTALLED.get(mob).calm != null && BaitInterestIndex.budget((ServerLevel) mob.level())
                .claim(mob.getUUID(), null, AnimalServerConfig.current());
    }
    private static void remove(Mob mob, Goals goals) {
        if (goals.bait != null) { mob.goalSelector.removeGoal(goals.bait); goals.bait.stop(); }
        if (goals.calm != null) { mob.goalSelector.removeGoal(goals.calm); goals.calm.stop(); }
        var queue = WAITING.get((ServerLevel) mob.level());
        if (queue != null) queue.remove(goals.bait);
        BaitInterestIndex.budget((ServerLevel) mob.level()).releaseAll(mob.getUUID());
    }
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getLevel() instanceof ServerLevel) {
            var goals = INSTALLED.remove(mob);
            if (goals != null) remove(mob, goals);
        }
    }
    static void enqueue(HerbalBaitGoal goal) {
        var queue = WAITING.computeIfAbsent((ServerLevel) goal.mob.level(), ignored -> new ArrayDeque<>());
        if (queue.size() >= 512) return;
        goal.queued = true; queue.addLast(goal);
    }
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        var queue = WAITING.get(level); if (queue == null) return;
        var budget = BaitInterestIndex.budget(level);
        int queries = 0;
        while (!queue.isEmpty() && budget.checks() < AnimalServerConfig.current().checksPerTick() && queries++ < 32) {
            var goal = queue.removeFirst();
            if (!goal.mob.isRemoved()) goal.query(); else goal.queued = false;
        }
    }
    public static void reset() { INSTALLED.clear(); WAITING.clear(); BaitInterestIndex.reset(); }
}
