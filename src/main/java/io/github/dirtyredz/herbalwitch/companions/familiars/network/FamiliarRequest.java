package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

public record FamiliarRequest(int container, long nonce, long revision, String action, UUID bond, UUID cursor, String value) {
    public void encode(FriendlyByteBuf b) {
        b.writeVarInt(container); b.writeLong(nonce); b.writeLong(revision); b.writeUtf(action, 24);
        b.writeBoolean(bond != null); if (bond != null) b.writeUUID(bond);
        b.writeBoolean(cursor != null); if (cursor != null) b.writeUUID(cursor); b.writeUtf(value, 64);
    }
    public static FamiliarRequest decode(FriendlyByteBuf b) {
        PayloadBounds.requireAtMost(b, 512);
        return new FamiliarRequest(b.readVarInt(), b.readLong(), b.readLong(), b.readUtf(24), b.readBoolean() ? b.readUUID() : null,
                b.readBoolean() ? b.readUUID() : null, b.readUtf(64));
    }
}
