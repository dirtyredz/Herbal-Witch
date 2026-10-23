package io.github.dirtyredz.herbalwitch.companions.animals.capture.data;

import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Sole world-global authority. Corrupt/unsupported files are preserved and never replaced by an empty ledger. */
public final class CaptureSavedData extends SavedData {
    public static final String NAME = "herbalwitch_captures";
    private static final Map<MinecraftServer, CaptureSavedData> WORLDS = new WeakHashMap<>();
    private final Map<UUID, CaptureRecord> records = new LinkedHashMap<>();
    private final CaptureCheckpoint writer;
    private final Path path;
    // uncertain: no further write may run (a failed write or an unreadable file); unreadable: the records themselves are unknown.
    private boolean uncertain, unreadable, transaction;
    private long nextTransaction;

    public CaptureSavedData(Path path, CaptureCheckpoint writer) { this.path = path; this.writer = writer; }
    public static synchronized CaptureSavedData get(MinecraftServer server) {
        return WORLDS.computeIfAbsent(server, key -> {
            var data = new CaptureSavedData(key.getWorldPath(LevelResource.ROOT).resolve("data").resolve(NAME + ".dat"), new CaptureCheckpoint());
            data.load();
            key.overworld().getDataStorage().set(NAME, data);
            return data;
        });
    }
    public static synchronized void reset() { WORLDS.clear(); }
    public synchronized void load() {
        if (!Files.exists(path)) return;
        try {
            records.putAll(CaptureCodec.decode(writer.read(path, 16 * 1024 * 1024)));
            boolean prepared = false;
            for (var entry : records.entrySet()) {
                var r = entry.getValue();
                if (r.phase() == CaptureRecord.Phase.CAPTURE_PREPARED || r.phase() == CaptureRecord.Phase.RELEASE_PREPARED
                        || r.phase() == CaptureRecord.Phase.RELEASED && r.storedPayload()) {
                    entry.setValue(r.phase(CaptureRecord.Phase.QUARANTINED, r.destination())); prepared = true;
                }
            }
            if (prepared) { setDirty(); checkpoint(); }
        } catch (Exception failure) {
            uncertain = true;
            unreadable = true;
            org.slf4j.LoggerFactory.getLogger(CaptureSavedData.class).error("Capture ledger is read-only; preserve its file for operator inspection", failure);
        }
    }
    public synchronized boolean writable() { return !uncertain; }
    /** Whether the records are known. A failed write locks writing but leaves every loaded record authoritative. */
    public synchronized boolean readable() { return !unreadable; }
    public synchronized Optional<CaptureRecord> record(UUID animal) { return Optional.ofNullable(records.get(animal)); }
    public synchronized Optional<CaptureRecord> token(UUID token) {
        if (token == null) return Optional.empty();
        return records.values().stream().filter(r -> token.equals(r.token())).findFirst();
    }
    public synchronized int size() { return records.size(); }
    /** After the verified release only; a crash before this cleanup is durable conservatively quarantines its retained payload. */
    public synchronized void compact(UUID animal) { records.put(animal, records.get(animal).compact()); setDirty(); }
    public synchronized boolean capacity(UUID animal, UUID captor) { return CaptureCodec.fits(records.values(), animal, captor, AnimalServerConfig.current()); }
    public synchronized boolean begin(long tick) {
        if (uncertain || transaction || tick < nextTransaction) return false;
        transaction = true; nextTransaction = tick + AnimalServerConfig.current().transactionInterval(); return true;
    }
    public synchronized void end() { transaction = false; }

    /** Size check precedes mutation. A failed write preserves dirty state and locks every subsequent writer. */
    public synchronized boolean commit(CaptureRecord record) {
        if (uncertain) return false;
        Map<UUID, CaptureRecord> next = new LinkedHashMap<>(records); next.put(record.animal(), record);
        byte[] bytes;
        try { bytes = CaptureCodec.encode(next.values(), AnimalServerConfig.current().snapshotBytes()); }
        catch (Exception failure) { return false; }
        records.put(record.animal(), record); setDirty();
        boolean success = writer.write(path, bytes, AnimalServerConfig.current().snapshotBytes());
        if (success) setDirty(false); else uncertain = true;
        return success;
    }
    public synchronized boolean checkpoint() {
        if (uncertain) return false;
        try {
            if (writer.write(path, CaptureCodec.encode(records.values(), AnimalServerConfig.current().snapshotBytes()), AnimalServerConfig.current().snapshotBytes())) {
                setDirty(false); return true;
            }
        } catch (Exception failure) { org.slf4j.LoggerFactory.getLogger(CaptureSavedData.class).error("Capture serialization failed", failure); }
        uncertain = true; setDirty(); return false;
    }
    @Override public synchronized void save(File ignored) { if (isDirty() && !uncertain) checkpoint(); }
    @Override public CompoundTag save(CompoundTag tag) { throw new UnsupportedOperationException("Capture ledger must use its verified file writer"); }
}
