package io.github.dirtyredz.herbalwitch.companions.familiars.recovery;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.navigation.FamiliarDestinations;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.companions.lifecycle.CompanionLifecycleAuthority;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.permission.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Revival consumes only proven incapacitated state. Missing/unloaded ACTIVE identities are ineligible. */
public final class FamiliarRevival {
    private FamiliarRevival() {}
    public static boolean eligible(ServerPlayer owner, UUID bond, BlockPos altar) {
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        return record != null && store.writable(bond) && record.owner().equals(owner.getUUID())
                && record.phase() == FamiliarRecord.Phase.INCAPACITATED && !record.payload().isEmpty()
                && !record.state().contains("receipt") && !FamiliarPlayerReceipt.pending(owner)
                && owner.isAlive() && WitchStatusService.isWitch(owner) && owner.serverLevel().hasChunkAt(altar)
                && owner.distanceToSqr(Vec3.atCenterOf(altar)) <= 64 && ActionPermissionService.mayUse(ActionContext.of(owner, owner.level(), altar))
                && !CompanionLifecycleAuthority.live(owner.server, record.creature());
    }
    public static boolean revive(ServerPlayer owner, UUID bond, BlockPos altar, AltarOffering offering) {
        if (!eligible(owner, bond, altar) || !offering.intact(owner) || WitchStatusService.check(owner, 5000) != EnergyService.Result.OK) return false;
        var store = FamiliarStore.get(owner.server); var original = store.record(bond).orElseThrow();
        Mob probe = FamiliarSpeciesAdapters.restore(owner.serverLevel(), original); if (probe == null) return false;
        var destination = FamiliarDestinations.near(owner.serverLevel(), probe, Vec3.atBottomCenterOf(altar), owner, 3); probe.discard();
        if (destination.isEmpty() || !store.begin(owner.server.getTickCount())) return false;
        Mob[] candidate = {null}; boolean[] success = {false}; var backup = offering.backup(owner);
        try {
            var prepared = original.transition(FamiliarRecord.Phase.REVIVAL_PREPARED, Math.addExact(original.generation(), 1), UUID.randomUUID(), original.payload());
            if (!store.update(prepared, true) || !FamiliarPlayerReceipt.begin(owner, store, bond)) return false;
            boolean paid = WitchStatusService.spend(owner, 5000, new WitchStatusService.Change() {
                @Override public boolean prepare() { if (!offering.intact(owner)) return false; offering.consume(owner); return true; }
                @Override public boolean apply() {
                    var current = store.record(bond).orElseThrow(); candidate[0] = FamiliarSpeciesAdapters.restore(owner.serverLevel(), current);
                    if (candidate[0] == null) return false;
                    candidate[0].moveTo(destination.get());
                    io.github.dirtyredz.herbalwitch.companions.familiars.bond.FamiliarBondProgress.project(candidate[0], current);
                    candidate[0].setHealth(candidate[0].getMaxHealth() / 2);
                    candidate[0].setRemainingFireTicks(0); candidate[0].setAirSupply(candidate[0].getMaxAirSupply()); candidate[0].setTicksFrozen(0);
                    if (!FamiliarDestinations.safe(owner.serverLevel(), candidate[0], destination.get(), owner)) return false;
                    try (var permit = FamiliarReconciliation.permit(candidate[0], current)) {
                        if (!owner.serverLevel().addFreshEntity(candidate[0]) || owner.serverLevel().getEntity(current.creature()) != candidate[0]) return false;
                    } catch (Exception failed) { return false; }
                    return store.update(current.transition(FamiliarRecord.Phase.ACTIVE, current.generation(), current.transaction(), current.payload()).change(s -> {
                        s.remove("buffs"); s.remove("mobileOrder"); s.putString("command", "stay"); s.putString("status", "");
                        s.putString("dimension", owner.level().dimension().location().toString()); s.putString("anchorDimension", owner.level().dimension().location().toString());
                        s.putLong("position", candidate[0].blockPosition().asLong()); s.putLong("anchor", candidate[0].blockPosition().asLong());
                        s.putFloat("health", candidate[0].getHealth()); s.putFloat("maxHealth", candidate[0].getMaxHealth());
                    }), true);
                }
                @Override public void rollback() { AltarOffering.restore(owner, backup); }
            });
            success[0] = paid && FamiliarPlayerReceipt.complete(owner, store, bond, true); return success[0];
        } finally {
            if (!success[0]) {
                if (candidate[0] != null) candidate[0].discard();
                store.record(bond).filter(r -> r.revision() != original.revision()).ifPresent(r -> store.update(r.quarantine(), true));
            }
            store.end();
        }
    }
}
