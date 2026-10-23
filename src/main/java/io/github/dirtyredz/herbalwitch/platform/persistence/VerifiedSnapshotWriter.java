package io.github.dirtyredz.herbalwitch.platform.persistence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

/** Serialized temp/force/atomic-replace/read-back mechanics, without capture or bond policy. */
public final class VerifiedSnapshotWriter {
    public enum Stage { BEFORE_WRITE, BEFORE_FORCE, BEFORE_REPLACE, BEFORE_VERIFY }
    @FunctionalInterface public interface Fault { void at(Stage stage) throws IOException; }
    private final SnapshotEnvelope envelope;
    private final Fault fault;

    public VerifiedSnapshotWriter(SnapshotEnvelope envelope) { this(envelope, stage -> {}); }
    public VerifiedSnapshotWriter(SnapshotEnvelope envelope, Fault fault) {
        this.envelope = Objects.requireNonNull(envelope);
        this.fault = Objects.requireNonNull(fault);
    }

    /** A failure after replacement is ambiguous. The caller must retain payloads and lock writes. */
    public synchronized void write(Path destination, byte[] snapshot, int maximum) throws IOException {
        byte[] encoded = envelope.encode(snapshot, maximum);
        Path target = destination.toAbsolutePath();
        Path temporary = null;
        try {
            Files.createDirectories(target.getParent());
            temporary = Files.createTempFile(target.getParent(), "herbalwitch-snapshot-", ".tmp");
            fault.at(Stage.BEFORE_WRITE);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer bytes = ByteBuffer.wrap(encoded);
                while (bytes.hasRemaining()) channel.write(bytes);
                fault.at(Stage.BEFORE_FORCE);
                channel.force(true);
            }
            fault.at(Stage.BEFORE_REPLACE);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            temporary = null;
            fault.at(Stage.BEFORE_VERIFY);
            if (!Arrays.equals(snapshot, read(target, maximum))) throw new IOException("Snapshot verification mismatch");
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* not authoritative */ }
        }
    }

    public synchronized byte[] read(Path path, int maximum) throws IOException {
        SnapshotEnvelope.checkMaximum(maximum);
        long length = Files.size(path);
        if (length < SnapshotEnvelope.HEADER || length > maximum + (long) SnapshotEnvelope.HEADER)
            throw new IOException("Invalid snapshot file size");
        byte[] bytes;
        try (var input = Files.newInputStream(path)) { bytes = input.readNBytes(maximum + SnapshotEnvelope.HEADER + 1); }
        return envelope.decode(bytes, maximum);
    }
}
