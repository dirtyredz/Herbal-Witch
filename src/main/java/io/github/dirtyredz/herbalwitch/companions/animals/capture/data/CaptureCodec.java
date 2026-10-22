package io.github.dirtyredz.herbalwitch.companions.animals.capture.data;

import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import net.minecraft.nbt.*;
import java.io.*;
import java.util.*;

/** Strict whole-snapshot decoding. A malformed record blocks the ledger; it is never skipped. */
public final class CaptureCodec {
    private CaptureCodec() {}
    public static byte[] encode(Collection<CaptureRecord> records, int maximum) throws IOException {
        CompoundTag root = new CompoundTag(); root.putInt("schema", 1);
        ListTag entries = new ListTag();
        records.stream().sorted(Comparator.comparing(r -> r.animal().toString())).forEach(r -> entries.add(write(r)));
        root.put("records", entries);
        return bytes(root, maximum);
    }
    public static byte[] bytes(CompoundTag tag, int maximum) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream() {
            @Override public synchronized void write(int b) { if (count >= maximum) throw new IllegalArgumentException("NBT exceeds byte bound"); super.write(b); }
            @Override public synchronized void write(byte[] b, int off, int len) { if ((long) count + len > maximum) throw new IllegalArgumentException("NBT exceeds byte bound"); super.write(b, off, len); }
        };
        NbtIo.write(tag, new DataOutputStream(bytes)); return bytes.toByteArray();
    }
    public static Map<UUID, CaptureRecord> decode(byte[] bytes) throws IOException {
        if (bytes.length > 16 * 1024 * 1024) throw new IOException("Capture snapshot exceeds hard bound");
        var input = new DataInputStream(new ByteArrayInputStream(bytes));
        CompoundTag root = NbtIo.read(input, new NbtAccounter(64L * 1024 * 1024));
        if (input.available() != 0 || !root.getAllKeys().equals(Set.of("schema", "records")) || !root.contains("schema", Tag.TAG_INT)
                || root.getInt("schema") != 1 || !root.contains("records", Tag.TAG_LIST)) throw new IOException("Unsupported capture schema");
        ListTag entries = root.getList("records", Tag.TAG_COMPOUND);
        if (entries.size() != ((ListTag) root.get("records")).size() || entries.size() > 16384) throw new IOException("Invalid capture records");
        Map<UUID, CaptureRecord> records = new LinkedHashMap<>(); Set<UUID> tokens = new HashSet<>(); int payloads = 0;
        try {
            for (Tag tag : entries) {
                CaptureRecord r = read((CompoundTag) tag);
                if (records.putIfAbsent(r.animal(), r) != null || r.token() != null && !tokens.add(r.token())) throw new IllegalArgumentException("Duplicate capture identity");
                if (r.storedPayload() && ++payloads > 512) throw new IllegalArgumentException("Too many capture payloads");
            }
        } catch (RuntimeException failure) { throw new IOException("Malformed capture record", failure); }
        return records;
    }
    public static CompoundTag write(CaptureRecord r) {
        CompoundTag tag = new CompoundTag(); tag.putUUID("animal", r.animal()); tag.putLong("generation", r.generation());
        tag.putUUID("transaction", r.transaction()); tag.putString("phase", r.phase().name()); tag.putUUID("captor", r.captor());
        if (r.token() != null) tag.putUUID("token", r.token());
        tag.putString("type", r.type()); tag.putInt("adapter", r.adapter()); tag.putString("summary", r.summary());
        tag.putString("source", r.source()); tag.putString("destination", r.destination());
        if (r.storedPayload()) tag.put("payload", r.payload());
        return tag;
    }
    private static CaptureRecord read(CompoundTag tag) {
        Set<String> keys = new HashSet<>(Set.of("animal", "generation", "transaction", "phase", "captor", "type", "adapter", "summary", "source", "destination"));
        var phase = CaptureRecord.Phase.valueOf(tag.getString("phase"));
        if (phase != CaptureRecord.Phase.RELEASED) { keys.add("token"); keys.add("payload"); }
        else if (tag.contains("payload", Tag.TAG_COMPOUND)) keys.add("payload");
        if (!tag.getAllKeys().equals(keys) || !tag.hasUUID("animal") || !tag.hasUUID("transaction") || !tag.hasUUID("captor")
                || !tag.contains("generation", Tag.TAG_LONG) || !tag.contains("adapter", Tag.TAG_INT)) throw new IllegalArgumentException("Invalid record fields");
        for (String key : List.of("phase", "type", "summary", "source", "destination")) if (!tag.contains(key, Tag.TAG_STRING)) throw new IllegalArgumentException("Invalid record text");
        if (phase != CaptureRecord.Phase.RELEASED && (!tag.hasUUID("token") || !tag.contains("payload", Tag.TAG_COMPOUND))) throw new IllegalArgumentException("Missing capture payload");
        return new CaptureRecord(tag.getUUID("animal"), tag.getLong("generation"), tag.getUUID("transaction"), phase,
                tag.getUUID("captor"), tag.hasUUID("token") ? tag.getUUID("token") : null, tag.getString("type"), tag.getInt("adapter"),
                tag.getString("summary"), tag.getString("source"), tag.getString("destination"), tag.contains("payload", Tag.TAG_COMPOUND) ? tag.getCompound("payload") : null);
    }
    public static boolean fits(Collection<CaptureRecord> records, UUID newAnimal, UUID captor, AnimalRules rules) {
        int stored = 0, owned = 0; boolean existing = false;
        for (var r : records) {
            if (r.animal().equals(newAnimal)) { existing = true; if (r.phase() != CaptureRecord.Phase.RELEASED) return false; }
            if (r.storedPayload()) { stored++; if (r.captor().equals(captor)) owned++; }
        }
        return (existing || records.size() < rules.lifetime()) && stored < rules.storedWorld() && owned < rules.perCaptor();
    }
}
