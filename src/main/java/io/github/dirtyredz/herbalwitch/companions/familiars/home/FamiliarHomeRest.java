package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;

/** Loaded, safe rest only; never grants energy, reserve or recovery from incapacitation. */
public final class FamiliarHomeRest {
    private FamiliarHomeRest() {}
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        var r = FamiliarReconciliation.record(mob).orElse(null); if (r == null || !r.state().contains("home")) return;
        var target = FamiliarHomeTarget.resolve(mob, r); if (target.position() == null) return;
        boolean safe = mob.position().distanceToSqr(target.position()) <= 4 && mob.getTarget() == null && !mob.isOnFire()
                && !mob.isInLava() && !mob.isInWater() && !mob.isFreezing() && !ControlEffects.held(mob)
                && (mob.getLastHurtByMob() == null || mob.tickCount - mob.getLastHurtByMobTimestamp() >= 100);
        int ticks = safe ? r.state().getInt("homeRest") + 1 : 0;
        if (ticks >= 100) { mob.heal(1); ticks = 0; }
        final int next = ticks;
        if (next != r.state().getInt("homeRest")) FamiliarStore.get(mob.getServer()).update(r.change(s -> s.putInt("homeRest", next)), false);
    }
}
