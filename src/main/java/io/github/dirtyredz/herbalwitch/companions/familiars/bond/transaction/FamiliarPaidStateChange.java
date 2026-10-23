package io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;
import java.util.function.UnaryOperator;

/** Durable paid bond-state mutation shared by upgrades and explicit prepaid-reserve charges. */
public final class FamiliarPaidStateChange {
    private FamiliarPaidStateChange() {}
    public static boolean commit(ServerPlayer owner, UUID bond, int units, AltarOffering offering, UnaryOperator<FamiliarRecord> change) {
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        if (record == null || !record.owner().equals(owner.getUUID()) || FamiliarPlayerReceipt.pending(owner) || !offering.intact(owner)
                || WitchStatusService.check(owner, units) != EnergyService.Result.OK) return false;
        var mob = FamiliarReconciliation.loaded(owner.server, record);
        if (mob == null || FamiliarReconciliation.record(mob).isEmpty()) return false;
        var payload = FamiliarSpeciesAdapters.snapshot(mob).orElse(null);
        if (payload == null || !store.begin(owner.server.getTickCount())) return false;
        boolean success = false; var backup = offering.backup(owner);
        try {
            if (!store.update(record.transition(FamiliarRecord.Phase.ACTIVE, record.generation(), UUID.randomUUID(), payload), true)
                    || !FamiliarPlayerReceipt.begin(owner, store, bond)) return false;
            boolean paid = WitchStatusService.spend(owner, units, new WitchStatusService.Change() {
                @Override public boolean prepare() { if (!offering.intact(owner)) return false; offering.consume(owner); return true; }
                @Override public boolean apply() { return store.update(change.apply(store.record(bond).orElseThrow()), true); }
                @Override public void rollback() { AltarOffering.restore(owner, backup); }
            });
            success = paid && FamiliarPlayerReceipt.complete(owner, store, bond, true); return success;
        } finally {
            if (!success) store.record(bond).filter(r -> r.revision() != record.revision()).ifPresent(r -> { store.update(r.quarantine(), true); mob.discard(); });
            store.end();
        }
    }
}
