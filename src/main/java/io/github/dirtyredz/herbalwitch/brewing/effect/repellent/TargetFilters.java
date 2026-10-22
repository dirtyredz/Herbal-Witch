package io.github.dirtyredz.herbalwitch.brewing.effect.repellent;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;

import java.util.function.Predicate;

/**
 * Target acquisition filtering, the only way these preparations change mob behaviour: a creature
 * simply cannot choose some targets while an effect lasts. Never AI rewrites, never persistent flags:
 * a sleeping creature chooses nobody; a dazzled one nobody farther than {@link #NEAR}; nobody chooses
 * a concealed creature from farther than {@link #NEAR}; a family member never chooses someone wearing
 * its repellent. Applying an effect also clears matching existing targets close by
 * ({@link #clearTargets}, a bounded box, never a world scan).
 */
public final class TargetFilters {
    /** Blocks within which a concealed creature can still be found, or a dazzled one can still see. */
    public static final double NEAR = 4.0;
    /** How far applying a repellent or concealment clears existing targets. */
    public static final double CLEAR_RADIUS = 8.0;
    /** At most this many creatures are cleared per application. */
    public static final int MAX_CLEARED = 32;

    private TargetFilters() {
    }

    /** Forge bus. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity seeker = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (target == null) {
            return;
        }
        if (refuses(seeker, target)) {
            event.setCanceled(true);
        }
    }

    /** Whether {@code seeker} may not choose {@code target} now. */
    public static boolean refuses(LivingEntity seeker, LivingEntity target) {
        if (seeker.hasEffect(PreparationEffects.SLEEP.get())) {
            return true;
        }
        double distance = seeker.distanceTo(target);
        if (seeker.hasEffect(PreparationEffects.DAZZLED.get()) && distance > NEAR) {
            return true;
        }
        if (target.hasEffect(PreparationEffects.CONCEALMENT.get()) && distance > NEAR) {
            return true;
        }
        for (RepellentProfiles profile : RepellentProfiles.values()) {
            if (profile.matches(seeker) && target.hasEffect(profile.repellent())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears the targets of nearby mobs (within {@link #CLEAR_RADIUS} of {@code around}) whose current
     * target is {@code target} and who match {@code which}; returns how many. Bounded.
     */
    public static int clearTargets(LivingEntity around, LivingEntity target, Predicate<Mob> which) {
        AABB box = around.getBoundingBox().inflate(CLEAR_RADIUS);
        int cleared = 0;
        for (Mob mob : around.level().getEntitiesOfClass(Mob.class, box, m -> m.getTarget() == target && which.test(m))) {
            if (cleared >= MAX_CLEARED) {
                break;
            }
            mob.setTarget(null);
            cleared++;
        }
        return cleared;
    }
}
