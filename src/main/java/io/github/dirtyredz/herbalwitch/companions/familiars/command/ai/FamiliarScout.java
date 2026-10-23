package io.github.dirtyredz.herbalwitch.companions.familiars.command.ai;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarAlerts;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;

/** Ordinary scouting reports visible threats; it is not a magical sense or a harvesting task. */
public final class FamiliarScout implements FamiliarWork.Task {
    private final Mob mob;
    private long nextScan;
    public FamiliarScout(Mob mob) { this.mob = mob; }
    public void tick() { if (mob.level().getGameTime() >= nextScan) FamiliarWork.in((ServerLevel) mob.level()).scan(this); }
    @Override public boolean valid() {
        return !mob.isRemoved() && mob.isAlive() && !ControlEffects.held(mob)
                && FamiliarReconciliation.record(mob).filter(r -> r.command() == FamiliarCommand.SCOUT).isPresent();
    }
    @Override public void perform() {
        nextScan = mob.level().getGameTime() + 100;
        var record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) return;
        var owner = OnlinePlayers.find(record.owner()).orElse(null); if (owner == null || owner.level() != mob.level()) return;
        int results = 0;
        for (var entity : FamiliarQueries.entities((ServerLevel) mob.level(), mob.getBoundingBox().inflate(FamiliarServerConfig.current().senseRadius()))) {
            if (!(entity instanceof Mob candidate) || !(candidate instanceof Enemy) || candidate instanceof OwnableEntity pet && pet.getOwnerUUID() != null
                    || candidate.getType().is(ControlEffects.IMMUNE) || !mob.hasLineOfSight(candidate)
                    || !io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission.mayUse((ServerLevel) mob.level(), record.owner(), candidate.blockPosition()).allowed()
                    || !EntityActionPermission.check(EntityActionContext.Action.HARM, owner, candidate).allowed()) continue;
            FamiliarAlerts.warn(owner, mob, candidate); if (++results == FamiliarQueries.RESULTS) break;
        }
    }
    public void clear() { FamiliarWork.in((ServerLevel) mob.level()).remove(this); }
}
