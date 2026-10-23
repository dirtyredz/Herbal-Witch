package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A bounded commerce intent (client to server): the open menu id, its server-issued nonce, the next
 * request sequence, an action and its small arguments. It never carries an owner, cost, inventory or
 * result; the server derives those. The whole payload is at most 4 KiB and its text at most 128 UTF-8
 * bytes (a shop name is further limited to 32 code points); anything else is refused while decoding.
 *
 * @param target   offer, order, shop or banned-player id, or the zero UUID
 * @param target2  a second id (the order a purchase answers, the offer a request is associated with)
 * @param revision the offer revision the client displayed
 */
public record ShopRequest(int container, UUID nonce, int sequence, Action action, UUID target, UUID target2, long revision,
                          int a, int b, int c, boolean flag, String text) {
    public static final UUID NONE = new UUID(0, 0);
    public static final int MAX_TEXT_BYTES = 256;

    public enum Action {
        REFRESH(false), HISTORY_PAGE(false),
        CREATE(true), RECONNECT(true), RENAME(true), OPEN(true), CLOSE(true), RETIRE(true), PUBLIC(true), NPC(true), BAN(true), UNBAN(true),
        DEPOSIT(true), WITHDRAW(true), PROCEEDS(true), OFFER_SAVE(true), OFFER_REMOVE(true), BUY(true),
        LINK(true), UNLINK(true), DISPLAY_ASSIGN(true),
        ORDER_CREATE(true), ORDER_CANCEL(true), ORDER_DECLINE(true), ORDER_OFFER(true);

        private final boolean mutation;

        Action(boolean mutation) {
            this.mutation = mutation;
        }

        public boolean mutation() {
            return mutation;
        }
    }

    public ShopRequest {
        if (text.getBytes(StandardCharsets.UTF_8).length > MAX_TEXT_BYTES) throw new IllegalArgumentException("Request text too long");
    }

    public static ShopRequest of(int container, UUID nonce, int sequence, Action action) {
        return new ShopRequest(container, nonce, sequence, action, NONE, NONE, 0, 0, 0, 0, false, "");
    }

    public UUID targetOrNull() {
        return NONE.equals(target) ? null : target;
    }

    public UUID target2OrNull() {
        return NONE.equals(target2) ? null : target2;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(container);
        buf.writeUUID(nonce);
        buf.writeVarInt(sequence);
        PayloadBounds.writeEnum(buf, action);
        buf.writeUUID(target);
        buf.writeUUID(target2);
        buf.writeVarLong(revision);
        buf.writeVarInt(a);
        buf.writeVarInt(b);
        buf.writeVarInt(c);
        buf.writeBoolean(flag);
        buf.writeUtf(text, MAX_TEXT_BYTES);
    }

    public static ShopRequest decode(FriendlyByteBuf buf) {
        PayloadBounds.requireAtMost(buf, PayloadBounds.MAX_REQUEST_BYTES);
        int container = buf.readVarInt();
        UUID nonce = buf.readUUID();
        int sequence = buf.readVarInt();
        Action action = PayloadBounds.readEnum(buf, Action.class);
        UUID target = buf.readUUID(), target2 = buf.readUUID();
        long revision = buf.readVarLong();
        int a = buf.readVarInt(), b = buf.readVarInt(), c = buf.readVarInt();
        boolean flag = buf.readBoolean();
        String text = PayloadBounds.readString(buf, MAX_TEXT_BYTES);
        if (text.getBytes(StandardCharsets.UTF_8).length > MAX_TEXT_BYTES || sequence < 1) throw new DecoderException("Invalid commerce request");
        return new ShopRequest(container, nonce, sequence, action, target, target2, revision, a, b, c, flag, text);
    }
}
