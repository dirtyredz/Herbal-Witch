package io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.util.UUID;

/** One monotonic player marker, not a roster. Never replays or refunds an interrupted transfer. */
public final class FamiliarPlayerReceipt {
    public static final String KEY = "herbalwitch_familiar_receipt";
    // Forge's remapping helper resolves the pinned 1.20.1 protected single-player save in both dev and production.
    private static final java.lang.reflect.Method SAVE = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod(
            net.minecraft.server.players.PlayerList.class, "m_6765_", ServerPlayer.class);
    private FamiliarPlayerReceipt() {}
    public static boolean pending(ServerPlayer player) { return player.getPersistentData().getCompound(KEY).getBoolean("pending"); }
    public static boolean begin(ServerPlayer player, FamiliarStore store, UUID bond) {
        if (pending(player)) return false;
        var r = store.record(bond).orElseThrow();
        long previous = player.getPersistentData().getCompound(KEY).getLong("sequence");
        if (previous < 0 || previous == Long.MAX_VALUE) return false;
        CompoundTag marker = new CompoundTag(); marker.putInt("schema", 1); marker.putLong("sequence", previous + 1);
        marker.putUUID("bond", bond); marker.putUUID("transaction", r.transaction()); marker.putBoolean("pending", true);
        if (!store.update(r.change(s -> s.put("receipt", marker.copy())), true)) return false;
        player.getPersistentData().put(KEY, marker);
        return save(player, marker);
    }
    /** Called after the paid change and practice. Both sides remain disabled until verified. */
    public static boolean complete(ServerPlayer player, FamiliarStore store, UUID bond, boolean compactPayload) {
        var r = store.record(bond).orElseThrow();
        CompoundTag marker = player.getPersistentData().getCompound(KEY);
        if (!marker.getBoolean("pending") || !marker.hasUUID("bond") || !marker.getUUID("bond").equals(bond)
                || !marker.equals(r.state().getCompound("receipt"))) return false;
        marker = marker.copy(); marker.putBoolean("pending", false); player.getPersistentData().put(KEY, marker);
        if (!save(player, marker)) return false;
        var next = r.change(s -> s.remove("receipt"));
        if (compactPayload) next = next.transition(next.phase(), next.generation(), next.transaction(), new CompoundTag());
        return store.update(next, true);
    }
    private static boolean save(ServerPlayer player, CompoundTag marker) {
        try {
            SAVE.invoke(player.server.getPlayerList(), player);
            var path = player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat");
            if (!Files.isRegularFile(path)) return false;
            try (var channel = java.nio.channels.FileChannel.open(path, java.nio.file.StandardOpenOption.WRITE)) { channel.force(true); }
            var saved = NbtIo.readCompressed(path.toFile());
            return marker.equals(saved.getCompound("ForgeData").getCompound(KEY));
        } catch (Exception failure) {
            org.slf4j.LoggerFactory.getLogger(FamiliarPlayerReceipt.class).error("Familiar player receipt not verified for {}", player.getUUID(), failure);
            return false;
        }
    }
    public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !pending(player)) return;
        var marker = player.getPersistentData().getCompound(KEY);
        if (!marker.hasUUID("bond")) return;
        var store = FamiliarStore.get(player.server);
        store.record(marker.getUUID("bond")).filter(r -> r.owner().equals(player.getUUID())).ifPresent(r -> store.update(r.quarantine(), true));
    }
    public static void clonePlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(KEY)) event.getEntity().getPersistentData().put(KEY, event.getOriginal().getPersistentData().getCompound(KEY).copy());
    }
}
