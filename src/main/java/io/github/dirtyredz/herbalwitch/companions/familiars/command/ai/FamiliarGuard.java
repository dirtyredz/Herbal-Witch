package io.github.dirtyredz.herbalwitch.companions.familiars.command.ai;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

/** A bounded guard observation and ordinary species attack. Authorization is checked again at each use. */
public final class FamiliarGuard implements FamiliarWork.Task {
    private final Mob familiar;
    private Vec3 anchor;
    private Mob target;
    private long nextScan, nextAttack;
    public FamiliarGuard(Mob familiar) { this.familiar = familiar; }
    public void tick(Vec3 centre) {
        anchor = centre;
        if (target != null && !allowed(target)) { target = null; familiar.setTarget(null); }
        if (familiar.level().getGameTime() >= nextScan) FamiliarWork.in((ServerLevel) familiar.level()).scan(this);
    }
    @Override public boolean valid() {
        return anchor != null && !familiar.isRemoved() && familiar.isAlive() && !ControlEffects.held(familiar)
                && FamiliarReconciliation.record(familiar).filter(r -> r.command() == FamiliarCommand.GUARD_WITCH || r.command() == FamiliarCommand.PROTECT_HOME).isPresent();
    }
    @Override public void perform() {
        nextScan = familiar.level().getGameTime() + 20;
        if (!valid()) return;
        double radius = FamiliarServerConfig.current().guardRadius();
        for (var entity : FamiliarQueries.entities((ServerLevel) familiar.level(), new AABB(anchor, anchor).inflate(radius))) {
            if (entity instanceof Mob candidate && allowed(candidate)) { target = candidate; break; }
        }
        if (target != null) {
            FamiliarReconciliation.record(familiar).flatMap(r -> OnlinePlayers.find(r.owner())).ifPresent(owner ->
                    io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarAlerts.warn(owner, familiar, target));
        }
    }
    public boolean allowed(Mob candidate) {
        if (!valid() || candidate == familiar || !(candidate instanceof Enemy) || candidate instanceof OwnableEntity pet && pet.getOwnerUUID() != null
                || candidate.getType().is(ControlEffects.IMMUNE) || candidate.getType().is(EntityActionPermission.PROTECTED)
                || !candidate.isAlive() || candidate.isRemoved() || candidate.level() != familiar.level() || !familiar.hasLineOfSight(candidate)) return false;
        var record = FamiliarReconciliation.record(familiar).orElseThrow();
        var owner = OnlinePlayers.find(record.owner()).orElse(null);
        if (owner == null && record.command() != FamiliarCommand.PROTECT_HOME || owner != null && !owner.isAlive()) return false;
        if (owner == null && !io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarOfflineGuards.allowed(familiar, record)) return false;
        if (!io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission.mayUse((ServerLevel) familiar.level(), record.owner(), candidate.blockPosition()).allowed()
                || !io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission.mayUse((ServerLevel) familiar.level(), record.owner(), familiar.blockPosition()).allowed()
                || !EntityActionPermission.checkHarmByPrincipal((ServerLevel) familiar.level(), record.owner(), candidate).allowed()) return false;
        int radius = FamiliarServerConfig.current().guardRadius();
        if (candidate.position().distanceToSqr(anchor) > radius * radius || familiar.position().distanceToSqr(anchor) > radius * radius) return false;
        return record.command() == FamiliarCommand.PROTECT_HOME || candidate.getTarget() == owner || candidate.getTarget() == familiar
                || owner.getLastHurtByMob() == candidate && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100
                || familiar.getLastHurtByMob() == candidate && familiar.tickCount - familiar.getLastHurtByMobTimestamp() < 100;
    }
    public Mob target() { return target != null && allowed(target) ? target : null; }
    public void attack() {
        var enemy = target(); if (enemy == null) return;
        var record = FamiliarReconciliation.record(familiar).orElseThrow();
        float damage = FamiliarCatalog.find(record.species()).orElseThrow().damage();
        if (damage <= 0 || familiar.level().getGameTime() < nextAttack) return;
        double reach = Math.max(1.5, familiar.getBbWidth() + enemy.getBbWidth());
        if (familiar.distanceToSqr(enemy) > reach * reach || !allowed(enemy)) return;
        nextAttack = familiar.level().getGameTime() + 20;
        familiar.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        enemy.hurt(familiar.damageSources().mobAttack(familiar), damage);
    }
    public void clear() { FamiliarWork.in((ServerLevel) familiar.level()).remove(this); anchor = null; target = null; familiar.setTarget(null); }
}
