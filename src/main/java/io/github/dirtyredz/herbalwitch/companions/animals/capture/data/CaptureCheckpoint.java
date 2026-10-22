package io.github.dirtyredz.herbalwitch.companions.animals.capture.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/** One serialized, verified writer for explicit checkpoints and SavedData's ordinary save path. */
public final class CaptureCheckpoint {
    private static final int MAGIC = 0x48574338;
    private static final int HEADER = 44;
    public enum Stage { BEFORE_WRITE, BEFORE_FORCE, BEFORE_REPLACE, BEFORE_VERIFY }
    @FunctionalInterface public interface Fault { void at(Stage stage) throws IOException; }
    private final Fault fault;
    public CaptureCheckpoint() { this(stage -> {}); }
    public CaptureCheckpoint(Fault fault) { this.fault = fault; }

    public synchronized boolean write(Path destination, byte[] snapshot, int maximum) {
        Path temporary = null;
        try {
            if (snapshot.length > maximum) throw new IOException("Capture snapshot exceeds bound");
            byte[] envelope = ByteBuffer.allocate(HEADER + snapshot.length).putInt(MAGIC).putInt(1).putInt(snapshot.length)
                    .put(digest(snapshot)).put(snapshot).array();
            Files.createDirectories(destination.toAbsolutePath().getParent());
            temporary = Files.createTempFile(destination.toAbsolutePath().getParent(), "herbalwitch-captures-", ".tmp");
            fault.at(Stage.BEFORE_WRITE);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer bytes = ByteBuffer.wrap(envelope);
                while (bytes.hasRemaining()) channel.write(bytes);
                fault.at(Stage.BEFORE_FORCE); channel.force(true);
            }
            fault.at(Stage.BEFORE_REPLACE);
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            temporary = null;
            fault.at(Stage.BEFORE_VERIFY);
            if (!Arrays.equals(snapshot, read(destination, maximum))) throw new IOException("Capture checkpoint verification mismatch");
            return true;
        } catch (IOException | RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(CaptureCheckpoint.class).error("Capture checkpoint failed; transport is locked until restart and inspection", failure);
            return false;
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* not authoritative */ }
        }
    }

    public synchronized byte[] read(Path path, int maximum) throws IOException {
        long length = Files.size(path);
        if (length < HEADER || length > maximum + (long) HEADER) throw new IOException("Invalid capture checkpoint size");
        byte[] bytes;
        try (var input = Files.newInputStream(path)) { bytes = input.readNBytes(maximum + HEADER + 1); }
        if (bytes.length < HEADER || bytes.length > maximum + HEADER) throw new IOException("Capture checkpoint changed size while reading");
        ByteBuffer in = ByteBuffer.wrap(bytes);
        if (in.getInt() != MAGIC || in.getInt() != 1) throw new IOException("Unsupported capture checkpoint");
        int size = in.getInt();
        if (size < 0 || size != bytes.length - HEADER || size > maximum) throw new IOException("Invalid capture checkpoint length");
        byte[] checksum = new byte[32]; in.get(checksum);
        byte[] payload = new byte[size]; in.get(payload);
        if (!MessageDigest.isEqual(checksum, digest(payload))) throw new IOException("Corrupt capture checkpoint checksum");
        return payload;
    }

    private static byte[] digest(byte[] bytes) {
        try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
