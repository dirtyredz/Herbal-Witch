package io.github.dirtyredz.herbalwitch.companions.familiars.ability.action;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.CombatEvents;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.preparation.FamiliarBuffRules;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.*;
import java.util.*;

/** Guard powers share strict target policy; distraction owns only its temporary artificial target. */
public final class FamiliarCombatPowers {
    private record Distraction(Mob familiar, UUID owner, long until) {}
    private static final Map<Mob, Distraction> DISTRACTIONS = new WeakHashMap<>();
    private FamiliarCombatPowers() {}
    public static boolean target(Mob familiar, Mob target, int range) {
        var record = FamiliarReconciliation.record(familiar).orElse(null);
        return record != null && target != null && target.level() == familiar.level() && familiar.hasLineOfSight(target)
                && familiar.distanceToSqr(target) <= range * range && EntityActionPermission.checkByPrincipal(EntityActionContext.Action.FAMILIAR_HARM,
                (ServerLevel) familiar.level(), record.owner(), target).allowed();
    }
    public static boolean repel(Mob familiar, Mob target) {
        var anchor = defendedAnchor(familiar, target);
        if (anchor == null || !target(familiar, target, 16) || !target.isPushable()) return false;
        var direction = target.position().subtract(anchor).multiply(1, 0, 1).normalize(); if (direction.lengthSqr() < .1) return false;
        return FamiliarAbilityService.perform(familiar, FamiliarPower.REPEL, () -> {
            if (!target(familiar, target, 16)) return false;
            var before = target.getDeltaMovement(); target.push(direction.x * .6, .1, direction.z * .6); target.hurtMarked = true;
            return !target.getDeltaMovement().equals(before);
        });
    }
    public static boolean distract(Mob familiar, Mob target) {
        if (!target(familiar, target, 32) || target.getTarget() == familiar) return false;
        return FamiliarAbilityService.perform(familiar, FamiliarPower.DISTRACT, () -> {
            if (!target(familiar, target, 32)) return false;
            target.setTarget(familiar); if (target.getTarget() != familiar) return false;
            var record = FamiliarReconciliation.record(familiar).orElseThrow();
            DISTRACTIONS.put(target, new Distraction(familiar, record.owner(), target.level().getGameTime() + 100)); return true;
        });
    }
    public static boolean intercept(Mob familiar, Mob threat) {
        var record = FamiliarReconciliation.record(familiar).orElse(null);
        if (record == null || defendedAnchor(familiar, threat) == null || !target(familiar, threat, 16) || record.state().getLong("interceptUntil") > FamiliarAbilityService.clock(record)) return false;
        return FamiliarAbilityService.perform(familiar, FamiliarPower.INTERCEPT, s -> s.putLong("interceptUntil", FamiliarAbilityService.clock(record) + 100),
                () -> target(familiar, threat, 16));
    }
    private static Vec3 defendedAnchor(Mob familiar, Mob threat) {
        var record = FamiliarReconciliation.record(familiar).orElse(null);
        if (record == null || threat == null || !FamiliarAbilityService.guarding(record)) return null;
        Vec3 anchor;
        if (record.command() == io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand.PROTECT_HOME)
            anchor = io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeTarget.inspect(familiar, record).position();
        else {
            var owner = io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers.find(record.owner()).orElse(null);
            if (owner == null || owner.level() != familiar.level() || !(threat.getTarget() == owner || threat.getTarget() == familiar
                    || owner.getLastHurtByMob() == threat && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100
                    || familiar.getLastHurtByMob() == threat && familiar.tickCount - familiar.getLastHurtByMobTimestamp() < 100)) return null;
            anchor = owner.position();
        }
        int radius = io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.current().guardRadius();
        return anchor != null && threat.position().distanceToSqr(anchor) <= radius * radius && familiar.position().distanceToSqr(anchor) <= radius * radius ? anchor : null;
    }
    public static boolean venom(Mob familiar, Mob victim) {
        var record = FamiliarReconciliation.record(familiar).orElse(null);
        if (record == null || !target(familiar, victim, 3) || FamiliarBuffRules.active(record.state(), "venomleaf") || record.state().contains("poisonCoating")) return false;
        return FamiliarAbilityService.perform(familiar, FamiliarPower.VENOM, () -> target(familiar, victim, 3)
                && CombatEvents.applyVenom(familiar, victim, record.owner(), EntityActionContext.Action.FAMILIAR_HARM));
    }
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob target) || target.level().isClientSide) return;
        var mark = DISTRACTIONS.get(target); if (mark == null) return;
        if (target.level().getGameTime() >= mark.until() || !mark.familiar().isAlive() || mark.familiar().isRemoved()
                || !target(mark.familiar(), target, 32) || target.getTarget() != mark.familiar()) {
            if (target.getTarget() == mark.familiar()) target.setTarget(null); DISTRACTIONS.remove(target);
        }
    }
    public static void damage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || event.getAmount() <= 0 || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        var record = FamiliarReconciliation.record(mob).orElse(null);
        if (record != null && FamiliarAbilityService.guarding(record) && record.state().getLong("interceptUntil") > FamiliarAbilityService.clock(record))
            event.setAmount(event.getAmount() * .8F);
    }
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event) { DISTRACTIONS.keySet().removeIf(mob -> mob.getServer() == event.getServer()); }
}
