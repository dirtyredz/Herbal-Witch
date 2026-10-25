package io.github.dirtyredz.herbalwitch.platform.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

/** Vanilla player-file durability mechanics. Features own marker schemas and failure policy. */
public final class PlayerFileCheckpoint {
    private static final int MAX_FILE = 4 * 1024 * 1024;

    // Lazy so filesystem-only verification does not require Forge's runtime remapper.
    private static final class PlayerSave {
        private static final Method SAVE = ObfuscationReflectionHelper.findMethod(PlayerList.class, "m_6765_", ServerPlayer.class);
    }

    private PlayerFileCheckpoint() { }

    /** Save, force and read the actual login authority. The predicate receives the saved player NBT. */
    public static boolean save(ServerPlayer player, Predicate<CompoundTag> marker) throws ReflectiveOperationException, IOException {
        return save(player, marker, stage -> { });
    }

    /** Observer preserves the commerce fault/crash seam around the protected vanilla player save. */
    public static boolean save(ServerPlayer player, Predicate<CompoundTag> marker, Consumer<String> observer)
            throws ReflectiveOperationException, IOException {
        MinecraftServer server = player.server;
        observer.accept("before_save");
        PlayerSave.SAVE.invoke(server.getPlayerList(), player);
        observer.accept("after_save");
        if (server.isSingleplayerOwner(player.getGameProfile())) {
            LevelStorageSource.LevelStorageAccess access = ObfuscationReflectionHelper.getPrivateValue(
                    MinecraftServer.class, server, "f_129744_");
            if (access == null) return false;
            CompoundTag host = server.getPlayerList().getSingleplayerData();
            if (host == null) return false;
            // level.dat's Player wins over playerdata at the next integrated-host login.
            access.saveDataTag(server.registryAccess(), server.getWorldData(), host);
            return verify(access.getLevelPath(LevelResource.LEVEL_DATA_FILE), player.getUUID(), marker);
        }
        return verify(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat"), null, marker);
    }

    /** A non-null host selects Data.Player and requires the exact player identity; never falls back. */
    static boolean verify(Path file, UUID host, Predicate<CompoundTag> marker) throws IOException {
        if (!Files.isRegularFile(file)) throw new IOException("Missing saved player data " + file);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
        CompoundTag saved = read(file);
        if (host != null) {
            saved = saved.getCompound("Data").getCompound("Player");
            if (!saved.hasUUID("UUID") || !host.equals(saved.getUUID("UUID"))) return false;
        }
        return marker.test(saved);
    }

    /** Offline recovery follows vanilla host precedence, including a stale host receipt. */
    public static CompoundTag saved(Path world, UUID player) throws IOException {
        Path level = world.resolve("level.dat");
        if (Files.exists(level)) {
            CompoundTag data = read(level).getCompound("Data");
            if (data.contains("Player")) {
                if (!data.contains("Player", Tag.TAG_COMPOUND)) throw new IOException("Malformed host player data");
                CompoundTag host = data.getCompound("Player");
                if (!host.hasUUID("UUID")) throw new IOException("Missing host player identity");
                if (player.equals(host.getUUID("UUID"))) return host;
            }
        }
        return read(world.resolve("playerdata").resolve(player + ".dat"));
    }

    private static CompoundTag read(Path path) throws IOException {
        if (Files.size(path) > MAX_FILE) throw new IOException("Player data too large");
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            return NbtIo.read(input, new NbtAccounter(16L * 1024 * 1024));
        }
    }

    /** Death/End clone transfers only the feature's marker, without aliasing its mutable NBT. */
    public static void copy(Player original, Player replacement, String key) {
        copy(original.getPersistentData(), replacement.getPersistentData(), key);
    }

    static void copy(CompoundTag original, CompoundTag replacement, String key) {
        if (original.contains(key)) replacement.put(key, original.getCompound(key).copy());
    }
}
