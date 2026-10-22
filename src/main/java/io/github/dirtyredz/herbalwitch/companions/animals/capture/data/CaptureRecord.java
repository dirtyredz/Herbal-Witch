package io.github.dirtyredz.herbalwitch.companions.animals.capture.data;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

/** Immutable authority for one animal. Released generations remain permanent replay tombstones. */
public record CaptureRecord(UUID animal, long generation, UUID transaction, Phase phase, UUID captor,
                            UUID token, String type, int adapter, String summary, String source, String destination,
                            CompoundTag payload) {
    public enum Phase { CAPTURE_PREPARED, STORED, RELEASE_PREPARED, RELEASED, QUARANTINED }
    public CaptureRecord {
        if (animal == null || generation < 1 || transaction == null || phase == null || captor == null
                || type == null || type.length() > 256 || adapter != 1 || summary == null || summary.length() > 128
                || source == null || source.length() > 256 || destination == null || destination.length() > 256
                || phase != Phase.RELEASED && (payload == null || payload.isEmpty() || token == null)
                || phase == Phase.RELEASED && token != null) throw new IllegalArgumentException("Invalid capture record");
        payload = payload == null ? null : payload.copy();
    }
    @Override public CompoundTag payload() { return payload == null ? null : payload.copy(); }
    public boolean storedPayload() { return payload != null; }
    public CaptureRecord phase(Phase next, String destination) {
        boolean legal = next == Phase.QUARANTINED && storedPayload() || switch (phase) {
            case CAPTURE_PREPARED -> next == Phase.STORED;
            case STORED -> next == Phase.RELEASE_PREPARED;
            case RELEASE_PREPARED -> next == Phase.STORED || next == Phase.RELEASED;
            default -> false;
        };
        if (!legal) throw new IllegalStateException("Illegal capture transition " + phase + " -> " + next);
        return new CaptureRecord(animal, generation, transaction, next, captor, next == Phase.RELEASED ? null : token == null ? UUID.randomUUID() : token,
                type, adapter, summary, source, destination, payload);
    }
    public CaptureRecord compact() {
        if (phase != Phase.RELEASED) throw new IllegalStateException("Only a verified release may compact");
        return new CaptureRecord(animal, generation, transaction, phase, captor, null, type, adapter, summary, source, destination, null);
    }
    public CaptureRecord reissue(UUID nextToken) {
        if (phase != Phase.STORED) throw new IllegalStateException("Only stored tokens may be reissued");
        return new CaptureRecord(animal, generation, UUID.randomUUID(), phase, captor, nextToken, type, adapter, summary, source, destination, payload);
    }
    public CaptureRecord recover(UUID nextToken) {
        if (phase == Phase.STORED || phase == Phase.RELEASED || generation == Long.MAX_VALUE) throw new IllegalStateException("No recovery payload");
        return new CaptureRecord(animal, generation + 1, UUID.randomUUID(), Phase.STORED, captor, nextToken, type, adapter, summary, source, "", payload);
    }
}
