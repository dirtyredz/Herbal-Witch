package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * A role-filtered projection of one shop for one open menu (server to client), with the answer to the
 * request that caused it. The snapshot is at most 24 KiB; history arrives in pages instead of being
 * truncated. A public snapshot never contains stock slots, proceeds, bans, history, transaction data
 * or other customers' requests.
 */
public record ShopUpdate(int container, UUID nonce, int sequence, CompoundTag snapshot, ShopResult result) {
    public static final int MAX_SNAPSHOT_BYTES = 24 * 1024;

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(container);
        buf.writeUUID(nonce);
        buf.writeVarInt(sequence);
        buf.writeNbt(snapshot);
        buf.writeBoolean(result != null);
        if (result != null) {
            buf.writeBoolean(result.success());
            buf.writeUtf(result.code(), 64);
            buf.writeBoolean(result.transaction() != null);
            if (result.transaction() != null) buf.writeUUID(result.transaction());
        }
    }

    public static ShopUpdate decode(FriendlyByteBuf buf) {
        if (buf.readableBytes() > MAX_SNAPSHOT_BYTES + 512) throw new DecoderException("Commerce snapshot too large");
        int container = buf.readVarInt();
        UUID nonce = buf.readUUID();
        int sequence = buf.readVarInt();
        CompoundTag snapshot = buf.readNbt(new NbtAccounter(MAX_SNAPSHOT_BYTES * 8L));
        ShopResult result = null;
        if (buf.readBoolean()) {
            boolean success = buf.readBoolean();
            String code = buf.readUtf(64);
            UUID transaction = buf.readBoolean() ? buf.readUUID() : null;
            result = new ShopResult(success, code, transaction);
        }
        return new ShopUpdate(container, nonce, sequence, snapshot == null ? new CompoundTag() : snapshot, result);
    }
}
