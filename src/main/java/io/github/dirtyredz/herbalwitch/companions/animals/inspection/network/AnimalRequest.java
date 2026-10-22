package io.github.dirtyredz.herbalwitch.companions.animals.inspection.network;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

/** The only card operation is earned adoption. Costs, actor and inventory are always server-derived. */
public record AnimalRequest(int menu, long nonce, UUID target, long revision) {
    public void encode(FriendlyByteBuf buf) { buf.writeVarInt(menu); buf.writeLong(nonce); buf.writeUUID(target); buf.writeLong(revision); }
    public static AnimalRequest decode(FriendlyByteBuf buf) {
        PayloadBounds.requireAtMost(buf, 4096);
        var request = new AnimalRequest(buf.readVarInt(), buf.readLong(), buf.readUUID(), buf.readLong());
        if (buf.isReadable()) throw new io.netty.handler.codec.DecoderException("Trailing animal request data");
        return request;
    }
    public boolean matches(int id, long session, UUID animal, long current) {
        return menu == id && nonce != 0 && nonce == session && target.equals(animal) && revision > 0 && revision == current;
    }
}
