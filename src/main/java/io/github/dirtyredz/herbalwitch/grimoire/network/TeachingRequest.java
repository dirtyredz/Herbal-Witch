package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.UUID;

/**
 * Client to server, from an open reader: invite a nearby player to learn a recipe, accept or
 * decline the invitation addressed to this player, or withdraw one's own. The sender is always the
 * connection's player; {@code other} names the recipient (invite) or the teacher (accept, decline)
 * and is revalidated, so naming someone else changes nothing.
 *
 * @param containerId the reader menu's id
 * @param session     the reader session nonce
 * @param revision    the content revision the client is looking at
 * @param op          what to do
 * @param recipe      the recipe (invite, accept)
 * @param other       the other player (invite, accept, decline)
 */
public record TeachingRequest(int containerId, long session, int revision, Op op, Optional<EntryKey> recipe,
                              Optional<UUID> other) {
    public enum Op {
        INVITE,
        ACCEPT,
        DECLINE,
        CANCEL
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeLong(session);
        buf.writeVarInt(revision);
        PayloadBounds.writeEnum(buf, op);
        GrimoireCodec.writeOptionalKey(buf, recipe);
        buf.writeBoolean(other.isPresent());
        other.ifPresent(buf::writeUUID);
    }

    public static TeachingRequest decode(FriendlyByteBuf buf) {
        return new TeachingRequest(buf.readVarInt(), buf.readLong(), buf.readVarInt(), PayloadBounds.readEnum(buf, Op.class),
                GrimoireCodec.readOptionalKey(buf), buf.readBoolean() ? Optional.of(buf.readUUID()) : Optional.empty());
    }
}
