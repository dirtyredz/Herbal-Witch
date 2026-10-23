package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.navigation.FamiliarDestinations;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.permission.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import java.util.UUID;

/** Loaded-entity transfer seam for the roster and M10. Never reconstructs an absent entity. */
public final class FamiliarRecall {
    private FamiliarRecall() {}
    public static String recall(ServerPlayer owner, UUID bond) {
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        if (!FamiliarMenuAccess.roster(owner) || record == null || !record.owner().equals(owner.getUUID())
                || record.phase() != FamiliarRecord.Phase.ACTIVE || !store.writable(bond) || record.state().contains("receipt")
                || FamiliarPlayerReceipt.pending(owner)) return "unavailable";
        var source = FamiliarMenuAccess.loaded(owner, record);
        if (source == null) return "not_loaded";
        if (source.isPassenger() || source.isVehicle() || source.isLeashed() || source.isNoAi() || ControlEffects.held(source)) return "controlled";
        if (record.state().getLong("recallAt") > owner.server.overworld().getGameTime()) return "cooldown";
        if (!ActionPermissionService.mayUse(ActionContext.of(owner, source.level(), source.blockPosition()))
                || !io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.check(
                io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action.ANIMAL_RELEASE, owner, source).allowed()) return "permission";
        var destination = FamiliarDestinations.near(owner.serverLevel(), source, owner.position(), owner, 3);
        if (destination.isEmpty()) return "blocked";
        var snapshot = FamiliarSpeciesAdapters.snapshot(source);
        if (snapshot.isEmpty() || WitchStatusService.check(owner, 500) != EnergyService.Result.OK) return "unavailable";
        if (!store.begin(owner.server.getTickCount())) return "busy";
        Mob[] candidate = {null}; boolean[] success = {false};
        try {
            var prepared = record.transition(FamiliarRecord.Phase.TRANSFER_PREPARED, Math.addExact(record.generation(), 1), UUID.randomUUID(), snapshot.get());
            if (!store.update(prepared, true) || !FamiliarPlayerReceipt.begin(owner, store, bond)) return "interrupted";
            boolean paid = WitchStatusService.spend(owner, 500, () -> {
                var current = store.record(bond).orElseThrow();
                candidate[0] = FamiliarSpeciesAdapters.restore(owner.serverLevel(), current);
                if (candidate[0] == null) return false;
                candidate[0].moveTo(destination.get());
                if (!FamiliarDestinations.safe(owner.serverLevel(), candidate[0], destination.get(), owner)) return false;
                source.discard();
                try (var permit = FamiliarReconciliation.permit(candidate[0], current)) {
                    if (!owner.serverLevel().addFreshEntity(candidate[0]) || owner.serverLevel().getEntity(current.creature()) != candidate[0]) return false;
                } catch (Exception failure) { return false; }
                var next = current.transition(FamiliarRecord.Phase.ACTIVE, current.generation(), current.transaction(), current.payload()).change(s -> {
                    s.putString("command", FamiliarCommand.STAY.key()); s.remove("mobileOrder"); s.putString("status", "");
                    s.putLong("anchor", candidate[0].blockPosition().asLong()); s.putLong("position", candidate[0].blockPosition().asLong());
                    s.putString("anchorDimension", owner.level().dimension().location().toString()); s.putString("dimension", owner.level().dimension().location().toString());
                    s.putLong("recallAt", owner.server.overworld().getGameTime() + 100);
                });
                return store.update(next, true);
            });
            success[0] = paid && FamiliarPlayerReceipt.complete(owner, store, bond, true);
            return success[0] ? "recalled" : "interrupted";
        } finally {
            if (!success[0]) {
                if (candidate[0] != null) candidate[0].discard();
                var current = store.record(bond).orElseThrow();
                if (current.phase() != FamiliarRecord.Phase.ACTIVE || current.generation() != record.generation()) {
                    source.discard(); store.update(current.quarantine(), true);
                }
            }
            store.end();
        }
    }
}
