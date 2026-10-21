package io.github.dirtyredz.herbalwitch.brewing.effect;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;

/**
 * Narrow pest avoidance: a mob of the repelled-pests tag (silverfish and endermites) cannot acquire
 * a player under the effect as its target, and applying the effect clears such targets within
 * eight blocks. No global scans, no other mobs, no AI rewrites.
 */
public final class PestRepellentEvents {
    public static final double CLEAR_RADIUS = 8.0;

    private PestRepellentEvents() {
    }

    /** Forge bus listener. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (target instanceof Player player && repelled(event.getEntity()) && player.hasEffect(PreparationEffects.PEST_REPELLENT.get())) {
            event.setCanceled(true);
        }
    }

    static boolean repelled(LivingEntity entity) {
        return entity.getType().is(PreparationTags.REPELLED_PESTS);
    }

    /** Clears pests targeting {@code player} nearby; returns how many were cleared. */
    public static int clearTargets(Player player) {
        AABB box = player.getBoundingBox().inflate(CLEAR_RADIUS);
        int cleared = 0;
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, box, m -> repelled(m) && m.getTarget() == player)) {
            mob.setTarget(null);
            cleared++;
        }
        return cleared;
    }
}
