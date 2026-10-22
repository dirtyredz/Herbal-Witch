package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Server to client, for one open reader: either the server-computed ingredient counts of the
 * selected recipe (one line per requirement, in page order) or the players near enough to teach.
 *
 * @param containerId the reader menu's id
 * @param session     the reader session nonce
 * @param key         the recipe the counts are for (availability only)
 * @param lines       required and available per requirement (availability only)
 * @param nearby      players in teaching range (nearby only)
 */
public record ReaderUpdate(int containerId, long session, Kind kind, Optional<EntryKey> key, List<Line> lines,
                           List<Nearby> nearby) {
    public static final int MAX_LINES = 8;
    public static final int MAX_NEARBY = 32;

    public enum Kind {
        AVAILABILITY,
        NEARBY
    }

    /** One requirement: how many the recipe needs, how many the carried inventory can supply. */
    public record Line(int required, int available) {
        public int missing() {
            return Math.max(0, required - available);
        }
    }

    public record Nearby(UUID id, String name) {
        public Nearby {
            name = name.length() > 16 ? name.substring(0, 16) : name;
        }
    }

    public ReaderUpdate {
        lines = List.copyOf(lines);
        nearby = List.copyOf(nearby);
    }

    public static ReaderUpdate availability(int containerId, long session, EntryKey key, List<Line> lines) {
        return new ReaderUpdate(containerId, session, Kind.AVAILABILITY, Optional.of(key), lines, List.of());
    }

    public static ReaderUpdate nearby(int containerId, long session, List<Nearby> nearby) {
        return new ReaderUpdate(containerId, session, Kind.NEARBY, Optional.empty(), List.of(), nearby);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeLong(session);
        PayloadBounds.writeEnum(buf, kind);
        GrimoireCodec.writeOptionalKey(buf, key);
        buf.writeVarInt(lines.size());
        lines.forEach(l -> {
            buf.writeVarInt(l.required());
            buf.writeVarInt(l.available());
        });
        buf.writeVarInt(nearby.size());
        nearby.forEach(n -> {
            buf.writeUUID(n.id());
            buf.writeUtf(n.name(), 16);
        });
    }

    public static ReaderUpdate decode(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        long session = buf.readLong();
        Kind kind = PayloadBounds.readEnum(buf, Kind.class);
        Optional<EntryKey> key = GrimoireCodec.readOptionalKey(buf);
        int n = PayloadBounds.readCount(buf, MAX_LINES);
        List<Line> lines = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            lines.add(new Line(PayloadBounds.readCount(buf, 64 * 36), PayloadBounds.readCount(buf, 64 * 36)));
        }
        int m = PayloadBounds.readCount(buf, MAX_NEARBY);
        List<Nearby> nearby = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            nearby.add(new Nearby(buf.readUUID(), PayloadBounds.readString(buf, 16)));
        }
        return new ReaderUpdate(containerId, session, kind, key, lines, nearby);
    }
}
