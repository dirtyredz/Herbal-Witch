package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sequencing and replay, and the wire bounds of both commerce messages. */
class ShopSessionTest {
    static ShopSession session() {
        return new ShopSession(UUID.randomUUID(), 7, UUID.randomUUID(), UUID.randomUUID(), ShopSession.Origin.REGISTER, ShopSession.View.BROWSE,
                "minecraft:overworld", 1L, 1, 0, false);
    }

    @Test
    void onlyTheNextSequenceActsAndARetransmissionReplaysTheResult() {
        ShopSession s = session();
        assertEquals(ShopSession.Arrival.STALE, s.arrive(0));
        assertEquals(ShopSession.Arrival.STALE, s.arrive(2), "a reordered request never becomes a new purchase");
        assertEquals(ShopSession.Arrival.NEW, s.arrive(1));
        ShopResult bought = ShopResult.ok("purchased", UUID.randomUUID());
        s.answered(bought);
        assertEquals(ShopSession.Arrival.REPLAY, s.arrive(1));
        assertEquals(bought, s.last());
        assertEquals(ShopSession.Arrival.NEW, s.arrive(2));
        assertEquals(ShopSession.Arrival.STALE, s.arrive(1 + 5));
        assertTrue(s.matches(7, s.nonce()));
        assertEquals(false, s.matches(8, s.nonce()));
        assertEquals(false, s.matches(7, UUID.randomUUID()));
    }

    @Test
    void requestsRoundTripAndOversizedOrInvalidPayloadsAreRefused() {
        ShopRequest request = new ShopRequest(3, UUID.randomUUID(), 1, ShopRequest.Action.BUY, UUID.randomUUID(), ShopRequest.NONE, 4, 1, 2, 3, true, "Hazel");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        request.encode(buf);
        assertEquals(request, ShopRequest.decode(buf));
        assertThrows(IllegalArgumentException.class, () -> new ShopRequest(3, UUID.randomUUID(), 1, ShopRequest.Action.RENAME, ShopRequest.NONE,
                ShopRequest.NONE, 0, 0, 0, 0, false, "x".repeat(300)));
        FriendlyByteBuf unknown = new FriendlyByteBuf(Unpooled.buffer());
        unknown.writeVarInt(1);
        unknown.writeUUID(UUID.randomUUID());
        unknown.writeVarInt(1);
        unknown.writeVarInt(999);
        assertThrows(DecoderException.class, () -> ShopRequest.decode(unknown));
        FriendlyByteBuf huge = new FriendlyByteBuf(Unpooled.buffer());
        huge.writeBytes(new byte[5000]);
        assertThrows(DecoderException.class, () -> ShopRequest.decode(huge));
        FriendlyByteBuf zero = new FriendlyByteBuf(Unpooled.buffer());
        new ShopRequest(3, UUID.randomUUID(), 1, ShopRequest.Action.REFRESH, ShopRequest.NONE, ShopRequest.NONE, 0, 0, 0, 0, false, "").encode(zero);
        zero.setByte(17, 0); // sequence varint follows the container (1 byte) and nonce (16 bytes)
        assertThrows(DecoderException.class, () -> ShopRequest.decode(zero), "sequence numbers start at 1");
    }

    @Test
    void updatesAreBoundedAndCarryRecoveryIdentity() {
        CompoundTag snapshot = new CompoundTag();
        snapshot.putString("name", "Shop");
        ShopUpdate update = new ShopUpdate(3, UUID.randomUUID(), 2, snapshot, ShopResult.recovery("recovery", UUID.randomUUID()));
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        update.encode(buf);
        assertEquals(update, ShopUpdate.decode(buf));
        FriendlyByteBuf huge = new FriendlyByteBuf(Unpooled.buffer());
        huge.writeBytes(new byte[ShopUpdate.MAX_SNAPSHOT_BYTES + 1024]);
        assertThrows(DecoderException.class, () -> ShopUpdate.decode(huge));
    }
}
