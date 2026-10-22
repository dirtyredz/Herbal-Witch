package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.UUID;

/**
 * Server to one client: the invitation addressed to this player (incoming) or sent by them
 * (outgoing), or that it is gone. Only the two players involved ever receive it.
 *
 * @param kind      which invitation
 * @param other     the teacher (incoming) or recipient (outgoing)
 * @param otherName their name at the time of the invitation
 * @param recipe    the recipe offered
 * @param ticksLeft server ticks until it expires
 */
public record TeachingUpdate(Kind kind, Optional<UUID> other, String otherName, Optional<EntryKey> recipe, int ticksLeft) {
    public enum Kind {
        INCOMING,
        OUTGOING,
        INCOMING_CLEARED,
        OUTGOING_CLEARED
    }

    public TeachingUpdate {
        otherName = otherName.length() > 16 ? otherName.substring(0, 16) : otherName;
    }

    public static TeachingUpdate cleared(Kind kind) {
        return new TeachingUpdate(kind, Optional.empty(), "", Optional.empty(), 0);
    }

    public void encode(FriendlyByteBuf buf) {
        PayloadBounds.writeEnum(buf, kind);
        buf.writeBoolean(other.isPresent());
        other.ifPresent(buf::writeUUID);
        buf.writeUtf(otherName, 16);
        GrimoireCodec.writeOptionalKey(buf, recipe);
        buf.writeVarInt(ticksLeft);
    }

    public static TeachingUpdate decode(FriendlyByteBuf buf) {
        Kind kind = PayloadBounds.readEnum(buf, Kind.class);
        Optional<UUID> other = buf.readBoolean() ? Optional.of(buf.readUUID()) : Optional.empty();
        return new TeachingUpdate(kind, other, PayloadBounds.readString(buf, 16), GrimoireCodec.readOptionalKey(buf),
                PayloadBounds.readCount(buf, 72_000));
    }
}
