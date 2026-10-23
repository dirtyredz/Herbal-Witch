package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * Makes a player's actual inventory and commerce receipt durable through vanilla player storage and
 * proves it: invoke the player save, force the file, read it back and compare the receipt. A dedicated
 * server's authority is {@code playerdata/<uuid>.dat}; the integrated host's is the {@code Player} tag
 * of {@code level.dat}, so the host's checkpoint also writes and verifies level data (a forced world
 * data save at the transfer boundary, accepted for correctness). A save method that returns without
 * error is never treated as proof. No private inventory file exists.
 */
public final class ShopPlayerCheckpoint {
    // The pinned 1.20.1 protected PlayerList#save(ServerPlayer), resolved by Forge's remapper in dev and production.
    private static final Method SAVE = ObfuscationReflectionHelper.findMethod(PlayerList.class, "m_6765_", ServerPlayer.class);
    private static final int MAX_FILE = 4 * 1024 * 1024;
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
        MinecraftServer server = player.server;
        try {
            var fault = FAULT.get();
            if (fault != null) fault.accept("before_save");
            SAVE.invoke(server.getPlayerList(), player);
            if (fault != null) fault.accept("after_save");
            if (server.isSingleplayerOwner(player.getGameProfile())) return host(server, player, expected);
            Path path = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat");
            force(path);
            CompoundTag saved = read(path);
            return expected.equals(saved.getCompound("ForgeData").getCompound(ShopPlayerReceipt.KEY));
        } catch (Exception failure) {
            org.slf4j.LoggerFactory.getLogger(ShopPlayerCheckpoint.class).error("Commerce player checkpoint not verified for {}", player.getUUID(), failure);
            return false;
        }
    }

    /** The integrated host: level.dat's Player tag wins over the player file at the next load. */
    private static boolean host(MinecraftServer server, ServerPlayer player, CompoundTag expected) throws IOException {
        LevelStorageSource.LevelStorageAccess access = ObfuscationReflectionHelper.getPrivateValue(MinecraftServer.class, server, "f_129744_");
        if (access == null) return false;
        CompoundTag hostData = server.getPlayerList().getSingleplayerData();
        if (hostData == null) return false;
        access.saveDataTag(server.registryAccess(), server.getWorldData(), hostData);
        Path level = access.getLevelPath(LevelResource.LEVEL_DATA_FILE);
        force(level);
        CompoundTag saved = read(level).getCompound("Data").getCompound("Player");
        return saved.hasUUID("UUID") && saved.getUUID("UUID").equals(player.getUUID())
                && expected.equals(saved.getCompound("ForgeData").getCompound(ShopPlayerReceipt.KEY));
    }

    private static void force(Path path) throws IOException {
        if (!Files.isRegularFile(path)) throw new IOException("Missing saved player data " + path);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    private static CompoundTag read(Path path) throws IOException {
        if (Files.size(path) > MAX_FILE) throw new IOException("Player data too large");
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            return NbtIo.read(input, new NbtAccounter(16L * 1024 * 1024));
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
            CompoundTag data = null;
            CompoundTag host = server.getWorldData().getLoadedPlayerTag();
            if (host != null && host.hasUUID("UUID") && host.getUUID("UUID").equals(player)) data = host;
            if (data == null) {
                Path path = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player + ".dat");
                if (!Files.isRegularFile(path)) return Endpoint.UNREADABLE;
                data = read(path);
            }
            return endpoint(data);
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
