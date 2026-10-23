package io.github.dirtyredz.herbalwitch.companions.familiars.menu;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public final class FamiliarMenuAccess {
    private FamiliarMenuAccess() {}
    private static final java.util.List<java.util.function.Predicate<ServerPlayer>> FOCI = new java.util.concurrent.CopyOnWriteArrayList<>();
    /** Alternate held focus authorization; menu/session/ownership checks remain here. */
    public static void focus(java.util.function.Predicate<ServerPlayer> contribution) { FOCI.add(contribution); }
    public static boolean roster(ServerPlayer owner) {
        return owner.isAlive() && !owner.isSpectator() && (owner.getInventory().hasAnyMatching(GrimoireItems::isGrimoire) || FOCI.stream().anyMatch(f -> f.test(owner)))
                && FamiliarStore.get(owner.server).ready();
    }
    public static Mob loaded(ServerPlayer owner, FamiliarRecord record) {
        return FamiliarReconciliation.loaded(owner.server, record);
    }
    public static boolean detail(ServerPlayer owner, FamiliarRecord record) {
        if (!roster(owner) || record == null || !record.owner().equals(owner.getUUID()) || record.phase() != FamiliarRecord.Phase.ACTIVE
                || !FamiliarStore.get(owner.server).writable(record.bond()) || record.state().contains("receipt")) return false;
        var mob = loaded(owner, record);
        return mob != null && mob.level() == owner.level() && owner.canReach(mob, 0) && owner.hasLineOfSight(mob);
    }
}
