package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

/** One nearby menu's transfer receipt. Pending cursors suspend the familiar until settled, never replayed on login. */
public final class FamiliarEquipmentService {
    private final UUID bond;
    private long generation;
    private UUID transaction;
    private boolean active;
    public FamiliarEquipmentService(UUID bond) { this.bond = bond; }
    public boolean active() { return active; }
    public boolean owns(ServerPlayer owner) {
        var store = FamiliarStore.get(owner.server); var r = store.record(bond).orElse(null);
        return active && r != null && store.writable(bond) && r.owner().equals(owner.getUUID())
                && r.phase() == FamiliarRecord.Phase.ACTIVE && r.generation() == generation && r.transaction().equals(transaction)
                && r.state().getCompound("receipt").equals(owner.getPersistentData().getCompound(FamiliarPlayerReceipt.KEY))
                && FamiliarPlayerReceipt.pending(owner);
    }
    public boolean begin(ServerPlayer owner, ItemStack cursor) {
        if (active) return owns(owner);
        var store = FamiliarStore.get(owner.server); var r = store.record(bond).orElse(null);
        if (!FamiliarMenuAccess.detail(owner, r) || FamiliarPlayerReceipt.pending(owner)) return false;
        var mob = FamiliarMenuAccess.loaded(owner, r);
        var payload = FamiliarSpeciesAdapters.snapshot(mob).orElse(null); if (payload == null) return false;
        generation = r.generation(); transaction = UUID.randomUUID();
        if (!store.update(r.transition(FamiliarRecord.Phase.ACTIVE, generation, transaction, payload), true)) return false;
        active = FamiliarPlayerReceipt.begin(owner, store, bond, cursor);
        if (!active) quarantine(owner);
        return active;
    }
    /** Commit equipment before the player snapshot; a nonempty cursor keeps both sides in a pending receipt. */
    public boolean commit(ServerPlayer owner, ItemStack charm, ItemStack gear, ItemStack cursor) {
        if (!owns(owner)) return false;
        var store = FamiliarStore.get(owner.server); var r = store.record(bond).orElseThrow();
        try {
            if (!store.update(FamiliarEquipmentInventory.equipment(r, charm, gear), true)
                    || !FamiliarPlayerReceipt.cursor(owner, store, bond, cursor)) { quarantine(owner); return false; }
            if (cursor.isEmpty()) {
                if (!FamiliarPlayerReceipt.complete(owner, store, bond, true)) { quarantine(owner); return false; }
                active = false;
            }
            return true;
        } catch (IllegalArgumentException failure) { quarantine(owner); return false; }
    }
    /** A full inventory never ejects a cursor into the world; retain it in the disabled bond for explicit recovery. */
    public boolean escrow(ServerPlayer owner, ItemStack cursor) {
        if (!owns(owner)) return false;
        var store = FamiliarStore.get(owner.server);
        if (!FamiliarPlayerReceipt.cursor(owner, store, bond, cursor)) { quarantine(owner); return false; }
        quarantine(owner); return true;
    }
    public void quarantine(ServerPlayer owner) {
        var store = FamiliarStore.get(owner.server);
        store.record(bond).filter(r -> r.owner().equals(owner.getUUID())).ifPresent(r -> {
            store.update(r.quarantine(), true);
            io.github.dirtyredz.herbalwitch.companions.lifecycle.CompanionLifecycleAuthority.retire(owner.server, r.creature());
        });
    }
}
