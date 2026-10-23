package io.github.dirtyredz.herbalwitch.companions.familiars.bond.data;

import net.minecraft.nbt.*;
import java.io.*;
import java.util.Set;

/** Bounded uncompressed shard codec. Unknown schema/fields are refused, never silently discarded. */
public final class FamiliarCodec {
    public static final int MAX_BYTES = 128 * 1024;
    private static final Set<String> FIELDS = Set.of("schema", "bond", "owner", "creature", "species", "originalType", "generation", "phase", "transaction", "revision", "state", "payload");
    private FamiliarCodec() {}
    public static byte[] encode(FamiliarRecord r, int maximum) throws IOException {
        if (maximum < 1024 || maximum > MAX_BYTES) throw new IOException("Invalid familiar byte bound");
        CompoundTag root = new CompoundTag(); root.putInt("schema", 1);
        root.putUUID("bond", r.bond()); root.putUUID("owner", r.owner()); root.putUUID("creature", r.creature());
        root.putString("species", r.species()); root.putString("originalType", r.originalType());
        root.putLong("generation", r.generation()); root.putString("phase", r.phase().name());
        root.putUUID("transaction", r.transaction()); root.putLong("revision", r.revision());
        root.put("state", r.state()); root.put("payload", r.payload()); validate(root, 0);
        ByteArrayOutputStream out = new ByteArrayOutputStream() {
            @Override public synchronized void write(int b) { if (count >= maximum) throw new IllegalArgumentException("Familiar record exceeds bound"); super.write(b); }
            @Override public synchronized void write(byte[] b, int off, int len) { if ((long)count + len > maximum) throw new IllegalArgumentException("Familiar record exceeds bound"); super.write(b, off, len); }
        };
        NbtIo.write(root, new DataOutputStream(out)); return out.toByteArray();
    }
    public static FamiliarRecord decode(byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) throw new IOException("Familiar record exceeds hard bound");
        var in = new DataInputStream(new ByteArrayInputStream(bytes));
        CompoundTag root = NbtIo.read(in, new NbtAccounter(MAX_BYTES * 8L));
        try {
            if (in.available() != 0 || !root.getAllKeys().equals(FIELDS) || !root.contains("schema", Tag.TAG_INT) || root.getInt("schema") != 1)
                throw new IllegalArgumentException("Unsupported familiar schema");
            for (String id : Set.of("bond", "owner", "creature", "transaction")) if (!root.hasUUID(id)) throw new IllegalArgumentException("Missing familiar identity");
            for (String field : Set.of("generation", "revision")) if (!root.contains(field, Tag.TAG_LONG)) throw new IllegalArgumentException("Invalid familiar revision");
            for (String field : Set.of("state", "payload")) if (!root.contains(field, Tag.TAG_COMPOUND)) throw new IllegalArgumentException("Invalid familiar data");
            validate(root, 0);
            return new FamiliarRecord(root.getUUID("bond"), root.getUUID("owner"), root.getUUID("creature"), root.getString("species"), root.getString("originalType"),
                    root.getLong("generation"), FamiliarRecord.Phase.valueOf(root.getString("phase")), root.getUUID("transaction"), root.getLong("revision"), root.getCompound("state"), root.getCompound("payload"));
        } catch (RuntimeException ex) { throw new IOException("Malformed familiar record", ex); }
    }
    public static void validate(Tag tag, int depth) {
        if (depth > 16) throw new IllegalArgumentException("Familiar NBT too deep");
        if (tag instanceof CompoundTag compound) {
            if (compound.size() > 256) throw new IllegalArgumentException("Too many familiar fields");
            for (String key : compound.getAllKeys()) {
                if (key.length() > 256) throw new IllegalArgumentException("Familiar key too long");
                validate(compound.get(key), depth + 1);
            }
        } else if (tag instanceof CollectionTag<?> list) {
            if (list.size() > 256) throw new IllegalArgumentException("Familiar collection too long");
            for (Tag value : list) validate(value, depth + 1);
        } else if (tag instanceof StringTag text && text.getAsString().length() > 2048) throw new IllegalArgumentException("Familiar text too long");
        else if (tag instanceof NumericTag number && !Double.isFinite(number.getAsDouble())) throw new IllegalArgumentException("Non-finite familiar number");
    }
}
