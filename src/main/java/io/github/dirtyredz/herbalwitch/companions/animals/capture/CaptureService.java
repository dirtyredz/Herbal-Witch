package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord.Phase;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.payload.VanillaCaptureAdapters;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.companions.lifecycle.CompanionLifecycleAuthority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import java.util.UUID;

/** Synchronous server transactions. No charm exists until its payload is durably stored. */
public final class CaptureService {
    private CaptureService() {}
    public static String capture(ServerPlayer player, Mob mob, InteractionHand hand) {
        String denial = CapturePolicy.check(player, mob, hand); if (denial != null) return denial;
        var data = CaptureSavedData.get(player.server);
        if (!data.writable()) return "persistence";
        if (!data.capacity(mob.getUUID(), player.getUUID())) return "capacity";
        var previous = data.record(mob.getUUID()).orElse(null);
        long generation = previous == null ? 0 : previous.generation();
        if (CaptureReconciliation.generation(mob) != generation || generation == Long.MAX_VALUE) return "generation";
        var payload = VanillaCaptureAdapters.snapshot(mob); if (payload == null) return "payload";
        if (location(mob).length() > 256) return "unsupported";
        CaptureRecord prepared = new CaptureRecord(mob.getUUID(), generation + 1, UUID.randomUUID(), Phase.CAPTURE_PREPARED,
                player.getUUID(), UUID.randomUUID(), EntityType.getKey(mob.getType()).toString(), 1,
                EntityType.getKey(mob.getType()).toString(), location(mob), "", payload);
        if (!data.begin(player.server.getTickCount())) return "busy";
        try {
            if (!data.commit(prepared)) return "persistence";
            mob.discard();
            if (!mob.isRemoved() || player.serverLevel().getEntity(mob.getUUID()) == mob) return quarantine(data, prepared);
            CaptureRecord stored = prepared.phase(Phase.STORED, "");
            if (!data.commit(stored)) return "persistence";
            player.setItemInHand(hand, CaptureToken.item(stored));
            if (player.containerMenu instanceof io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu) player.closeContainer();
            return null;
        } catch (RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(CaptureService.class).error("Capture interrupted; retained payload requires inspection", failure);
            return quarantine(data, prepared);
        } finally { data.end(); }
    }

    public static String release(ServerPlayer player, InteractionHand hand, BlockPos clicked, Direction face) {
        if (player instanceof FakePlayer || !player.isAlive() || player.isSpectator()) return "actor";
        var data = CaptureSavedData.get(player.server);
        if (!data.writable()) return "persistence";
        UUID token = CaptureToken.read(player.getItemInHand(hand));
        var record = data.token(token).orElse(null);
        if (record == null || record.phase() != Phase.STORED) return "token";
        if (!record.captor().equals(player.getUUID())) return "owner";
        if (CompanionLifecycleAuthority.live(player.server, record.animal())) return "generation";
        Mob candidate = VanillaCaptureAdapters.restore(player.serverLevel(), record); if (candidate == null) return "payload";
        try {
            long prior = CaptureReconciliation.generation(candidate);
            if (!AnimalTrustService.generation(candidate, prior, record.generation())) return "generation";
            CaptureReleasePlacement.position(candidate, clicked, face);
            if (!CaptureReleasePlacement.safe(player, candidate, clicked)) return "destination";
            if (location(candidate).length() > 256) return "destination";
            CaptureRecord prepared = record.phase(Phase.RELEASE_PREPARED, location(candidate));
            if (!data.begin(player.server.getTickCount())) return "busy";
            try {
                if (!data.commit(prepared)) return "persistence";
                if (!CaptureReleasePlacement.safe(player, candidate, clicked)) {
                    if (!data.commit(record)) return "persistence";
                    return "destination";
                }
                boolean inserted;
                try (var permit = CaptureReconciliation.permit(player.serverLevel(), candidate, prepared)) {
                    inserted = player.serverLevel().addFreshEntity(candidate);
                }
                if (!inserted || candidate.isRemoved() || player.serverLevel().getEntity(record.animal()) != candidate)
                    return quarantine(data, prepared);
                if (!data.commit(prepared.phase(Phase.RELEASED, prepared.destination()))) return "persistence";
                data.compact(record.animal());
                player.setItemInHand(hand, new ItemStack(AnimalItems.CHARM.get()));
                candidate = null; // exact inserted object now belongs to the durable released generation
                return null;
            } catch (Exception failure) {
                org.slf4j.LoggerFactory.getLogger(CaptureService.class).error("Release interrupted; automatic replay refused", failure);
                return quarantine(data, prepared);
            } finally { data.end(); }
        } finally { if (candidate != null) candidate.discard(); }
    }

    /** Operator commands rotate authority before delivering one item, and never reconstruct an animal. */
    public static String issue(ServerPlayer destination, UUID animal, boolean recovery) {
        var data = CaptureSavedData.get(destination.server); var record = data.record(animal).orElse(null);
        if (!data.writable()) return "persistence";
        if (record == null || !record.captor().equals(destination.getUUID())) return "owner";
        int slot = destination.getInventory().getFreeSlot(); if (slot < 0) return "inventory";
        if (recovery ? !record.storedPayload() || record.phase() == Phase.STORED : record.phase() != Phase.STORED) return "token";
        if (!data.begin(destination.server.getTickCount())) return "busy";
        try {
            CaptureRecord next = recovery ? record.recover(UUID.randomUUID()) : record.reissue(UUID.randomUUID());
            if (!data.commit(next)) return "persistence";
            if (recovery) CompanionLifecycleAuthority.retire(destination.server, animal);
            destination.getInventory().setItem(slot, CaptureToken.item(next)); destination.inventoryMenu.broadcastChanges();
            return null;
        } catch (RuntimeException failure) { return "generation"; }
        finally { data.end(); }
    }
    private static String quarantine(CaptureSavedData data, CaptureRecord record) {
        data.commit(record.phase(Phase.QUARANTINED, record.destination())); return "quarantine";
    }
    private static String location(Mob mob) { return mob.level().dimension().location() + " " + mob.blockPosition().toShortString(); }
}
