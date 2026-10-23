package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotEnvelope;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * One shop shard, {@code data/herbalwitch_shop_<uuid>.dat}, written only through the shared verified
 * writer (temp, force, atomic replace, read-back) for both explicit checkpoints and ordinary saves.
 * Vanilla's deferred save is never a checkpoint. A failed write leaves the shard read-only with its
 * dirty state retained; an unreadable or unsupported file is preserved and never replaced.
 */
public final class ShopSavedData extends SavedData {
    public static final SnapshotEnvelope FORMAT = new SnapshotEnvelope(0x48575348, 1);
    private final Path path;
    private final UUID id;
    private final VerifiedSnapshotWriter writer;
    private ShopRecord record;
    private boolean uncertain;
    private String problem;

    public ShopSavedData(Path directory, UUID id, VerifiedSnapshotWriter writer) {
        this.path = directory.resolve(ShopCodec.name(id) + ".dat");
        this.id = id;
        this.writer = writer;
    }

    public UUID id() {
        return id;
    }

    public Path path() {
        return path;
    }

    public ShopRecord record() {
        return record;
    }

    public boolean writable() {
        return !uncertain && record != null;
    }

    /** Why the shard is unavailable, or null. */
    public String problem() {
        return problem;
    }

    public void load() throws IOException {
        try {
            record = ShopCodec.decode(writer.read(path, ShopCodec.MAX_BYTES));
            if (!record.id.equals(id)) throw new IOException("Shop shard identity mismatch");
        } catch (IOException | RuntimeException failure) {
            record = null;
            uncertain = true;
            problem = failure instanceof ShopCodec.UnsupportedSchema ? "unsupported" : "unreadable";
            throw failure;
        }
    }

    /** Swaps in {@code next} when it is the same shop at a newer revision and fits; optionally checkpoints. */
    public boolean put(ShopRecord next, boolean checkpoint) {
        if (uncertain || !next.id.equals(id)) return false;
        if (record != null && (!record.owner.equals(next.owner) || next.revision <= record.revision)) return false;
        try {
            ShopCodec.encode(next);
        } catch (IOException | RuntimeException invalid) {
            return false;
        }
        record = next;
        setDirty();
        return !checkpoint || checkpoint();
    }

    public boolean checkpoint() {
        if (uncertain || record == null) return false;
        try {
            writer.write(path, ShopCodec.encode(record), ShopCodec.MAX_BYTES);
            setDirty(false);
            return true;
        } catch (IOException | RuntimeException failure) {
            uncertain = true;
            problem = "write_failed";
            setDirty();
            org.slf4j.LoggerFactory.getLogger(ShopSavedData.class).error("Shop shard is read-only; preserve {}", path, failure);
            return false;
        }
    }

    @Override
    public void save(File ignored) {
        if (isDirty() && !uncertain) checkpoint();
    }

    @Override
    public CompoundTag save(CompoundTag ignored) {
        throw new UnsupportedOperationException("Shop shard requires the verified writer");
    }
}
