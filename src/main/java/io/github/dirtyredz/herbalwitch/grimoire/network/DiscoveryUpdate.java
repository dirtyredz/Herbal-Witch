package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.network.ChunkAssembly;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Server to one client: that player's own discoveries, never anyone else's. A snapshot (after
 * login, respawn or a dimension change) arrives in bounded chunks and replaces the client's set
 * when complete; a delta adds keys just granted. Keys only; the client resolves them against its
 * content.
 *
 * @param snapshot whether this is part of a full snapshot (else a delta)
 * @param header   the chunk header of a snapshot (ignored for a delta)
 * @param keys     at most {@link PayloadBounds#MAX_CHUNK_RECORDS} keys
 */
public record DiscoveryUpdate(boolean snapshot, ChunkAssembly.Header header, List<EntryKey> keys) {
    private static final ChunkAssembly.Header DELTA = new ChunkAssembly.Header(0, 0, 1, 0);

    public DiscoveryUpdate {
        keys = List.copyOf(keys);
    }

    /** A full snapshot in chunks. */
    public static List<DiscoveryUpdate> snapshot(int revision, List<EntryKey> keys) {
        List<List<EntryKey>> chunks = PayloadBounds.chunk(keys, k -> PayloadBounds.stringBytes(k.serialized()));
        List<DiscoveryUpdate> out = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            out.add(new DiscoveryUpdate(true, new ChunkAssembly.Header(revision, i, chunks.size(), keys.size()), chunks.get(i)));
        }
        return out;
    }

    /** Newly granted keys, in as many bounded messages as needed. */
    public static List<DiscoveryUpdate> delta(List<EntryKey> keys) {
        return PayloadBounds.chunk(keys, k -> PayloadBounds.stringBytes(k.serialized())).stream()
                .filter(c -> !c.isEmpty()).map(c -> new DiscoveryUpdate(false, DELTA, c)).toList();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(snapshot);
        if (snapshot) {
            GrimoireCodec.writeHeader(buf, header);
        }
        buf.writeVarInt(keys.size());
        keys.forEach(k -> GrimoireCodec.writeKey(buf, k));
    }

    public static DiscoveryUpdate decode(FriendlyByteBuf buf) {
        boolean snapshot = buf.readBoolean();
        ChunkAssembly.Header header = snapshot ? GrimoireCodec.readHeader(buf) : DELTA;
        int n = PayloadBounds.readCount(buf, PayloadBounds.MAX_CHUNK_RECORDS);
        List<EntryKey> keys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            keys.add(GrimoireCodec.readKey(buf));
        }
        return new DiscoveryUpdate(snapshot, header, keys);
    }
}
