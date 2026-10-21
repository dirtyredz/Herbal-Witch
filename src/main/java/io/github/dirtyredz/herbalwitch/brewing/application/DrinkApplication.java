package io.github.dirtyredz.herbalwitch.brewing.application;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

/**
 * What each kettle drink does when finished, server side, once. Effects refresh (a second cup never
 * adds duration or level). Energy tea is ordinary food; its +25 witch energy waits for Milestone 6.
 * The restful infusion resets the vanilla time-since-rest statistic (phantoms) and forces no sleep.
 */
public final class DrinkApplication {
    public static final int SECOND = 20;

    /** The seven kettle drinks. */
    public enum Drink {
        RECOVERY(() -> MobEffects.REGENERATION, 90 * SECOND, 0),
        ENERGY(null, 0, 0),
        RESTFUL(null, 0, 0),
        WARMTH(() -> PreparationEffects.WARMTH.get(), 90 * SECOND, 0),
        COOLING(() -> MobEffects.FIRE_RESISTANCE, 60 * SECOND, 0),
        MINING(() -> MobEffects.DIG_SPEED, 90 * SECOND, 0),
        TRAVEL(() -> MobEffects.MOVEMENT_SPEED, 90 * SECOND, 0);

        private final Supplier<MobEffect> effect;
        private final int duration;
        private final int amplifier;

        Drink(Supplier<MobEffect> effect, int duration, int amplifier) {
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public MobEffectInstance effectInstance() {
            return effect == null ? null : new MobEffectInstance(effect.get(), duration, amplifier);
        }
    }

    /** Energy tea food: 4 hunger points, saturation modifier 0.5. */
    public static final int ENERGY_NUTRITION = 4;
    public static final float ENERGY_SATURATION = 0.5F;
    /** Energy tea item cooldown after drinking. */
    public static final int ENERGY_COOLDOWN = 60 * SECOND;

    private DrinkApplication() {
    }

    public static void apply(LivingEntity drinker, Drink drink) {
        MobEffectInstance effect = drink.effectInstance();
        if (effect != null) {
            drinker.addEffect(effect);
        }
        if (drinker instanceof ServerPlayer player) {
            switch (drink) {
                case ENERGY -> player.getFoodData().eat(ENERGY_NUTRITION, ENERGY_SATURATION);
                case RESTFUL -> player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                default -> {
                }
            }
        }
    }
}
