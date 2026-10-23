package io.github.dirtyredz.herbalwitch.magic.broom.network;

import java.util.UUID;

/** Ephemeral per-mount input lease. Never serialized in either the item or the entity. */
public final class BroomControlSession {
    public static final int FORWARD = 1, BACK = 2, LEFT = 4, RIGHT = 8, UP = 16, DOWN = 32, LAND = 64;
    private final UUID rider;
    private final UUID nonce;
    private long lastSequence = -1;
    private long acceptedTick;
    private long windowTick;
    private int attempts;
    private int flags;

    public BroomControlSession(UUID rider, UUID nonce, long tick) {
        this.rider = rider;
        this.nonce = nonce;
        acceptedTick = tick - 1;
        windowTick = tick;
    }

    public UUID nonce() { return nonce; }

    public boolean accept(UUID sender, UUID session, long sequence, int input, long tick) {
        if (tick < windowTick || tick - windowTick >= 20) { windowTick = tick; attempts = 0; }
        if (attempts < 41) { attempts++; }
        if (attempts > 40 || !rider.equals(sender) || !nonce.equals(session) || sequence < 0
                || sequence <= lastSequence || tick <= acceptedTick || (input & ~127) != 0) { return false; }
        lastSequence = sequence;
        acceptedTick = tick;
        flags = input;
        return true;
    }

    public int flags(long tick) { return tick - acceptedTick >= 10 ? 0 : flags; }
    public boolean expired(long tick) { return tick - acceptedTick >= 40; }
}
