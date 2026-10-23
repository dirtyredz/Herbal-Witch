package io.github.dirtyredz.herbalwitch.companions.familiars.menu;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public final class FamiliarMenuAccess {
    private FamiliarMenuAccess() {}
    public static boolean roster(ServerPlayer owner) {
        return owner.isAlive() && !owner.isSpectator() && owner.getInventory().hasAnyMatching(GrimoireItems::isGrimoire)
                && FamiliarStore.get(owner.server).ready();
    }
    public static Mob loaded(ServerPlayer owner, FamiliarRecord record) {
        for (var level : owner.server.getAllLevels()) {
            if (level.getEntity(record.creature()) instanceof Mob mob && mob.isAlive() && !mob.isRemoved() && FamiliarReconciliation.matches(mob, record)) return mob;
        }
        return null;
    }
    public static boolean detail(ServerPlayer owner, FamiliarRecord record) {
        if (!roster(owner) || record == null || !record.owner().equals(owner.getUUID()) || record.phase() != FamiliarRecord.Phase.ACTIVE
                || !FamiliarStore.get(owner.server).writable(record.bond()) || record.state().contains("receipt")) return false;
        var mob = loaded(owner, record);
        return mob != null && mob.level() == owner.level() && owner.canReach(mob, 0) && owner.hasLineOfSight(mob);
    }
}
