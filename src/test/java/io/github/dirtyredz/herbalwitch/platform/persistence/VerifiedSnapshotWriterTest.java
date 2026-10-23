package io.github.dirtyredz.herbalwitch.platform.persistence;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureCheckpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VerifiedSnapshotWriterTest {
    @TempDir Path directory;
    private static final SnapshotEnvelope FORMAT = new SnapshotEnvelope(0x48574338, 1);

    @Test void existingCaptureBytesRemainReadableAndNewWritesAreByteIdentical() throws Exception {
        byte[] payload = {1, 2, 3, 4};
        byte[] legacy = ByteBuffer.allocate(48).putInt(0x48574338).putInt(1).putInt(4)
                .put(MessageDigest.getInstance("SHA-256").digest(payload)).put(payload).array();
        Path path = directory.resolve("captures.dat");
        Files.write(path, legacy);
        assertArrayEquals(payload, new CaptureCheckpoint().read(path, 4));
        assertTrue(new CaptureCheckpoint().write(path, payload, 4));
        assertArrayEquals(legacy, Files.readAllBytes(path));
    }

    @Test void eachWriteFaultPreservesAnInspectableSnapshotAndCleansTemporaryFiles() throws Exception {
        for (var stage : VerifiedSnapshotWriter.Stage.values()) {
            Path path = directory.resolve(stage + ".dat");
            var normal = new VerifiedSnapshotWriter(FORMAT);
            normal.write(path, new byte[] {1}, 100);
            var faulty = new VerifiedSnapshotWriter(FORMAT, at -> { if (at == stage) throw new IOException("injected " + stage); });
            assertThrows(IOException.class, () -> faulty.write(path, new byte[] {2}, 100));
            assertArrayEquals(new byte[] {(byte) (stage == VerifiedSnapshotWriter.Stage.BEFORE_VERIFY ? 2 : 1)}, normal.read(path, 100));
        }
        try (var files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.toString().endsWith(".tmp")));
        }
    }

    @Test void rejectsCorruptTruncatedExtendedAndForeignEnvelopes() throws Exception {
        byte[] good = FORMAT.encode(new byte[] {1, 2}, 2);
        for (int offset : new int[] {0, 4, 8, 12, 45}) {
            byte[] bad = good.clone(); bad[offset] ^= 1;
            assertThrows(IOException.class, () -> FORMAT.decode(bad, 100));
        }
        assertThrows(IOException.class, () -> FORMAT.decode(Arrays.copyOf(good, good.length - 1), 100));
        assertThrows(IOException.class, () -> FORMAT.decode(Arrays.copyOf(good, good.length + 1), 100));
        assertThrows(IOException.class, () -> FORMAT.decode(good, 1));
        assertThrows(IOException.class, () -> FORMAT.encode(new byte[2], 1));
        assertThrows(IOException.class, () -> FORMAT.encode(new byte[0], -1));
        assertThrows(IOException.class, () -> FORMAT.decode(good, Integer.MAX_VALUE));
    }

    @Test void readAndOversizedWriteNeverReplaceTheExistingFile() throws Exception {
        Path path = directory.resolve("bounded.dat");
        var writer = new VerifiedSnapshotWriter(FORMAT);
        writer.write(path, new byte[] {4, 5}, 2);
        byte[] before = Files.readAllBytes(path);
        assertThrows(IOException.class, () -> writer.read(path, 1));
        assertThrows(IOException.class, () -> writer.write(path, new byte[3], 2));
        assertArrayEquals(before, Files.readAllBytes(path));
    }
}
