package io.github.dirtyredz.herbalwitch.brewing.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Pest repellent: pests ({@code herbalwitch:repelled_pests}) will not target the bearer ({@link PestRepellentEvents}). */
public class PestRepellentEffect extends MobEffect {
    public PestRepellentEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7A5C8A);
    }
}
