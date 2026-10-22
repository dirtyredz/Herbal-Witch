package io.github.dirtyredz.herbalwitch.apothecary.brewing.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.function.Supplier;

/** A lazy registry reference and the fixed duration/level of one refreshing application. */
public record TimedEffect(Supplier<MobEffect> effect, int duration, int amplifier) {
    public MobEffectInstance instance() {
        return new MobEffectInstance(effect.get(), duration, amplifier);
    }
}
