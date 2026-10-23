package io.github.dirtyredz.herbalwitch.companions.familiars.command.ai;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.navigation.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeTarget;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** The seven command goals share one mutually exclusive selector and loaded-only motion driver. */
public final class FamiliarCommandGoal extends Goal {
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private final Mob mob;
    private final FamiliarMotion motion;
    private final FamiliarGuard guard;
    private final FamiliarScout scout;
    private FamiliarCommand command;
    private long anchorKey;
    private int waypoint;
    private boolean following;
    private Vec3 idleDestination;
    public FamiliarCommandGoal(Mob mob) {
        this.mob = mob; motion = new FamiliarMotion(mob); guard = new FamiliarGuard(mob); scout = new FamiliarScout(mob);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
    }
    @Override public boolean canUse() { return !mob.isNoAi() && FamiliarReconciliation.record(mob).isPresent(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { command = null; motion.reset(); }
    @Override public void tick() {
        var record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) { stop(); return; }
        var state = record.state();
        if (command != record.command() || anchorKey != state.getLong("anchor")) {
            motion.reset(); guard.clear(); scout.clear(); command = record.command(); anchorKey = state.getLong("anchor"); waypoint = 0; following = false; idleDestination = null;
        }
        // Control, hazards and retreat always precede command decisions. Never sets NoAI.
        if (ControlEffects.held(mob)) { motion.stop(); guard.clear(); mob.setTarget(null); return; }
        var owner = OnlinePlayers.find(record.owner()).orElse(null);
        if (command.mobile() && (owner == null || !owner.isAlive())) {
            FamiliarCommands.stopAssignments(mob.getServer(), record.owner(), "owner_offline"); stop(); return;
        }
        if (mob.isInLava() || mob.isOnFire() || mob.getAirSupply() < mob.getMaxAirSupply() / 2 || mob.isFreezing()) {
            guard.clear(); escape(mob.position(), owner); return;
        }
        if (mob.getHealth() < mob.getMaxHealth() * .25F) {
            var threat = guard.target(); guard.clear();
            var home = FamiliarHomeTarget.resolve(mob, record);
            Vec3 centre = home.position() != null && mob.position().distanceToSqr(home.position()) <= 64 * 64
                    ? home.position().add(0, -1, 0) : owner != null && owner.level() == mob.level()
                    && mob.distanceToSqr(owner) <= 64 * 64 ? owner.position() : null;
            if (centre != null) {
                var safe = FamiliarDestinations.near((ServerLevel) mob.level(), mob, centre, owner, 1);
                if (safe.isPresent() && FamiliarDestinations.routeLoaded(mob, safe.get())) {
                    hold(safe.get(), 1.2); motion.status("retreating"); return;
                }
            }
            Vec3 refuge = threat == null ? mob.position() : mob.position().add(mob.position().subtract(threat.position()).normalize().scale(4));
            escape(refuge, owner); motion.status("retreating"); return;
        }
        Vec3 anchor = Vec3.atBottomCenterOf(BlockPos.of(anchorKey));
        double speed = FamiliarCatalog.find(record.species()).orElseThrow().speed();
        if (command != FamiliarCommand.GUARD_WITCH && command != FamiliarCommand.PROTECT_HOME) guard.clear();
        switch (command) {
            case FOLLOW -> follow(owner, speed);
            case STAY -> { if (!sameAnchor(state.getString("anchorDimension"))) break; hold(anchor, speed); }
            case PASSIVE -> { mob.setTarget(null); if (sameAnchor(state.getString("anchorDimension"))) wander(anchor, 4, speed, owner); }
            case SCOUT -> {
                mob.setTarget(null);
                if (owner.level() != mob.level()) { motion.stop(); motion.status("other_dimension"); break; }
                scout.tick();
                if (sameAnchor(state.getString("anchorDimension"))) wander(anchor, Math.min(24, FamiliarServerConfig.current().senseRadius()), speed, owner);
            }
            case GUARD_WITCH -> {
                if (owner.level() != mob.level()) { motion.stop(); guard.clear(); motion.status("other_dimension"); break; }
                guard.tick(owner.position());
                if (!defend(record, speed, owner)) follow(owner, speed);
            }
            case PROTECT_HOME -> {
                if (owner == null && !FamiliarOfflineGuards.allowed(mob, record)) { motion.stop(); guard.clear(); motion.status("offline_limit"); break; }
                var home = FamiliarHomeTarget.resolve(mob, record);
                if (home.position() == null) { motion.stop(); guard.clear(); motion.status(home.reason()); break; }
                if (!io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission.mayUse((ServerLevel) mob.level(), record.owner(), BlockPos.containing(home.position())).allowed()) {
                    motion.stop(); guard.clear(); motion.status("permission"); break;
                }
                guard.tick(home.position());
                if (!defend(record, speed, owner)) {
                    if (mob.position().distanceToSqr(home.position()) > 16) hold(home.position(), speed);
                    else wander(home.position(), 4, speed, owner);
                }
            }
            case RETURN_HOME -> {
                var home = FamiliarHomeTarget.resolve(mob, record);
                if (home.position() == null) { motion.stop(); motion.status(home.reason()); break; }
                if (mob.position().distanceToSqr(home.position()) <= 4) { motion.stop(); FamiliarCommands.stay(mob, ""); }
                else {
                    // Ground paths cannot terminate in the air above a partial bed or nest.
                    // Arrive on checked floor beside the home, inside the same rest radius.
                    var arrival = FamiliarDestinations.near((ServerLevel) mob.level(), mob, home.position().add(0, -1, 0), owner, 1);
                    if (arrival.isPresent()) motion.move(arrival.get(), speed);
                    else { motion.stop(); motion.status("blocked"); }
                }
            }
        }
    }
    private boolean sameAnchor(String dimension) {
        if (dimension.equals(mob.level().dimension().location().toString())) return true;
        motion.stop(); motion.status("other_dimension"); return false;
    }
    private void follow(ServerPlayer owner, double speed) {
        if (owner == null || owner.level() != mob.level()) { motion.stop(); motion.status("other_dimension"); return; }
        double distance = mob.distanceToSqr(owner);
        if (distance <= 9) { following = false; motion.stop(); return; }
        if (distance > 36) following = true;
        if (!following) { motion.stop(); return; }
        if (distance > 144 && !mob.isLeashed() && !mob.isPassenger() && !mob.isVehicle()
                && mob.level().hasChunkAt(mob.blockPosition()) && EntityActionPermission.check(EntityActionContext.Action.ANIMAL_CARE, owner, mob).allowed()) {
            var destination = FamiliarDestinations.near(owner.serverLevel(), mob, owner.position(), owner, 3);
            if (destination.isPresent()) { var point = destination.get(); motion.reset(); mob.teleportTo(point.x, point.y, point.z); mob.fallDistance = 0; following = false; return; }
        }
        mob.getLookControl().setLookAt(owner, 10, mob.getMaxHeadXRot()); motion.move(owner.position(), speed);
    }
    private void hold(Vec3 point, double speed) {
        mob.setTarget(null);
        if (mob.position().distanceToSqr(point) <= 1) motion.stop(); else motion.move(point, speed);
    }
    private boolean defend(FamiliarRecord record, double speed, ServerPlayer owner) {
        var target = guard.target(); if (target == null) { mob.setTarget(null); return false; }
        if (FamiliarCatalog.find(record.species()).orElseThrow().damage() > 0) {
            mob.setTarget(target); mob.getLookControl().setLookAt(target, 20, mob.getMaxHeadXRot());
            if (mob.distanceToSqr(target) <= 2.25) motion.stop(); else motion.move(target.position(), speed);
            guard.attack();
        } else {
            mob.setTarget(null);
            if (mob.distanceToSqr(target) < 9) escape(mob.position().add(mob.position().subtract(target.position()).normalize().scale(3)), owner);
            else motion.stop();
        }
        return true;
    }
    private void escape(Vec3 centre, ServerPlayer owner) {
        var safe = FamiliarDestinations.near((ServerLevel) mob.level(), mob, centre, owner, 3);
        if (safe.isPresent()) motion.move(safe.get(), 1.2); else { motion.stop(); motion.status("blocked"); }
    }
    private void wander(Vec3 anchor, int radius, double speed, ServerPlayer owner) {
        if (mob.position().distanceToSqr(anchor) > radius * radius + 4) { motion.move(anchor, speed); return; }
        if (idleDestination == null || mob.position().distanceToSqr(idleDestination) < 2.25) {
            idleDestination = null;
            for (int i = 0; i < 4; i++) {
                var direction = DIRECTIONS[waypoint++ % 4]; var candidate = anchor.add(direction[0] * radius, 0, direction[1] * radius);
                if (FamiliarDestinations.safe((ServerLevel) mob.level(), mob, candidate, owner)) { idleDestination = candidate; break; }
            }
        }
        if (idleDestination != null) motion.move(idleDestination, speed); else { motion.stop(); motion.status("blocked"); }
    }
    @Override public void stop() { motion.reset(); guard.clear(); scout.clear(); following = false; idleDestination = null; }
}
