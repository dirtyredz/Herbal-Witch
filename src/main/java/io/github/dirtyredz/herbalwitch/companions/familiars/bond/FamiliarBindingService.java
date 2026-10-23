package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import java.util.UUID;

/** Exact conversion. Any uncertain filesystem or insertion result retains the recovery payload. */
public final class FamiliarBindingService {
    private FamiliarBindingService() {}
    public static boolean bind(ServerPlayer player, Mob source, BlockPos altar, AltarOffering offering) {
        if (FamiliarEligibility.refusal(player, source, altar) != null || !offering.intact(player)) return false;
        if (WitchStatusService.check(player, 2500) != io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService.Result.OK) return false;
        var snapshot = FamiliarSpeciesAdapters.snapshot(source);
        if (snapshot.isEmpty()) return false;
        var store = FamiliarStore.get(player.server);
        if (!store.begin(player.server.getTickCount())) return false;
        FamiliarRecord prepared = FamiliarRecord.binding(player.getUUID(), source.getUUID(),
                FamiliarCatalog.type(EntityType.getKey(source.getType()).toString()).orElseThrow().key(), 1, snapshot.get());
        prepared = prepared.change(s -> {
            s.putString("dimension", source.level().dimension().location().toString()); s.putLong("position", source.blockPosition().asLong());
            s.putLong("anchor", source.blockPosition().asLong()); s.putString("anchorDimension", source.level().dimension().location().toString());
            s.putString("name", source.getName().getString());
        });
        final UUID bond = prepared.bond();
        Mob[] candidate = {null}; boolean[] succeeded = {false};
        var backup = offering.backup(player);
        try {
            if (!store.create(prepared) || !FamiliarPlayerReceipt.begin(player, store, bond)) return false;
            boolean paid = WitchStatusService.spend(player, 2500, new WitchStatusService.Change() {
                @Override public boolean prepare() { if (!offering.intact(player)) return false; offering.consume(player); return true; }
                @Override public boolean apply() {
                    var current = store.record(bond).orElseThrow();
                    candidate[0] = FamiliarSpeciesAdapters.restore(player.serverLevel(), current);
                    if (candidate[0] == null) return false;
                    source.discard();
                    try (var permit = FamiliarReconciliation.permit(candidate[0], current)) {
                        if (!player.serverLevel().addFreshEntity(candidate[0]) || player.serverLevel().getEntity(current.creature()) != candidate[0]) return false;
                    } catch (Exception failure) { return false; }
                    return store.update(current.transition(FamiliarRecord.Phase.ACTIVE, current.generation(), current.transaction(), current.payload()), true);
                }
                @Override public void rollback() { AltarOffering.restore(player, backup); }
            });
            succeeded[0] = paid && FamiliarPlayerReceipt.complete(player, store, bond, true);
            return succeeded[0];
        } finally {
            if (!succeeded[0]) {
                if (candidate[0] != null) candidate[0].discard();
                store.record(bond).ifPresent(r -> store.update(r.quarantine(), true));
                // A prepared identity cannot keep acting even when its original object survived.
                if (store.creature(source.getUUID()).isPresent() && !source.isRemoved()) source.discard();
            }
            store.end();
        }
    }
}
