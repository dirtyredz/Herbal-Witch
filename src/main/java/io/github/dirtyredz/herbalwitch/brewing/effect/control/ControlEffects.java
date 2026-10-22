package io.github.dirtyredz.herbalwitch.brewing.effect.control;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * The rules of hard control (binding and sleep). Players are never held; nor are the creatures of
 * {@link #IMMUNE} (ender dragon, wither, warden); modded creatures only when a datapack lists them in
 * {@link #ALLOWED} (their AI is unknown). A held creature cannot be held again while either control
 * lasts, and for {@link #IMMUNITY_TICKS} after one ends it is steadfast against both. Durations never
 * add up and levels never rise.
 */
public final class ControlEffects {
    public static final int IMMUNITY_TICKS = 100;
    public static final TagKey<EntityType<?>> IMMUNE = tag("hard_control_immune");
    public static final TagKey<EntityType<?>> ALLOWED = tag("hard_control_allowed");

    private ControlEffects() {
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path));
    }

    /** Whether binding or sleep may land on {@code target} now. */
    public static boolean controllable(LivingEntity target) {
        if (target instanceof Player || target.getType().is(IMMUNE)) {
            return false;
        }
        ResourceLocation id = EntityType.getKey(target.getType());
        if (!"minecraft".equals(id.getNamespace()) && !target.getType().is(ALLOWED)) {
            return false;
        }
        return !held(target) && !target.hasEffect(PreparationEffects.STEADFAST.get());
    }

    /** Whether a hard control is on the creature now. */
    public static boolean held(LivingEntity target) {
        return target.hasEffect(PreparationEffects.BINDING.get()) || target.hasEffect(PreparationEffects.SLEEP.get());
    }

    /** Applies {@code control} for {@code ticks} if allowed; returns whether it landed. */
    public static boolean hold(LivingEntity target, MobEffect control, int ticks) {
        if (!controllable(target)) {
            return false;
        }
        return target.addEffect(new MobEffectInstance(control, ticks, 0));
    }

    /** The immunity that follows a hard control. */
    public static void steady(LivingEntity target) {
        if (target.isAlive()) {
            target.addEffect(new MobEffectInstance(PreparationEffects.STEADFAST.get(), IMMUNITY_TICKS, 0));
        }
    }
}
