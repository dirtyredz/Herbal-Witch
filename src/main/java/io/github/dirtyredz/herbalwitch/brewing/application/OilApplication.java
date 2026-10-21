package io.github.dirtyredz.herbalwitch.brewing.application;

import io.github.dirtyredz.herbalwitch.brewing.effect.PestRepellentEvents;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Oils applied to yourself: the fishing blend (Luck I, normal fishing loot rules) and the pest
 * repellent (90 s of avoidance by silverfish and endermites, clearing their current targets).
 * Lantern oil is poured into an oil lantern instead (OilLanternBlock).
 */
public final class OilApplication {
    public static final int DURATION = 90 * DrinkApplication.SECOND;

    /** Self-applied oils. */
    public enum Oil {
        FISHING,
        PEST,
        LANTERN
    }

    private OilApplication() {
    }

    public static void apply(Player player, Oil oil) {
        switch (oil) {
            case FISHING -> player.addEffect(new MobEffectInstance(MobEffects.LUCK, DURATION, 0));
            case PEST -> {
                player.addEffect(new MobEffectInstance(PreparationEffects.PEST_REPELLENT.get(), DURATION, 0));
                PestRepellentEvents.clearTargets(player);
            }
            case LANTERN -> {
            }
        }
    }
}
