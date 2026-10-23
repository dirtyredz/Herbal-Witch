package io.github.dirtyredz.herbalwitch.companions.familiars.bond.data;

import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;

/** Server-thread shard address book. Incomplete or corrupt indexes never prove absence. */
public final class FamiliarStore implements AutoCloseable {
    private static final Map<MinecraftServer, FamiliarStore> WORLDS = new WeakHashMap<>();
    private final Path directory;
    private final Map<UUID, FamiliarSavedData> shards = new HashMap<>();
    private final FamiliarRosterIndex owners = new FamiliarRosterIndex();
    private final FamiliarEntityIndex creatures = new FamiliarEntityIndex();
    private final BiConsumer<String, FamiliarSavedData> register;
    private DirectoryStream<Path> stream;
    private Iterator<Path> pending;
    private boolean indexed, corrupt, transaction;
    private long nextTransaction;
    public FamiliarStore(Path directory, BiConsumer<String, FamiliarSavedData> register) {
        this.directory = directory; this.register = register;
        try {
            Files.createDirectories(directory);
            stream = Files.newDirectoryStream(directory, FamiliarSavedData.PREFIX + "*.dat"); pending = stream.iterator();
        } catch (IOException failure) { corrupt = true; indexed = true; }
    }
    public static FamiliarStore get(MinecraftServer server) {
        return WORLDS.computeIfAbsent(server, s -> new FamiliarStore(s.getWorldPath(LevelResource.ROOT).resolve("data"),
                (name, data) -> s.overworld().getDataStorage().set(name, data)));
    }
    public static void stopped(MinecraftServer server) { var store = WORLDS.remove(server); if (store != null) store.close(); }
    public boolean indexed() { return indexed; }
    public boolean ready() { return indexed && !corrupt; }
    public FamiliarRosterIndex roster() { return owners; }
    /** At most 32 shards per tick, including malformed files. No complete directory materialization. */
    public int indexBatch() {
        if (indexed) return 0;
        int processed = 0;
        try {
            while (processed < 32 && pending.hasNext()) {
                Path path = pending.next(); processed++;
                try {
                    String file = path.getFileName().toString();
                    UUID id = UUID.fromString(file.substring(FamiliarSavedData.PREFIX.length(), file.length() - 4));
                    var data = new FamiliarSavedData(directory, id, new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT)); data.load();
                    if (!creatures.add(data.record()) || shards.putIfAbsent(id, data) != null) throw new IOException("Duplicate familiar identity");
                    owners.changed(data.record()); register.accept(FamiliarSavedData.name(id), data);
                    if (data.isDirty() && !data.checkpoint(FamiliarCodec.MAX_BYTES)) corrupt = true;
                } catch (Exception failure) {
                    corrupt = true;
                    org.slf4j.LoggerFactory.getLogger(FamiliarStore.class).error("Cannot index familiar shard {}; preserving it and blocking mutations", path, failure);
                }
            }
            if (!pending.hasNext()) { indexed = true; close(); }
        } catch (DirectoryIteratorException failure) { corrupt = true; indexed = true; close(); }
        return processed;
    }
    public Optional<FamiliarRecord> record(UUID bond) { var shard = shards.get(bond); return shard == null ? Optional.empty() : Optional.ofNullable(shard.record()); }
    public Optional<FamiliarRecord> creature(UUID creature) { return creatures.bond(creature).flatMap(this::record); }
    public boolean writable(UUID bond) { var shard = shards.get(bond); return ready() && shard != null && shard.writable(); }
    public boolean create(FamiliarRecord record) {
        if (!ready() || shards.containsKey(record.bond()) || creatures.bond(record.creature()).isPresent()
                || Files.exists(directory.resolve(FamiliarSavedData.name(record.bond()) + ".dat"))) return false;
        var shard = new FamiliarSavedData(directory, record.bond(), new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT));
        // Validate before adding derived entries; write uncertainty must still retain the identity.
        if (!shard.put(record, false, FamiliarCodec.MAX_BYTES)) return false;
        shards.put(record.bond(), shard); creatures.add(record); owners.changed(record);
        register.accept(FamiliarSavedData.name(record.bond()), shard);
        return shard.checkpoint(FamiliarCodec.MAX_BYTES);
    }
    public boolean update(FamiliarRecord next, boolean checkpoint) {
        if (!ready()) return false;
        var shard = shards.get(next.bond()); if (shard == null) return false;
        long previous = shard.record().revision();
        boolean success = shard.put(next, checkpoint, FamiliarCodec.MAX_BYTES);
        if (shard.record().revision() != previous) owners.changed(shard.record());
        return success;
    }
    public boolean begin(long tick) {
        if (!ready() || transaction || tick < nextTransaction) return false;
        transaction = true; nextTransaction = tick + 5; return true;
    }
    public void end() { transaction = false; }
    @Override public void close() {
        if (stream != null) { try { stream.close(); } catch (IOException failure) { corrupt = true; } stream = null; }
    }
}
