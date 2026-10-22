package io.github.dirtyredz.herbalwitch.apothecary.brewing.application.advanced;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.HarmAttribution;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

/**
 * What each thrown preparation does to one creature it reaches (the projectile picks the creatures:
 * see {@code PreparationImpact}). Harmful bursts pass the entity permission boundary per creature;
 * concealment is a gift and reaches players and pets only. Effects refresh, never add up or rise;
 * binding and sleep follow the hard-control rules. A delayed poison on a player records who threw it.
 */
public final class ThrowableApplications {
    public static final int SECOND = DrinkApplication.SECOND;

    /** The thrown preparations. */
    public enum Burst {
        POISON(true, 8 * SECOND),
        CONCEALMENT(false, 8 * SECOND),
        SLOWING(true, 8 * SECOND),
        BINDING(true, 5 * SECOND),
        WEAKENING(true, 8 * SECOND),
        SLEEP(true, 5 * SECOND),
        BLINDING(true, 5 * SECOND);

        private final boolean harmful;
        private final int ticks;

        Burst(boolean harmful, int ticks) {
            this.harmful = harmful;
            this.ticks = ticks;
        }

        public boolean harmful() {
            return harmful;
        }

        public int ticks() {
            return ticks;
        }
    }

    private ThrowableApplications() {
    }

    /** Whether the burst may reach {@code target} at all (who it is for, and permission when harmful). */
    public static boolean eligible(Burst burst, Entity thrower, LivingEntity target) {
        if (!burst.harmful()) {
            return target instanceof Player || (target instanceof OwnableEntity pet && pet.getOwnerUUID() != null);
        }
        return EntityActionPermission.mayHarm(thrower, target);
    }

    /** Applies the burst to one eligible creature; returns whether anything changed. */
    public static boolean affect(Burst burst, Entity thrower, LivingEntity target) {
        if (!eligible(burst, thrower, target)) {
            return false;
        }
        return switch (burst) {
            case POISON -> {
                boolean added = target.addEffect(new MobEffectInstance(MobEffects.POISON, burst.ticks(), 0), thrower);
                if (added && target instanceof Player && thrower != null) {
                    HarmAttribution.record(target, thrower.getUUID(), burst.ticks());
                }
                yield added;
            }
            case SLOWING -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, burst.ticks(), 1), thrower);
            case WEAKENING -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, burst.ticks(), 0), thrower);
            case BINDING -> ControlEffects.hold(target, PreparationEffects.BINDING.get(), burst.ticks());
            case SLEEP -> {
                boolean held = ControlEffects.hold(target, PreparationEffects.SLEEP.get(), burst.ticks());
                if (held && target instanceof Mob mob) {
                    mob.setTarget(null);
                }
                yield held;
            }
            case BLINDING -> blind(burst, thrower, target);
            case CONCEALMENT -> {
                target.addEffect(new MobEffectInstance(PreparationEffects.CONCEALMENT.get(), burst.ticks(), 0));
                TargetFilters.clearTargets(target, target, m -> m.distanceTo(target) > TargetFilters.NEAR);
                yield true;
            }
        };
    }

    private static boolean blind(Burst burst, Entity thrower, LivingEntity target) {
        if (target instanceof Player) {
            return target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, burst.ticks(), 0), thrower);
        }
        boolean added = target.addEffect(new MobEffectInstance(PreparationEffects.DAZZLED.get(), burst.ticks(), 0), thrower);
        if (target instanceof Mob mob && mob.getTarget() != null && mob.distanceTo(mob.getTarget()) > TargetFilters.NEAR) {
            mob.setTarget(null);
        }
        return added;
    }
}
