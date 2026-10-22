package io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.HarmAttribution;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Coated weapons and thorn retaliation, after damage is final (lowest priority: a cancelled or
 * zero-damage hit spends nothing). A direct hit with a venom-coated main-hand weapon poisons the
 * victim ({@link #VENOM_TICKS}) and spends one hit. A creature striking someone under Thorns directly
 * takes {@link #THORN_DAMAGE}, at most once a second per defender. Both pass the entity permission
 * boundary, and retaliation (thorns damage) never triggers retaliation or a coating.
 */
public final class CombatEvents {
    public static final int VENOM_TICKS = 5 * 20;
    public static final float THORN_DAMAGE = 1.0F;
    public static final int THORN_INTERVAL = 20;
    private static final Map<LivingEntity, Long> LAST_THORN = new WeakHashMap<>();

    private CombatEvents() {
    }

    public static void onDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (event.getAmount() <= 0 || event.getEntity().level().isClientSide || source.is(DamageTypes.THORNS)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return;
        }
        coating(attacker, victim);
        thorns(victim, attacker);
    }

    private static void coating(LivingEntity attacker, LivingEntity victim) {
        ItemStack weapon = attacker.getMainHandItem();
        long now = attacker.level().getGameTime();
        if (WeaponCoating.read(weapon, now).isEmpty() || !EntityActionPermission.mayHarm(attacker, victim)) {
            return;
        }
        victim.addEffect(new MobEffectInstance(MobEffects.POISON, VENOM_TICKS, 0), attacker);
        if (victim instanceof Player) {
            HarmAttribution.record(victim, attacker.getUUID(), VENOM_TICKS);
        }
        WeaponCoating.spend(weapon);
    }

    private static void thorns(LivingEntity defender, LivingEntity attacker) {
        if (!defender.hasEffect(PreparationEffects.THORNS.get()) || !EntityActionPermission.mayHarm(defender, attacker)) {
            return;
        }
        long now = defender.level().getGameTime();
        Long last = LAST_THORN.get(defender);
        if (last != null && now - last < THORN_INTERVAL) {
            return;
        }
        LAST_THORN.put(defender, now);
        attacker.hurt(defender.damageSources().thorns(defender), THORN_DAMAGE);
    }
}
