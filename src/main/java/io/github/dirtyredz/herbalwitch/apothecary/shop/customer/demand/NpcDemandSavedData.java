package io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand;

import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotEnvelope;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The one Overworld demand record, {@code data/herbalwitch_npc_demand.dat}, through the verified writer
 * for debits and ordinary saves. A debit is durable before the sale it pays for commits. An unreadable
 * file is preserved and disables villager sales (no fresh budget is invented).
 */
public final class NpcDemandSavedData extends SavedData {
    public static final String NAME = "herbalwitch_npc_demand";
    public static final SnapshotEnvelope FORMAT = new SnapshotEnvelope(0x4857444D, 1);
    private final Path path;
    private final VerifiedSnapshotWriter writer = new VerifiedSnapshotWriter(FORMAT);
    private NpcDemand demand = new NpcDemand();
    private boolean uncertain;

    public NpcDemandSavedData(Path directory) {
        this.path = directory.resolve(NAME + ".dat");
    }

    public void load() {
        if (!Files.exists(path)) return;
        try {
            demand = NpcDemandCodec.decode(writer.read(path, NpcDemandCodec.MAX_BYTES));
        } catch (IOException | RuntimeException failure) {
            uncertain = true;
            org.slf4j.LoggerFactory.getLogger(NpcDemandSavedData.class).error("Villager demand record unreadable; villager sales disabled; preserve {}", path, failure);
        }
    }

    public NpcDemand demand() {
        return demand;
    }

    public boolean writable() {
        return !uncertain;
    }

    /** Applies a change to a copy and makes it durable; on failure the memory keeps the change (burned) and writes stop. */
    public boolean commit(NpcDemand next) {
        if (uncertain) return false;
        demand = next;
        setDirty();
        return checkpoint();
    }

    public void tick(long ticks, int window) {
        if (uncertain) return;
        demand.tick(ticks, window);
        setDirty();
    }

    public boolean checkpoint() {
        if (uncertain) return false;
        try {
            writer.write(path, NpcDemandCodec.encode(demand), NpcDemandCodec.MAX_BYTES);
            setDirty(false);
            return true;
        } catch (IOException | RuntimeException failure) {
            uncertain = true;
            org.slf4j.LoggerFactory.getLogger(NpcDemandSavedData.class).error("Villager demand record is read-only; preserve {}", path, failure);
            return false;
        }
    }

    @Override
    public void save(File ignored) {
        if (isDirty() && !uncertain) checkpoint();
    }

    @Override
    public CompoundTag save(CompoundTag ignored) {
        throw new UnsupportedOperationException("Demand record requires the verified writer");
    }
}
