package io.github.dirtyredz.herbalwitch.brewing.effect.control;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The event side of hard control: a sleeping creature's attacks are refused; any damage that actually
 * lands wakes it at once; and when binding or sleep ends (expiry, waking, milk) the creature becomes
 * steadfast for a while. The immunity is granted on the creature's next tick, never inside the effect
 * map's own iteration.
 */
public final class ControlEvents {
    private static final Set<LivingEntity> ENDED = Collections.newSetFromMap(new WeakHashMap<>());

    private ControlEvents() {
    }

    /** A sleeping creature cannot attack. */
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker.hasEffect(PreparationEffects.SLEEP.get())) {
            event.setCanceled(true);
        }
    }

    /** Damage that lands wakes a sleeper (lowest priority: only damage that really happens). */
    public static void onDamage(LivingDamageEvent event) {
        if (event.getAmount() > 0 && event.getEntity().hasEffect(PreparationEffects.SLEEP.get())) {
            event.getEntity().removeEffect(PreparationEffects.SLEEP.get());
        }
    }

    public static void onExpired(MobEffectEvent.Expired event) {
        ended(event.getEntity(), event.getEffectInstance());
    }

    public static void onRemoved(MobEffectEvent.Remove event) {
        ended(event.getEntity(), event.getEffectInstance());
    }

    private static void ended(LivingEntity entity, MobEffectInstance instance) {
        if (instance == null || entity.level().isClientSide) {
            return;
        }
        if (instance.getEffect() == PreparationEffects.BINDING.get() || instance.getEffect() == PreparationEffects.SLEEP.get()) {
            ENDED.add(entity);
        }
    }

    /** Grants the pending immunities, outside any effect iteration. */
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (ENDED.isEmpty()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (ENDED.remove(entity) && !ControlEffects.held(entity)) {
            ControlEffects.steady(entity);
        }
    }
}
