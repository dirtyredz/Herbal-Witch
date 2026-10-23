package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarActionCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** The three permanent/progression rituals share the same durable player payment boundary. */
public final class FamiliarUpgrades {
    private FamiliarUpgrades() {}
    public static boolean eligible(ServerPlayer owner, UUID bond, BlockPos altar, AltarActionSpec action) {
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        if (record == null || !store.writable(bond) || !record.owner().equals(owner.getUUID()) || FamiliarPlayerReceipt.pending(owner)) return false;
        var mob = FamiliarReconciliation.loaded(owner.server, record);
        if (mob == null || FamiliarReconciliation.record(mob).isEmpty() || mob.level() != owner.level() || !owner.isAlive()
                || !WitchStatusService.isWitch(owner) || mob.distanceToSqr(Vec3.atCenterOf(altar)) > 16
                || owner.distanceToSqr(Vec3.atCenterOf(altar)) > 16 || !owner.hasLineOfSight(mob)
                || !EntityActionPermission.check(EntityActionContext.Action.ANIMAL_CARE, owner, mob).allowed()) return false;
        var state = record.state();
        if (action == FamiliarActionCatalog.HARMONY) return state.getInt("xp") < 350 && (!state.contains("harmonyAt")
                || state.getCompound("progress").getLong("clock") >= state.getLong("harmonyAt"));
        return FamiliarBondProgress.level(state.getInt("xp")) >= 3 && (action == FamiliarActionCatalog.HEARTWOOD && !state.getBoolean("heartwood")
                || action == FamiliarActionCatalog.SPIRIT_WELL && !state.getBoolean("spiritWell"));
    }
    public static boolean apply(ServerPlayer owner, UUID bond, BlockPos altar, AltarActionSpec action, AltarOffering offering) {
        if (!eligible(owner, bond, altar, action) || !offering.intact(owner) || WitchStatusService.check(owner, action.energy() * 100) != EnergyService.Result.OK) return false;
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElseThrow();
        var mob = FamiliarReconciliation.loaded(owner.server, record); var payload = FamiliarSpeciesAdapters.snapshot(mob).orElse(null);
        if (payload == null || !store.begin(owner.server.getTickCount())) return false;
        boolean success = false; var backup = offering.backup(owner);
        try {
            if (!store.update(record.transition(FamiliarRecord.Phase.ACTIVE, record.generation(), UUID.randomUUID(), payload), true)
                    || !FamiliarPlayerReceipt.begin(owner, store, bond)) return false;
            boolean paid = WitchStatusService.spend(owner, action.energy() * 100, new WitchStatusService.Change() {
                @Override public boolean prepare() { if (!offering.intact(owner)) return false; offering.consume(owner); return true; }
                @Override public boolean apply() {
                    var current = store.record(bond).orElseThrow();
                    if (action == FamiliarActionCatalog.HARMONY) current = FamiliarBondProgress.award(current, 10, true);
                    return store.update(current.change(s -> {
                        if (action == FamiliarActionCatalog.HEARTWOOD) s.putBoolean("heartwood", true);
                        else if (action == FamiliarActionCatalog.SPIRIT_WELL) s.putBoolean("spiritWell", true);
                        else s.putLong("harmonyAt", s.getCompound("progress").getLong("clock") + 24000);
                    }), true);
                }
                @Override public void rollback() { AltarOffering.restore(owner, backup); }
            });
            success = paid && FamiliarPlayerReceipt.complete(owner, store, bond, true);
            if (success) FamiliarBondProgress.project(mob, store.record(bond).orElseThrow());
            return success;
        } finally {
            if (!success) store.record(bond).filter(r -> r.revision() != record.revision()).ifPresent(r -> {
                store.update(r.quarantine(), true); mob.discard();
            });
            store.end();
        }
    }
}
