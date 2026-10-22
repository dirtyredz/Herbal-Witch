package io.github.dirtyredz.herbalwitch.apothecary.brewing.application;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.TimedEffect;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.DrinkRecovery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

/**
 * What each kettle drink does when finished, server side, once. Effects refresh (a second cup never
 * adds duration or level). Energy tea is ordinary food for everyone and restores energy for a witch
 * ({@link DrinkRecovery}); the restful infusion resets the vanilla time-since-rest statistic
 * (phantoms), forces no sleep, and prepares a witch's next rewarded sleep.
 */
public final class DrinkApplication {
    public static final float HEALING = 6.0F;
    public static final int HEALING_COOLDOWN = 10 * TickTime.SECOND;

    /** Kettle drinks and utility draughts. No enum ordinal is persisted. */
    public enum Drink {
        RECOVERY(() -> MobEffects.REGENERATION, 90 * TickTime.SECOND, 0),
        ENERGY(null, 0, 0),
        RESTFUL(null, 0, 0),
        WARMTH(() -> PreparationEffects.WARMTH.get(), 90 * TickTime.SECOND, 0),
        COOLING(() -> MobEffects.FIRE_RESISTANCE, 60 * TickTime.SECOND, 0),
        MINING(() -> MobEffects.DIG_SPEED, 90 * TickTime.SECOND, 0),
        TRAVEL(() -> MobEffects.MOVEMENT_SPEED, 90 * TickTime.SECOND, 0),
        NIGHT_VISION(() -> MobEffects.NIGHT_VISION, 120 * TickTime.SECOND, 0),
        WATER_BREATHING(() -> MobEffects.WATER_BREATHING, 120 * TickTime.SECOND, 0),
        RESISTANCE(() -> MobEffects.DAMAGE_RESISTANCE, 90 * TickTime.SECOND, 0),
        HEALING(null, 0, 0);

        private final TimedEffect effect;

        Drink(Supplier<MobEffect> effect, int duration, int amplifier) {
            this.effect = effect == null ? null : new TimedEffect(effect, duration, amplifier);
        }

        public int duration() {
            return effect == null ? 0 : effect.duration();
        }

        public MobEffectInstance effectInstance() {
            return effect == null ? null : effect.instance();
        }
    }

    /** Energy tea food: 4 hunger points, saturation modifier 0.5. */
    public static final int ENERGY_NUTRITION = 4;
    public static final float ENERGY_SATURATION = 0.5F;

    private DrinkApplication() {
    }

    /** Healing needs a hurt drinker; checked at both use boundaries. */
    public static boolean usable(LivingEntity drinker, Drink drink) {
        return drink != Drink.HEALING || drinker.getHealth() < drinker.getMaxHealth();
    }

    public static boolean apply(LivingEntity drinker, Drink drink) {
        if (!usable(drinker, drink)) {
            return false;
        }
        if (drink == Drink.HEALING) {
            drinker.heal(HEALING);
        }
        MobEffectInstance effect = drink.effectInstance();
        if (effect != null) {
            drinker.addEffect(effect);
        }
        if (drinker instanceof ServerPlayer player) {
            switch (drink) {
                case ENERGY -> {
                    player.getFoodData().eat(ENERGY_NUTRITION, ENERGY_SATURATION);
                    DrinkRecovery.energyTea(player);
                }
                case RESTFUL -> {
                    player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                    DrinkRecovery.restfulInfusion(player);
                }
                default -> {
                }
            }
        }
        return true;
    }
}
