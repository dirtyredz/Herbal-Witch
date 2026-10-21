package io.github.dirtyredz.herbalwitch.brewing.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Warmth: vanilla freezing (powder snow) does not build up and any built-up freezing clears. Effect
 * ticks run before the entity's freezing step each tick, so frozen ticks never reach the damage
 * threshold. Nothing else: no temperature-mod claims.
 */
public class WarmthEffect extends MobEffect {
    public WarmthEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE08A3C);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
