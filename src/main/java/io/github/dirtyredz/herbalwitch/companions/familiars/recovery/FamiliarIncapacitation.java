package io.github.dirtyredz.herbalwitch.companions.familiars.recovery;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import java.util.UUID;

/** Capture before health reaches zero; discard bypasses death loot and experience entirely. */
public final class FamiliarIncapacitation {
    private FamiliarIncapacitation() {}
    public static void damage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide
                || event.getSource().is(DamageTypes.GENERIC_KILL) || event.getAmount() < mob.getHealth()) return;
        var record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) return;
        event.setCanceled(true);
        var store = FamiliarStore.get(mob.getServer());
        var payload = FamiliarSpeciesAdapters.snapshot(mob);
        // Critical death checkpoints cannot be rate-limited by an unrelated owner's menu transfer.
        if (payload.isEmpty()) {
            store.update(record.quarantine(), true); FamiliarReconciliation.hold(mob); return;
        }
        var prepared = record.transition(FamiliarRecord.Phase.INCAPACITATION_PREPARED, record.generation(), UUID.randomUUID(), payload.get())
                .change(s -> { s.remove("buffs"); s.remove("mobileOrder"); s.putString("command", "stay"); s.putFloat("health", 0); });
        if (!store.update(prepared, true)) { FamiliarReconciliation.hold(mob); return; }
        mob.getNavigation().stop(); mob.setTarget(null); mob.discard();
        var incapacitated = prepared.transition(FamiliarRecord.Phase.INCAPACITATED, Math.addExact(prepared.generation(), 1), prepared.transaction(), prepared.payload());
        if (!store.update(incapacitated, true)) return; // Failed shard remains read-only and retains its payload.
        OnlinePlayers.find(record.owner()).ifPresent(owner -> owner.sendSystemMessage(Component.literal(mob.getName().getString() + " is incapacitated. Use the altar to revive your familiar.")));
    }
}
