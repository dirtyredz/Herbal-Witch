package io.github.dirtyredz.herbalwitch.platform.permission.entity;

import io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.List;

/** Entity pickup authorization, separate from block placement and damage. Policies are additive vetoes. */
public final class ItemPickupPermission {
    @FunctionalInterface public interface Policy { EntityActionPermission.Denial check(ServerPlayer actor, ItemEntity item); }
    private static final List<Policy> POLICIES = new java.util.concurrent.CopyOnWriteArrayList<>();
    private ItemPickupPermission() {}
    public static void register(Policy policy) { POLICIES.add(policy); }
    public static boolean allowed(ServerPlayer actor, ItemEntity item) {
        if (actor == null || !actor.isAlive() || actor.isSpectator() || item == null || item.isRemoved()
                || item.level() != actor.level() || item.hasPickUpDelay() || !PrincipalPermission.mayUse(actor.serverLevel(), actor.getUUID(), item.blockPosition()).allowed()) return false;
        var saved = new net.minecraft.nbt.CompoundTag(); item.addAdditionalSaveData(saved);
        if (saved.hasUUID("Owner") && !saved.getUUID("Owner").equals(actor.getUUID())) return false;
        for (var policy : POLICIES) if (!policy.check(actor, item).allowed()) return false;
        return true;
    }
    /** The actual transfer also honors Forge's pickup veto, then rechecks the exact stack. */
    public static boolean authorize(ServerPlayer actor, ItemEntity item) {
        return allowed(actor, item) && !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.EntityItemPickupEvent(actor, item));
    }
}
