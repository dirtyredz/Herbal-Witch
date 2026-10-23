package io.github.dirtyredz.herbalwitch.platform.persistence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Bounded, versioned bytes with SHA-256 integrity. Lifecycle meaning belongs to the caller. */
public record SnapshotEnvelope(int magic, int version) {
    public static final int HEADER = 44;

    public byte[] encode(byte[] payload, int maximum) throws IOException {
        checkMaximum(maximum);
        if (payload.length > maximum) throw new IOException("Snapshot exceeds bound");
        return ByteBuffer.allocate(HEADER + payload.length).putInt(magic).putInt(version).putInt(payload.length)
                .put(digest(payload)).put(payload).array();
    }

    public byte[] decode(byte[] bytes, int maximum) throws IOException {
        checkMaximum(maximum);
        if (bytes.length < HEADER || bytes.length > maximum + HEADER) throw new IOException("Invalid snapshot size");
        ByteBuffer in = ByteBuffer.wrap(bytes);
        if (in.getInt() != magic || in.getInt() != version) throw new IOException("Unsupported snapshot envelope");
        int size = in.getInt();
        if (size < 0 || size != bytes.length - HEADER || size > maximum) throw new IOException("Invalid snapshot length");
        byte[] checksum = new byte[32]; in.get(checksum);
        byte[] payload = new byte[size]; in.get(payload);
        if (!MessageDigest.isEqual(checksum, digest(payload))) throw new IOException("Corrupt snapshot checksum");
        return payload;
    }

    static void checkMaximum(int maximum) throws IOException {
        if (maximum < 0 || maximum > Integer.MAX_VALUE - HEADER - 1) throw new IOException("Invalid snapshot bound");
    }

    private static byte[] digest(byte[] bytes) {
        try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
