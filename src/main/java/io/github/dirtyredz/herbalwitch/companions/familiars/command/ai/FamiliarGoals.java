package io.github.dirtyredz.herbalwitch.companions.familiars.command.ai;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarOfflineGuards;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.*;

public final class FamiliarGoals {
    private static final Map<Mob, FamiliarCommandGoal> INSTALLED = new WeakHashMap<>();
    private FamiliarGoals() {}
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        var record = FamiliarReconciliation.record(mob).orElse(null);
        if (record == null) {
            FamiliarOfflineGuards.leave(mob);
            var old = INSTALLED.remove(mob); if (old != null) { mob.goalSelector.removeGoal(old); old.stop(); }
            return;
        }
        FamiliarOfflineGuards.observe(mob, record);
        if (!INSTALLED.containsKey(mob)) {
            mob.goalSelector.removeAllGoals(goal -> true); mob.targetSelector.removeAllGoals(goal -> true);
            var goal = new FamiliarCommandGoal(mob); INSTALLED.put(mob, goal); mob.goalSelector.addGoal(1, goal);
        }
    }
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide) {
            FamiliarOfflineGuards.leave(mob);
            var goal = INSTALLED.remove(mob); if (goal != null) goal.stop();
        }
    }
    public static void stopped(ServerStoppedEvent event) { INSTALLED.keySet().removeIf(mob -> mob.getServer() == event.getServer()); FamiliarOfflineGuards.stopped(event.getServer()); }
}
