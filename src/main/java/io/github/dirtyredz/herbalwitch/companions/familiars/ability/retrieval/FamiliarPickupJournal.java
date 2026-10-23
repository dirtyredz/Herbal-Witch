package io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval;

import io.github.dirtyredz.herbalwitch.platform.persistence.*;
import net.minecraft.nbt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Immutable per-source retirement receipts. Collection cannot erase them or make an old chunk authoritative. */
public final class FamiliarPickupJournal {
    public static final int MAXIMUM = 128 * 1024;
    public static final SnapshotEnvelope FORMAT = new SnapshotEnvelope(0x48575049, 1);
    private final Path directory;
    private final VerifiedSnapshotWriter writer;
    private final Map<UUID, Boolean> cache = new LinkedHashMap<>();
    public FamiliarPickupJournal(Path directory) { this(directory, new VerifiedSnapshotWriter(FORMAT)); }
    public FamiliarPickupJournal(Path directory, VerifiedSnapshotWriter writer) { this.directory = directory; this.writer = writer; }
    private Path path(UUID entity) { return directory.resolve(entity + ".dat"); }
    private void remember(UUID id, boolean retired) {
        cache.remove(id); cache.put(id, retired);
        while (cache.size() > 4096) cache.remove(cache.keySet().iterator().next());
    }
    public boolean retired(UUID entity) {
        var known = cache.get(entity); if (known != null) return known;
        // A corrupt or unreadable receipt can never authorize a second source item.
        boolean retired = !Files.notExists(path(entity)); remember(entity, retired); return retired;
    }
    public boolean prepare(UUID entity, UUID bond, UUID transaction, CompoundTag source) {
        if (retired(entity) || !source.hasUUID("UUID") || !source.getUUID("UUID").equals(entity)) return false;
        var receipt = new CompoundTag(); receipt.putInt("schema", 1); receipt.putUUID("entity", entity);
        receipt.putUUID("bond", bond); receipt.putUUID("transaction", transaction); receipt.put("source", source.copy());
        remember(entity, true);
        try {
            io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarCodec.validate(receipt, 0);
            var bytes = new ByteArrayOutputStream(); NbtIo.write(receipt, new DataOutputStream(bytes));
            if (bytes.size() > MAXIMUM) return false;
            writer.write(path(entity), bytes.toByteArray(), MAXIMUM); return true;
        } catch (IOException | RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(FamiliarPickupJournal.class).error("Pickup retirement uncertain; preserve source receipt for {}", entity, failure); return false;
        }
    }
    public CompoundTag inspect(UUID entity) throws IOException {
        var input = new DataInputStream(new ByteArrayInputStream(writer.read(path(entity), MAXIMUM)));
        var receipt = NbtIo.read(input, new NbtAccounter(MAXIMUM * 8L));
        if (input.available() != 0 || receipt.getInt("schema") != 1 || !receipt.hasUUID("entity") || !receipt.getUUID("entity").equals(entity))
            throw new IOException("Invalid familiar pickup receipt");
        return receipt;
    }
}
