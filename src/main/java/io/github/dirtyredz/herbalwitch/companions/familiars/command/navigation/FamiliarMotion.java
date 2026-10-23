package io.github.dirtyredz.herbalwitch.companions.familiars.command.navigation;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.FamiliarWork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** A queued path attempt, with a twenty-tick repath floor and three-failure stop. */
public final class FamiliarMotion implements FamiliarWork.Task {
    private final Mob mob;
    private Vec3 target, previous;
    private double speed, priorDistance = Double.MAX_VALUE;
    private long nextPath;
    private int failures;
    private String command;
    public FamiliarMotion(Mob mob) { this.mob = mob; }
    public void move(Vec3 destination, double speed) {
        if (previous != null && previous.distanceToSqr(destination) > 16) { failures = 0; priorDistance = Double.MAX_VALUE; }
        target = destination; this.speed = speed;
        command = FamiliarReconciliation.record(mob).map(r -> r.command().key()).orElse("");
        if (!valid() || failures >= 3) return;
        if (!FamiliarDestinations.routeLoaded(mob, destination)) { stop(); status("unloaded_route"); return; }
        var path = mob.getNavigation().getPath();
        if (path != null && !path.isDone() && !mob.level().hasChunkAt(path.getNextNodePos())) { stop(); status("unloaded_route"); return; }
        if (mob.level().getGameTime() >= nextPath) FamiliarWork.in((ServerLevel) mob.level()).path(this);
    }
    @Override public boolean valid() {
        return target != null && mob.isAlive() && !mob.isRemoved() && !mob.isNoAi() && !ControlEffects.held(mob)
                && FamiliarReconciliation.record(mob).filter(r -> r.command().key().equals(command)).isPresent();
    }
    @Override public void perform() {
        if (mob.level().getGameTime() < nextPath) return;
        nextPath = mob.level().getGameTime() + 20;
        if (!FamiliarDestinations.routeLoaded(mob, target)) { stop(); status("unloaded_route"); return; }
        double distance = mob.position().distanceToSqr(target);
        if (failures >= 3) { stop(); status("blocked"); return; }
        boolean stalled = previous != null && distance > 2.25 && priorDistance - distance < .05;
        previous = target; priorDistance = distance;
        var path = mob.getNavigation().createPath(BlockPos.containing(target), 0);
        if (path == null || !path.canReach() || !mob.getNavigation().moveTo(path, speed)) {
            failures++; mob.getNavigation().stop(); status(failures >= 3 ? "blocked" : "path_pending");
        } else if (stalled) {
            failures++; if (failures >= 3) { stop(); status("blocked"); }
        } else { failures = 0; status(""); }
    }
    public void status(String value) {
        FamiliarReconciliation.record(mob).filter(r -> !r.state().getString("status").equals(value)).ifPresent(r ->
                FamiliarStore.get(mob.getServer()).update(r.change(s -> s.putString("status", value)), false));
    }
    public void stop() {
        FamiliarWork.in((ServerLevel) mob.level()).remove(this); mob.getNavigation().stop(); target = null;
    }
    public void reset() { stop(); failures = 0; previous = null; priorDistance = Double.MAX_VALUE; }
}
