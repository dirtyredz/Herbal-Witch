package io.github.dirtyredz.herbalwitch.companions.animals.inspection.network;

import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

/** Private card facts. No relationship map, owner UUID, capture record or payload crosses this boundary. */
public record AnimalSnapshot(int menu, long nonce, UUID target, long revision, String species, int trust, int feeds,
                             int cooldown, int hurt, int healing, int calm, int sleep, int owner, int actions, Reason adoption) {
    public AnimalSnapshot {
        if (menu < 0 || nonce == 0 || target == null || revision < 1 || species.length() > 256 || trust < 0 || trust > 100
                || feeds < 0 || feeds > 1_000_000 || cooldown < 0 || cooldown > 1200 || hurt < 0 || hurt > 600
                || healing < 0 || healing > 200 || calm < 0 || calm > 200 || sleep < 0 || sleep > 1_000_000
                || owner < 0 || owner > 3 || actions < 0 || actions > 127 || adoption == null)
            throw new IllegalArgumentException("Invalid animal card");
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(menu); buf.writeLong(nonce); buf.writeUUID(target); buf.writeLong(revision); buf.writeUtf(species, 256);
        for (int n : new int[] {trust, feeds, cooldown, hurt, healing, calm, sleep, owner, actions}) buf.writeVarInt(n);
        PayloadBounds.writeEnum(buf, adoption);
    }
    public static AnimalSnapshot decode(FriendlyByteBuf buf) {
        PayloadBounds.requireAtMost(buf, 4096);
        var result = new AnimalSnapshot(buf.readVarInt(), buf.readLong(), buf.readUUID(), buf.readLong(), buf.readUtf(256),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), PayloadBounds.readEnum(buf, Reason.class));
        if (buf.isReadable()) throw new io.netty.handler.codec.DecoderException("Trailing animal card data");
        return result;
    }
}
