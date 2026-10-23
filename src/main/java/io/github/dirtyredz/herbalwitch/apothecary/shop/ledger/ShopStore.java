package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Server-thread address book of shop shards. Shards are indexed in batches of 32 per tick before any
 * commerce mutation is enabled; until then, and for unreadable shards, absence is never assumed. Owner,
 * register and fixture indexes are derived from validated records only.
 */
public final class ShopStore implements AutoCloseable {
    private static final Map<MinecraftServer, ShopStore> WORLDS = new WeakHashMap<>();
    private final Path directory;
    private final Map<UUID, ShopSavedData> shards = new HashMap<>();
    private final ShopIndexes indexes = new ShopIndexes();
    private final BiConsumer<String, ShopSavedData> register;
    private Consumer<ShopSavedData> loaded = data -> { };
    private DirectoryStream<Path> stream;
    private Iterator<Path> pending;
    private boolean indexed, corrupt;

    public ShopStore(Path directory, BiConsumer<String, ShopSavedData> register) {
        this.directory = directory;
        this.register = register;
        try {
            Files.createDirectories(directory);
            stream = Files.newDirectoryStream(directory, "herbalwitch_shop_*.dat");
            pending = stream.iterator();
        } catch (IOException failure) {
            corrupt = true;
            indexed = true;
        }
    }

    public static ShopStore get(MinecraftServer server) {
        return WORLDS.computeIfAbsent(server, s -> new ShopStore(s.getWorldPath(LevelResource.ROOT).resolve("data"),
                (name, data) -> s.overworld().getDataStorage().set(name, data)));
    }

    public static Optional<ShopStore> existing(MinecraftServer server) {
        return Optional.ofNullable(WORLDS.get(server));
    }

    public static void stopped(MinecraftServer server) {
        ShopStore store = WORLDS.remove(server);
        if (store != null) store.close();
    }

    /** Called for each shard as it is indexed (restart reconciliation runs here). */
    public void onLoaded(Consumer<ShopSavedData> listener) {
        loaded = listener;
    }

    public boolean ready() {
        return indexed && !corrupt;
    }

    public boolean indexed() {
        return indexed;
    }

    public ShopIndexes indexes() {
        return indexes;
    }

    public Path directory() {
        return directory;
    }

    /** At most 32 shard files per call, malformed ones included; never materializes the directory. */
    public int indexBatch() {
        if (indexed) return 0;
        int processed = 0;
        try {
            while (processed < 32 && pending.hasNext()) {
                Path path = pending.next();
                processed++;
                String file = path.getFileName().toString();
                UUID id;
                try {
                    id = UUID.fromString(file.substring("herbalwitch_shop_".length(), file.length() - 4));
                } catch (IllegalArgumentException notAShard) {
                    continue;
                }
                ShopSavedData data = new ShopSavedData(directory, id, new VerifiedSnapshotWriter(ShopSavedData.FORMAT));
                shards.put(id, data);
                try {
                    data.load();
                    indexes.changed(null, data.record());
                    register.accept(ShopCodec.name(id), data);
                    loaded.accept(data);
                } catch (Exception failure) {
                    indexes.unavailable(id, data.problem() == null ? "unreadable" : data.problem());
                    org.slf4j.LoggerFactory.getLogger(ShopStore.class).error("Shop shard {} unavailable; preserved untouched", path, failure);
                }
            }
            if (!pending.hasNext()) {
                indexed = true;
                close();
            }
        } catch (DirectoryIteratorException failure) {
            corrupt = true;
            indexed = true;
            close();
        }
        return processed;
    }

    public Optional<ShopRecord> record(UUID id) {
        ShopSavedData shard = shards.get(id);
        return shard == null ? Optional.empty() : Optional.ofNullable(shard.record());
    }

    /** Whether the id names a shard this store knows (readable or not); unknown ids are never fresh shops. */
    public boolean known(UUID id) {
        return shards.containsKey(id);
    }

    public Optional<String> problem(UUID id) {
        ShopSavedData shard = shards.get(id);
        if (shard == null) return Optional.empty();
        return Optional.ofNullable(shard.problem());
    }

    public boolean writable(UUID id) {
        ShopSavedData shard = shards.get(id);
        return ready() && shard != null && shard.writable();
    }

    public Collection<ShopRecord> records() {
        return shards.values().stream().map(ShopSavedData::record).filter(java.util.Objects::nonNull).toList();
    }

    /** Creates and checkpoints a new shard; an existing file or identity is never reused. */
    public boolean create(ShopRecord record) {
        if (!ready() || shards.containsKey(record.id) || Files.exists(directory.resolve(ShopCodec.name(record.id) + ".dat"))) return false;
        ShopSavedData shard = new ShopSavedData(directory, record.id, new VerifiedSnapshotWriter(ShopSavedData.FORMAT));
        if (!shard.put(record, false)) return false;
        shards.put(record.id, shard);
        indexes.changed(null, record);
        register.accept(ShopCodec.name(record.id), shard);
        return shard.checkpoint();
    }

    /** Swaps in a newer revision of an existing shop and optionally checkpoints it. */
    public boolean update(ShopRecord next, boolean checkpoint) {
        if (!ready()) return false;
        ShopSavedData shard = shards.get(next.id);
        if (shard == null || shard.record() == null) return false;
        ShopRecord before = shard.record();
        boolean success = shard.put(next, checkpoint);
        if (shard.record() != before) indexes.changed(before, shard.record());
        return success;
    }

    /** Startup reconciliation only: replaces the loaded record before indexing completes. */
    public boolean reconcile(ShopSavedData shard, ShopRecord next) {
        ShopRecord before = shard.record();
        boolean success = shard.put(next, true);
        if (shard.record() != before) indexes.changed(before, shard.record());
        return success;
    }

    public List<UUID> ids() {
        return List.copyOf(shards.keySet());
    }

    @Override
    public void close() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException failure) {
                corrupt = true;
            }
            stream = null;
        }
    }
}
