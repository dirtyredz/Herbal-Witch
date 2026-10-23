package io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.FamiliarEquipmentInventory;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.ItemPickupPermission;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

/** Whole small stacks only: durable retirement precedes cargo, and neither uncertainty nor collection replays items. */
public final class FamiliarRetrieveItem {
    private FamiliarRetrieveItem() {}
    public static boolean apply(ServerPlayer owner, Mob mob, ItemEntity source, CompoundTag observedStack) {
        var record = FamiliarReconciliation.record(mob).orElse(null);
        if (record == null || !record.owner().equals(owner.getUUID()) || FamiliarPlayerReceipt.pending(owner)
                || !FamiliarAbilityService.ready(mob, FamiliarPower.RETRIEVE) || !FamiliarRetrievalRules.eligible(owner, mob, record, source)
                || !source.getItem().save(new CompoundTag()).equals(observedStack) || !ItemPickupPermission.authorize(owner, source)
                || !FamiliarRetrievalRules.eligible(owner, mob, record, source) || !source.getItem().save(new CompoundTag()).equals(observedStack)) return false;
        var payload = FamiliarSpeciesAdapters.snapshot(mob).orElse(null); if (payload == null) return false;
        var itemPayload = new CompoundTag(); if (!source.save(itemPayload)) return false;
        if (itemPayload.contains("ForgeCaps") && !itemPayload.getCompound("ForgeCaps").isEmpty()) return false;
        var prepared = record.transition(FamiliarRecord.Phase.ACTIVE, record.generation(), UUID.randomUUID(), payload)
                .change(s -> s.put("pickupPending", itemPayload.copy()));
        try { FamiliarCodec.encode(prepared, io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.current().recordBytes()); }
        catch (java.io.IOException | RuntimeException tooLarge) { return false; }
        var store = FamiliarStore.get(owner.server); if (!store.begin(owner.server.getTickCount())) return false;
        var journal = FamiliarPickupReceipts.get(owner.server); boolean success = false, claimed = false;
        var stack = source.getItem().copy();
        try {
            if (!store.update(prepared, true) || !FamiliarPlayerReceipt.begin(owner, store, record.bond())) return false;
            claimed = true;
            if (!journal.prepare(source.getUUID(), record.bond(), prepared.transaction(), itemPayload)) return false;
            source.discard();
            boolean paid = WitchStatusService.spend(owner, FamiliarPower.RETRIEVE.cost(record), () -> {
                var current = store.record(record.bond()).orElseThrow(); var cargo = FamiliarEquipmentInventory.cargo(current);
                if (cargo.isEmpty()) cargo = stack.copy(); else cargo.grow(stack.getCount());
                var next = FamiliarEquipmentInventory.cargo(current, cargo).change(s -> {
                    s.remove("pickupPending"); var cooldowns = s.getCompound("powerCooldowns");
                    cooldowns.putLong("retrieve", FamiliarAbilityService.clock(record) + FamiliarPower.RETRIEVE.cooldown(mob, record)); s.put("powerCooldowns", cooldowns);
                });
                return store.update(next, true);
            });
            success = paid && FamiliarPlayerReceipt.complete(owner, store, record.bond(), true); return success;
        } finally {
            if (!success) {
                if (claimed && journal.retired(source.getUUID())) source.discard();
                store.record(record.bond()).filter(r -> r.revision() != record.revision()).ifPresent(r -> { store.update(r.quarantine(), true); mob.discard(); });
            }
            store.end();
        }
    }
}
