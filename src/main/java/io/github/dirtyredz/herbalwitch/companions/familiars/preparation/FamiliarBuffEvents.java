package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.navigation.FamiliarDestinations;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.*;

public final class FamiliarBuffEvents {
    private FamiliarBuffEvents() {}
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        var record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) return;
        FamiliarBuffEffects.project(mob, record);
        record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) return;
        var state = record.state();
        if (state.getCompound("buffs").isEmpty() && !state.contains("poisonCoating")) return;
        var next = record.change(s -> {
            s.put("buffs", FamiliarBuffRules.advance(s.getCompound("buffs")));
            if (s.contains("poisonCoating")) {
                var coating = s.getCompound("poisonCoating"); int ticks = coating.getInt("ticks") - 1;
                if (ticks <= 0) s.remove("poisonCoating"); else { coating.putInt("ticks", ticks); s.put("poisonCoating", coating); }
            }
        });
        if (FamiliarStore.get(mob.getServer()).update(next, false)) FamiliarBuffEffects.project(mob, next);
        if (FamiliarBuffRules.active(next.state(), "shadowstep") && (mob.isOnFire() || mob.isInLava() || mob.isFreezing()
                || mob.getAirSupply() < mob.getMaxAirSupply() / 2 || mob.getHealth() < mob.getMaxHealth() * .25F)) {
            var owner = OnlinePlayers.find(next.owner()).orElse(null); if (owner != null && owner.level() != mob.level()) owner = null;
            var destination = FamiliarDestinations.near((ServerLevel) mob.level(), mob, mob.position(), owner, 3);
            if (destination.isEmpty() || destination.get().distanceToSqr(mob.position()) > 64) return;
            var point = destination.get();
            mob.teleportTo(point.x, point.y, point.z); mob.fallDistance = 0;
            if (mob.position().distanceToSqr(point) < .01) {
                var current = FamiliarReconciliation.record(mob).orElse(null);
                if (current != null) FamiliarStore.get(mob.getServer()).update(current.change(s -> { var buffs = s.getCompound("buffs"); buffs.remove("shadowstep"); s.put("buffs", buffs); }), true);
            }
        }
    }
    public static void damage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || event.getAmount() <= 0) return;
        var record = FamiliarReconciliation.record(mob).orElse(null);
        if (record == null || !FamiliarBuffRules.active(record.state(), "warding")) return;
        var source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) return;
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) event.setAmount(event.getAmount() * .8F);
    }
}
