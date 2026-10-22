package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

/**
 * Client to server, from an open reader: select the entry whose ingredients to count (or none), or
 * research one. Carries only intent and identities: the menu id, the server-issued session nonce
 * and the content revision the client is looking at. The server derives the player and every
 * authoritative value, and drops a request whose menu, session or revision is not current.
 *
 * @param containerId the reader menu's id
 * @param session     the reader session nonce
 * @param revision    the content revision the client built its pages from
 * @param op          what to do
 * @param key         the entry (empty to clear the selection)
 */
public record ReaderRequest(int containerId, long session, int revision, Op op, Optional<EntryKey> key) {
    public enum Op {
        SELECT,
        RESEARCH
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeLong(session);
        buf.writeVarInt(revision);
        PayloadBounds.writeEnum(buf, op);
        GrimoireCodec.writeOptionalKey(buf, key);
    }

    public static ReaderRequest decode(FriendlyByteBuf buf) {
        return new ReaderRequest(buf.readVarInt(), buf.readLong(), buf.readVarInt(), PayloadBounds.readEnum(buf, Op.class),
                GrimoireCodec.readOptionalKey(buf));
    }
}
