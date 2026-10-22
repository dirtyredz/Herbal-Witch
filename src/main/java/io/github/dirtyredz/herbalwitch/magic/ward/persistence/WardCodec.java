package io.github.dirtyredz.herbalwitch.magic.ward.persistence;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The ward records' saved format, schema {@value #SCHEMA}:
 * {@code {Schema, NextId, Records: [{Id, Dim, Pos, Owner?, Radius, Profile, Charge}], Preserved: [...]}}.
 * Pure. A file of another schema is kept byte for byte and every ward is disabled; a malformed record
 * is moved to {@code Preserved} (kept, never used) rather than dropped; more than
 * {@link WardCatalog#MAX_RECORDS} records keeps the first ones and preserves the rest.
 */
public final class WardCodec {
    public static final int SCHEMA = 1;

    /** What was read: the usable records, the next id, preserved entries and an unsupported whole file. */
    public record Loaded(Map<Long, WardRecord> records, long nextId, List<CompoundTag> preserved, Optional<CompoundTag> unsupported) {
    }

    private WardCodec() {
    }

    public static Loaded read(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return new Loaded(new LinkedHashMap<>(), 1, new ArrayList<>(), Optional.empty());
        }
        if (tag.getInt("Schema") != SCHEMA) {
            return new Loaded(new LinkedHashMap<>(), 1, new ArrayList<>(), Optional.of(tag.copy()));
        }
        Map<Long, WardRecord> records = new LinkedHashMap<>();
        List<CompoundTag> preserved = new ArrayList<>();
        ListTag list = tag.getList("Records", Tag.TAG_COMPOUND);
        long nextId = Math.max(1, tag.getLong("NextId"));
        for (int i = 0; i < list.size(); i++) {
            CompoundTag r = list.getCompound(i);
            Optional<WardRecord> record = record(r);
            if (record.isEmpty() || records.containsKey(record.get().id()) || records.size() >= WardCatalog.MAX_RECORDS) {
                preserved.add(r.copy());
                continue;
            }
            records.put(record.get().id(), record.get());
            nextId = Math.max(nextId, record.get().id() + 1);
        }
        ListTag kept = tag.getList("Preserved", Tag.TAG_COMPOUND);
        for (int i = 0; i < kept.size(); i++) {
            preserved.add(kept.getCompound(i).copy());
        }
        return new Loaded(records, nextId, preserved, Optional.empty());
    }

    private static Optional<WardRecord> record(CompoundTag r) {
        if (!r.contains("Id", Tag.TAG_LONG) || !r.contains("Dim", Tag.TAG_STRING) || !r.contains("Pos", Tag.TAG_LONG)
                || !r.contains("Radius", Tag.TAG_INT) || !r.contains("Profile", Tag.TAG_INT) || !r.contains("Charge", Tag.TAG_INT)) {
            return Optional.empty();
        }
        int profile = r.getInt("Profile");
        int radius = r.getInt("Radius");
        if (r.getLong("Id") <= 0 || profile < 0 || profile >= WardCatalog.Profile.values().length || radius < 1 || radius > 64
                || r.getInt("Charge") < 0) {
            return Optional.empty();
        }
        return Optional.of(new WardRecord(r.getLong("Id"), r.getString("Dim"), r.getLong("Pos"), r.hasUUID("Owner") ? r.getUUID("Owner") : null,
                radius, WardCatalog.Profile.values()[profile], r.getInt("Charge")));
    }

    public static CompoundTag write(Loaded loaded) {
        if (loaded.unsupported().isPresent()) {
            return loaded.unsupported().get().copy();
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", SCHEMA);
        tag.putLong("NextId", loaded.nextId());
        ListTag list = new ListTag();
        for (WardRecord record : loaded.records().values()) {
            CompoundTag r = new CompoundTag();
            r.putLong("Id", record.id());
            r.putString("Dim", record.dimension());
            r.putLong("Pos", record.pos());
            record.owner().ifPresent(o -> r.putUUID("Owner", o));
            r.putInt("Radius", record.radius());
            r.putInt("Profile", record.profile().ordinal());
            r.putInt("Charge", record.charge());
            list.add(r);
        }
        tag.put("Records", list);
        if (!loaded.preserved().isEmpty()) {
            ListTag kept = new ListTag();
            loaded.preserved().forEach(p -> kept.add(p.copy()));
            tag.put("Preserved", kept);
        }
        return tag;
    }
}
