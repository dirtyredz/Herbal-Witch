package io.github.dirtyredz.herbalwitch.brewing.effect.control;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * A hard-control effect (binding, sleep): voluntary horizontal movement stops while it lasts. Walking
 * and flying speed are multiplied to zero by attribute modifiers vanilla removes when the effect ends,
 * and each tick a held mob's path is dropped and its horizontal motion cleared; gravity, knockback
 * from damage and falling still apply. A sleeping creature also forgets its target each tick (its
 * attacks are refused by {@link ControlEvents}). No AI goal is touched and nothing outlives the effect.
 */
public class HeldEffect extends MobEffect {
    private final boolean sleep;

    public HeldEffect(int color, boolean sleep, String walkId, String flyId) {
        super(MobEffectCategory.HARMFUL, color);
        this.sleep = sleep;
        addAttributeModifier(Attributes.MOVEMENT_SPEED, walkId, -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.FLYING_SPEED, flyId, -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public boolean sleep() {
        return sleep;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !(entity instanceof Mob mob)) {
            return;
        }
        mob.getNavigation().stop();
        Vec3 motion = mob.getDeltaMovement();
        mob.setDeltaMovement(0, Math.min(motion.y, 0), 0);
        if (sleep && mob.getTarget() != null) {
            mob.setTarget(null);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
