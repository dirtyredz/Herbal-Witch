package io.github.dirtyredz.herbalwitch.companions.familiars.bond.data;

import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotEnvelope;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/** A single bond, with exactly the same writer for explicit checkpoints and ordinary saves. */
public final class FamiliarSavedData extends SavedData {
    public static final String PREFIX = "herbalwitch_familiar_";
    public static final SnapshotEnvelope FORMAT = new SnapshotEnvelope(0x48574639, 1);
    private final Path path;
    private final VerifiedSnapshotWriter writer;
    private FamiliarRecord record;
    private boolean uncertain;
    public FamiliarSavedData(Path directory, UUID bond, VerifiedSnapshotWriter writer) {
        path = directory.resolve(name(bond) + ".dat"); this.writer = writer;
    }
    public static String name(UUID bond) { return PREFIX + bond; }
    public FamiliarRecord record() { return record; }
    public boolean writable() { return !uncertain; }
    public void load() throws IOException {
        try {
            record = FamiliarCodec.decode(writer.read(path, FamiliarCodec.MAX_BYTES));
            if (!path.getFileName().toString().equals(name(record.bond()) + ".dat")) throw new IOException("Familiar shard identity mismatch");
            if (record.interrupted()) { record = record.quarantine(); setDirty(); }
        } catch (IOException | RuntimeException failure) { uncertain = true; throw failure; }
    }
    public boolean put(FamiliarRecord next, boolean checkpoint, int maximum) {
        if (uncertain) return false;
        if (record != null && (!record.bond().equals(next.bond()) || !record.owner().equals(next.owner()) || !record.creature().equals(next.creature())
                || !record.species().equals(next.species()) || next.revision() <= record.revision() || next.generation() < record.generation())) return false;
        try { FamiliarCodec.encode(next, maximum); } catch (IOException | RuntimeException failure) { return false; }
        record = next; setDirty(); return !checkpoint || checkpoint(maximum);
    }
    public boolean checkpoint(int maximum) {
        if (uncertain || record == null) return false;
        try { writer.write(path, FamiliarCodec.encode(record, maximum), maximum); setDirty(false); return true; }
        catch (IOException | RuntimeException failure) {
            uncertain = true; setDirty();
            org.slf4j.LoggerFactory.getLogger(FamiliarSavedData.class).error("Familiar shard is read-only; preserve {}", path, failure);
            return false;
        }
    }
    @Override public void save(File ignored) { if (isDirty() && !uncertain) checkpoint(FamiliarCodec.MAX_BYTES); }
    @Override public CompoundTag save(CompoundTag ignored) { throw new UnsupportedOperationException("Familiar shard requires verified writer"); }
}
