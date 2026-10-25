package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.platform.persistence.PlayerFileCheckpoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Makes a player's actual inventory and commerce receipt durable through vanilla player storage and
 * proves it: invoke the player save, force the file, read it back and compare the receipt. A dedicated
 * server's authority is {@code playerdata/<uuid>.dat}; the integrated host's is the {@code Player} tag
 * of {@code level.dat}, so the host's checkpoint also writes and verifies level data (a forced world
 * data save at the transfer boundary, accepted for correctness). A save method that returns without
 * error is never treated as proof. No private inventory file exists.
 */
public final class ShopPlayerCheckpoint {
    // GameTest-only fault/crash seam (installed by the separate test source set): told "before_save" and "after_save".
    static final ThreadLocal<java.util.function.Consumer<String>> FAULT = new ThreadLocal<>();

    /** What a saved player file proves: its receipt and its main-inventory slots (normalized item NBT). */
    public record Endpoint(boolean readable, ShopPlayerReceipt.Marker marker, Map<Integer, CompoundTag> slots) {
        public static final Endpoint UNREADABLE = new Endpoint(false, null, Map.of());

        public CompoundTag slot(int index) {
            return slots.getOrDefault(index, new CompoundTag());
        }
    }

    private ShopPlayerCheckpoint() {
    }

    /** Writes, forces, reads back and verifies the player's current marker. False on any doubt. */
    public static boolean save(ServerPlayer player) {
        CompoundTag expected = player.getPersistentData().getCompound(ShopPlayerReceipt.KEY).copy();
        try {
            var fault = FAULT.get();
            return PlayerFileCheckpoint.save(player,
                    saved -> expected.equals(saved.getCompound("ForgeData").getCompound(ShopPlayerReceipt.KEY)),
                    stage -> { if (fault != null) fault.accept(stage); });
        } catch (Exception failure) {
            org.slf4j.LoggerFactory.getLogger(ShopPlayerCheckpoint.class).error("Commerce player checkpoint not verified for {}", player.getUUID(), failure);
            return false;
        }
    }

    /** The live player's endpoint (authoritative only right after login or a verified checkpoint). */
    public static Endpoint live(ServerPlayer player) {
        ShopPlayerReceipt.Marker marker = ShopPlayerReceipt.of(player).orElse(null);
        Map<Integer, CompoundTag> slots = new HashMap<>();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) slots.put(i, stack.save(new CompoundTag()));
        }
        return new Endpoint(marker != null, marker, Map.copyOf(slots));
    }

    /** An offline player's saved endpoint, honouring the integrated host's level.dat precedence. */
    public static Endpoint saved(MinecraftServer server, UUID player) {
        try {
            return endpoint(PlayerFileCheckpoint.saved(server.getWorldPath(LevelResource.ROOT), player));
        } catch (IOException | RuntimeException unreadable) {
            return Endpoint.UNREADABLE;
        }
    }

    static Endpoint endpoint(CompoundTag data) {
        ShopPlayerReceipt.Marker marker = ShopPlayerReceipt.Marker.load(data.getCompound("ForgeData").getCompound(ShopPlayerReceipt.KEY));
        Map<Integer, CompoundTag> slots = new HashMap<>();
        ListTag inventory = data.getList("Inventory", Tag.TAG_COMPOUND);
        for (Tag tag : inventory) {
            CompoundTag entry = (CompoundTag) tag;
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 36) continue;
            ItemStack stack = ItemStack.of(entry);
            if (!stack.isEmpty()) slots.put(slot, stack.save(new CompoundTag()));
        }
        return new Endpoint(marker != null, marker, Map.copyOf(slots));
    }

}
