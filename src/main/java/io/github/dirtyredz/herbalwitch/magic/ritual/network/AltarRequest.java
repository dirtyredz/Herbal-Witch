package io.github.dirtyredz.herbalwitch.magic.ritual.network;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.UUID;

/**
 * Intent from an open altar screen: which menu and server-issued session it comes from, the rules
 * it was shown, the operation, and the one id that operation names (a nearby player to invite, an
 * invitation to accept or decline). Never an inventory, a cost, a duration or a result. At most
 * {@link PayloadBounds#MAX_REQUEST_BYTES}; unknown operations fail to decode.
 *
 * @param containerId the open altar menu
 * @param session     its nonce
 * @param rules       the fingerprint of the rules the screen showed
 * @param op          what to do
 * @param target      the player to invite ({@link Op#INVITE})
 * @param offer       the invitation id ({@link Op#ACCEPT}, {@link Op#DECLINE})
 */
public record AltarRequest(int containerId, long session, int rules, Op op, Optional<UUID> target, long offer) {
    public enum Op {
        FOUND,
        INVITE,
        WITHDRAW,
        ACCEPT,
        DECLINE,
        MEDITATE,
        ATTUNE,
        STOP
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeLong(session);
        buf.writeInt(rules);
        PayloadBounds.writeEnum(buf, op);
        buf.writeBoolean(target.isPresent());
        target.ifPresent(buf::writeUUID);
        buf.writeLong(offer);
    }

    public static AltarRequest decode(FriendlyByteBuf buf) {
        PayloadBounds.requireAtMost(buf, PayloadBounds.MAX_REQUEST_BYTES);
        int containerId = buf.readVarInt();
        long session = buf.readLong();
        int rules = buf.readInt();
        Op op = PayloadBounds.readEnum(buf, Op.class);
        Optional<UUID> target = buf.readBoolean() ? Optional.of(buf.readUUID()) : Optional.empty();
        long offer = buf.readLong();
        return new AltarRequest(containerId, session, rules, op, target, offer);
    }
}
