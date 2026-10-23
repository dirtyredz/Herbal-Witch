package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;
import java.util.*;

/** Owner-only projection. No recovery NBT, owner UUIDs, receipts or canonical inventory on the wire. */
public record FamiliarRosterPage(int container, long nonce, long revision, List<Row> rows, boolean more, String notice) {
    public static final int MAX_ROWS = 32, MAX_BYTES = 24 * 1024;
    public record Row(UUID bond, String name, String species, int level, float health, float maximum, String command,
                      String phase, boolean loaded, String dimension, long position, String home, String status, String buffs, int reserve, boolean automatic) {
        public Row {
            if (reserve < 0 || reserve > 3000 || level < 1 || level > 5 || !Float.isFinite(health) || !Float.isFinite(maximum) || health < 0 || maximum < 0 || maximum > 1_000_000)
                throw new IllegalArgumentException("Invalid familiar summary");
        }
    }
    public FamiliarRosterPage {
        rows = List.copyOf(rows); if (rows.size() > MAX_ROWS || revision < 0) throw new IllegalArgumentException("Invalid familiar page");
    }
    public void encode(FriendlyByteBuf b) {
        int start = b.writerIndex(); b.writeVarInt(container); b.writeLong(nonce); b.writeLong(revision); b.writeVarInt(rows.size());
        for (var r : rows) {
            b.writeUUID(r.bond()); b.writeUtf(r.name(), 64); b.writeUtf(r.species(), 16); b.writeVarInt(r.level());
            b.writeFloat(r.health()); b.writeFloat(r.maximum()); b.writeUtf(r.command(), 16); b.writeUtf(r.phase(), 32); b.writeBoolean(r.loaded());
            b.writeUtf(r.dimension(), 96); b.writeLong(r.position()); b.writeUtf(r.home(), 128); b.writeUtf(r.status(), 96); b.writeUtf(r.buffs(), 128);
            b.writeVarInt(r.reserve()); b.writeBoolean(r.automatic());
        }
        b.writeBoolean(more); b.writeUtf(notice, 128);
        if (b.writerIndex() - start > MAX_BYTES) throw new IllegalArgumentException("Familiar page exceeds byte bound");
    }
    public static FamiliarRosterPage decode(FriendlyByteBuf b) {
        PayloadBounds.requireAtMost(b, MAX_BYTES);
        int container = b.readVarInt(); long nonce = b.readLong(), revision = b.readLong();
        int count = PayloadBounds.readCount(b, MAX_ROWS); List<Row> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) rows.add(new Row(b.readUUID(), b.readUtf(64), b.readUtf(16), b.readVarInt(), b.readFloat(), b.readFloat(),
                b.readUtf(16), b.readUtf(32), b.readBoolean(), b.readUtf(96), b.readLong(), b.readUtf(128), b.readUtf(96), b.readUtf(128), b.readVarInt(), b.readBoolean()));
        return new FamiliarRosterPage(container, nonce, revision, rows, b.readBoolean(), b.readUtf(128));
    }
}
