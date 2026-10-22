package io.github.dirtyredz.herbalwitch.companions.animals.care.ai;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Priority zero, MOVE only: preempts panic briefly without deleting it or clearing attacker memory. */
public final class AnimalCalmGoal extends Goal {
    private final Mob mob;
    public AnimalCalmGoal(Mob mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE)); }
    public static boolean safe(Mob mob) {
        if (mob.getTarget() != null || AnimalCareAccess.danger(mob) || ControlEffects.held(mob)) return false;
        return AnimalCapability.of(mob).flatMap(AnimalCapability::state).filter(s -> s.calmRemaining() > 0)
                .flatMap(s -> s.calmSource()).flatMap(OnlinePlayers::find)
                .filter(p -> p.level() == mob.level() && EntityActionPermission.check(Action.ANIMAL_CARE, p, mob).allowed()).isPresent();
    }
    @Override public boolean canUse() { return AnimalServerConfig.current().calming() && safe(mob)
            && BaitInterestIndex.budget((ServerLevel) mob.level()).claim(mob.getUUID(), null, AnimalServerConfig.current()); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public void start() { mob.getNavigation().stop(); }
    @Override public void tick() { mob.getNavigation().stop(); }
    @Override public void stop() {
        BaitInterestIndex.budget((ServerLevel) mob.level()).release(mob.getUUID(), null);
        if (!mob.isRemoved()) AnimalTrustService.endCalm(mob);
    }
}
