package io.github.dirtyredz.herbalwitch.magic.ward.persistence;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Every ward record of the world, in the Overworld's {@code data/herbalwitch_wards.dat}, keyed by record
 * id and dimension-qualified: the sole owner of ward charge, ownership and settings (no duplicate
 * counters anywhere). Every mutation marks it dirty. An unsupported saved schema is kept unchanged and
 * disables every ward ({@link #writable}).
 */
public final class WardSavedData extends SavedData {
    public static final String NAME = "herbalwitch_wards";

    private final WardCodec.Loaded loaded;
    private long nextId;

    private WardSavedData(WardCodec.Loaded loaded) {
        this.loaded = loaded;
        this.nextId = loaded.nextId();
    }

    public static WardSavedData create() {
        return new WardSavedData(WardCodec.read(null));
    }

    public static WardSavedData load(CompoundTag tag) {
        return new WardSavedData(WardCodec.read(tag));
    }

    /** The world's ward records (stored with the Overworld). */
    public static WardSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(WardSavedData::load, WardSavedData::create, NAME);
    }

    public static WardSavedData get(ServerLevel level) {
        return get(level.getServer());
    }

    public boolean writable() {
        return loaded.unsupported().isEmpty();
    }

    public Optional<WardRecord> record(long id) {
        return Optional.ofNullable(loaded.records().get(id));
    }

    public List<WardRecord> records() {
        return List.copyOf(loaded.records().values());
    }

    public int size() {
        return loaded.records().size();
    }

    /** A new, uncharged, unbound record for a stone at {@code pos}; empty when full or disabled. */
    public Optional<WardRecord> create(Level level, BlockPos pos, int radius) {
        if (!writable() || loaded.records().size() >= WardCatalog.MAX_RECORDS) {
            return Optional.empty();
        }
        WardRecord record = new WardRecord(nextId++, level.dimension().location().toString(), pos.asLong(), null, radius,
                WardCatalog.Profile.BOTH, 0);
        loaded.records().put(record.id(), record);
        setDirty();
        return Optional.of(record);
    }

    /** Retires a record (its stone was broken); returns whether it existed. */
    public boolean retire(long id) {
        boolean removed = loaded.records().remove(id) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public void charge(WardRecord record, int ticks) {
        record.setCharge(ticks);
        setDirty();
    }

    public void bind(WardRecord record, UUID owner) {
        record.setOwner(owner);
        setDirty();
    }

    public void radius(WardRecord record, int radius) {
        record.setRadius(radius);
        setDirty();
    }

    public void profile(WardRecord record, WardCatalog.Profile profile) {
        record.setProfile(profile);
        setDirty();
    }

    /** Records bound to {@code owner}. */
    public long boundTo(UUID owner) {
        return loaded.records().values().stream().filter(r -> r.ownedBy(owner)).count();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return WardCodec.write(new WardCodec.Loaded(loaded.records(), nextId, loaded.preserved(), loaded.unsupported()));
    }
}
