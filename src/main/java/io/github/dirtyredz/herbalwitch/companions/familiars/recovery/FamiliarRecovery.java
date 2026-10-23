package io.github.dirtyredz.herbalwitch.companions.familiars.recovery;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.companions.lifecycle.CompanionLifecycleAuthority;
import net.minecraft.server.MinecraftServer;
import java.util.UUID;

/** Administrative recovery never grants unresolved escrow or directly spawns an entity. */
public final class FamiliarRecovery {
    private FamiliarRecovery() {}
    public static boolean recover(MinecraftServer server, UUID bond) {
        var store = FamiliarStore.get(server); var record = store.record(bond).orElse(null);
        if (record == null || !store.writable(bond) || record.phase() != FamiliarRecord.Phase.QUARANTINED
                || record.payload().isEmpty() || record.state().contains("receipt")) return false;
        var probe = FamiliarSpeciesAdapters.restore(server.overworld(), record);
        if (probe == null) return false;
        boolean supported = FamiliarSpeciesAdapters.snapshot(probe).isPresent(); probe.discard();
        if (!supported || !store.begin(server.getTickCount())) return false;
        try {
            var prepared = record.transition(FamiliarRecord.Phase.INCAPACITATION_PREPARED, Math.addExact(record.generation(), 1), UUID.randomUUID(), record.payload())
                    .change(s -> { s.remove("buffs"); s.remove("mobileOrder"); s.putString("command", "stay"); });
            if (!store.update(prepared, true)) return false;
            CompanionLifecycleAuthority.retire(server, record.creature());
            return store.update(prepared.transition(FamiliarRecord.Phase.INCAPACITATED, prepared.generation(), prepared.transaction(), prepared.payload()), true);
        } finally { store.end(); }
    }
}
