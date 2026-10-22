package io.github.dirtyredz.herbalwitch.companions.animals.care.ai;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Low priority interest; bounded scheduling and paths. Does not reward trust or traverse unloaded chunks. */
public final class HerbalBaitGoal extends Goal {
    final Mob mob;
    private ServerPlayer holder;
    private long nextQuery, nextPath;
    private int cursor;
    boolean queued;
    public HerbalBaitGoal(Mob mob) {
        this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        nextQuery = mob.level().getGameTime() + Math.floorMod(mob.getUUID().hashCode(), 20);
    }
    private boolean safe() { return mob.isAlive() && !mob.isRemoved() && mob.getTarget() == null && !ControlEffects.held(mob)
            && !AnimalCareAccess.danger(mob) && AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.calmRemaining() == 0).orElse(true); }
    private boolean accepts(ServerPlayer player) {
        return player.isAlive() && !player.isRemoved() && player.level() == mob.level() && mob.hasLineOfSight(player)
                && EntityActionPermission.check(Action.ANIMAL_CARE, player, mob).allowed();
    }
    @Override public boolean canUse() {
        if (!safe() || !AnimalServerConfig.current().attraction()) return false;
        if (holder != null && interested()) return BaitInterestIndex.budget((ServerLevel) mob.level())
                .claim(mob.getUUID(), holder.getUUID(), AnimalServerConfig.current());
        holder = null;
        if (!queued && mob.level().getGameTime() >= nextQuery && BaitInterestIndex.near(mob)) AnimalGoalInstaller.enqueue(this);
        return false;
    }
    void query() {
        queued = false;
        nextQuery = mob.level().getGameTime() + 20;
        if (!safe() || !AnimalServerConfig.current().attraction()) return;
        var profile = AnimalEligibility.profile(mob);
        if (profile.isPresent()) holder = BaitInterestIndex.find(mob, profile.get().bait(), cursor++, this::accepts);
    }
    private boolean interested() {
        var profile = AnimalEligibility.profile(mob);
        int radius = AnimalServerConfig.current().radius();
        return holder != null && profile.isPresent() && accepts(holder) && mob.distanceToSqr(holder) <= radius * radius
                && BaitInterestIndex.holding(holder, profile.get().bait());
    }
    @Override public boolean canContinueToUse() { return safe() && AnimalServerConfig.current().attraction() && interested(); }
    @Override public void start() { nextPath = 0; }
    @Override public void tick() {
        if (!interested()) return;
        mob.getLookControl().setLookAt(holder, 10, mob.getMaxHeadXRot());
        if (mob.distanceToSqr(holder) < 2.25) { mob.getNavigation().stop(); return; }
        long now = mob.level().getGameTime();
        if (now < nextPath || !loadedRoute()) return;
        if (BaitInterestIndex.budget((ServerLevel) mob.level()).path(AnimalServerConfig.current())) {
            nextPath = now + 20;
            mob.getNavigation().moveTo(holder, 1.0);
        }
    }
    private boolean loadedRoute() {
        var a = mob.blockPosition(); var b = holder.blockPosition();
        // The entire bounded navigation neighborhood is loaded, including the animal's width.
        int margin = (int) Math.ceil(mob.getBbWidth()) + 2;
        for (int x = Math.min(a.getX(), b.getX()) - margin; x <= Math.max(a.getX(), b.getX()) + margin; x += 1)
            for (int z = Math.min(a.getZ(), b.getZ()) - margin; z <= Math.max(a.getZ(), b.getZ()) + margin; z += 1)
                if (!mob.level().hasChunkAt(new net.minecraft.core.BlockPos(x, a.getY(), z))) return false;
        return true;
    }
    @Override public void stop() {
        if (holder != null) BaitInterestIndex.budget((ServerLevel) mob.level()).release(mob.getUUID(), holder.getUUID());
        holder = null; mob.getNavigation().stop();
    }
}
