package io.github.dirtyredz.herbalwitch.brewing.application.advanced;

import io.github.dirtyredz.herbalwitch.brewing.application.DrinkApplication;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

/**
 * What the four utility draughts do when finished, server side, once. Effects refresh (a second
 * draught never adds duration or level). The healing draught restores {@link #HEALING} health and
 * works only on someone hurt; its item keeps a {@link #HEALING_COOLDOWN}-tick cooldown.
 */
public final class DrinkApplications {
    public static final float HEALING = 6.0F;
    public static final int HEALING_COOLDOWN = 10 * DrinkApplication.SECOND;

    /** The utility draughts. */
    public enum Draught {
        NIGHT_VISION(() -> MobEffects.NIGHT_VISION, 120 * DrinkApplication.SECOND, 0),
        WATER_BREATHING(() -> MobEffects.WATER_BREATHING, 120 * DrinkApplication.SECOND, 0),
        RESISTANCE(() -> MobEffects.DAMAGE_RESISTANCE, 90 * DrinkApplication.SECOND, 0),
        HEALING(null, 0, 0);

        private final Supplier<MobEffect> effect;
        private final int duration;
        private final int amplifier;

        Draught(Supplier<MobEffect> effect, int duration, int amplifier) {
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public int duration() {
            return duration;
        }
    }

    private DrinkApplications() {
    }

    /** Whether the drinker can benefit now (healing needs a hurt drinker); checked before and after drinking. */
    public static boolean usable(LivingEntity drinker, Draught draught) {
        return draught != Draught.HEALING || drinker.getHealth() < drinker.getMaxHealth();
    }

    /** Applies the draught; false (nothing happened) when it cannot help. */
    public static boolean apply(LivingEntity drinker, Draught draught) {
        if (!usable(drinker, draught)) {
            return false;
        }
        if (draught == Draught.HEALING) {
            drinker.heal(HEALING);
        } else {
            drinker.addEffect(new MobEffectInstance(draught.effect.get(), draught.duration, draught.amplifier));
        }
        return true;
    }
}
