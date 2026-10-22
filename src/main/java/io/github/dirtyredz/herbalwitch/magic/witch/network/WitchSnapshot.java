package io.github.dirtyredz.herbalwitch.magic.witch.network;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

/**
 * The private, complete witch status of the receiving player (fixed size, so no deltas): sent on
 * login, respawn and dimension change, after changes at most every few ticks, and at once after a
 * conversion or a ritual's result. {@code epoch} identifies the server-side player object (a new one
 * after every replacement) and {@code revision} increases within it, so a client ignores anything
 * older than what it holds. Nobody else's energy ever travels.
 *
 * @param epoch      the player object this snapshot describes
 * @param revision   increasing within one epoch
 * @param initiated  witch status
 * @param energy     current energy in hundredths
 * @param capacity   current capacity in hundredths (derived on the server)
 * @param practice   successfully spent energy in hundredths
 * @param restful    a restful preparation awaits the next rewarded sleep
 * @param recovering what is recovering energy right now
 * @param readOnly   the saved state could not be read and is preserved unchanged
 */
public record WitchSnapshot(long epoch, int revision, boolean initiated, int energy, int capacity, int practice, boolean restful,
                            Recovering recovering, boolean readOnly) {
    /** What recovers energy at the moment (for the HUD's optional indicator). */
    public enum Recovering {
        NONE,
        REST,
        MEDITATION
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(epoch);
        buf.writeVarInt(revision);
        buf.writeBoolean(initiated);
        buf.writeVarInt(energy);
        buf.writeVarInt(capacity);
        buf.writeVarInt(practice);
        buf.writeBoolean(restful);
        PayloadBounds.writeEnum(buf, recovering);
        buf.writeBoolean(readOnly);
    }

    public static WitchSnapshot decode(FriendlyByteBuf buf) {
        long epoch = buf.readLong();
        int revision = buf.readVarInt();
        boolean initiated = buf.readBoolean();
        int energy = buf.readVarInt();
        int capacity = buf.readVarInt();
        int practice = buf.readVarInt();
        boolean restful = buf.readBoolean();
        Recovering recovering = PayloadBounds.readEnum(buf, Recovering.class);
        boolean readOnly = buf.readBoolean();
        if (energy < 0 || capacity < 0 || practice < 0 || revision < 0) {
            throw new io.netty.handler.codec.DecoderException("negative witch snapshot values");
        }
        return new WitchSnapshot(epoch, revision, initiated, energy, capacity, practice, restful, recovering, readOnly);
    }

    /** Whether this snapshot is newer than one held for ({@code epoch}, {@code revision}). */
    public boolean newerThan(long heldEpoch, int heldRevision) {
        return epoch > heldEpoch || epoch == heldEpoch && revision > heldRevision;
    }
}
