package io.github.dirtyredz.herbalwitch.apothecary.brewing.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * An effect with no behaviour of its own: a timed, saved marker that event handlers read (concealment,
 * repellents, thorns, the dazzled state, hard-control immunity). Vanilla saves, syncs and expires it,
 * so no extra per-entity state or codec is needed.
 */
public class PlainEffect extends MobEffect {
    public PlainEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
