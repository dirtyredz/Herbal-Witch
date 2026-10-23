package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord.Phase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class CaptureCheckpointTest {
    @TempDir Path directory;
    @Test void endingATransactionRetainsTheWorldCooldownAndTheBoundaryReopensIt() {
        var data = new CaptureSavedData(directory.resolve("interval.dat"), new CaptureCheckpoint());
        int interval = io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig.current().transactionInterval();
        assertTrue(data.begin(100));
        assertFalse(data.begin(100 + interval)); // Still active, even after the time gate.
        data.end();
        assertFalse(data.begin(100 + interval - 1)); // Finishing does not reset the world-wide interval.
        assertTrue(data.begin(100 + interval));
        data.end();
    }
    @Test void checksummedAtomicReplacementAndCorruptionDetection() throws Exception {
        var path = directory.resolve("ledger.dat"); var writer = new CaptureCheckpoint();
        assertTrue(writer.write(path, new byte[] {1, 2}, 1024)); assertTrue(writer.write(path, new byte[] {3, 4, 5}, 1024));
        assertArrayEquals(new byte[] {3, 4, 5}, writer.read(path, 1024));
        byte[] corrupt = Files.readAllBytes(path); corrupt[corrupt.length - 1] ^= 1; Files.write(path, corrupt);
        assertThrows(IOException.class, () -> writer.read(path, 1024));
    }
    @Test void everyFaultRetainsDirtyStateAndLocksAllSubsequentWrites() {
        for (var stage : CaptureCheckpoint.Stage.values()) {
            var path = directory.resolve(stage + ".dat");
            var data = new CaptureSavedData(path, new CaptureCheckpoint(at -> { if (at == stage) throw new IOException("injected " + stage); }));
            assertFalse(data.commit(CaptureStateTest.prepared(1))); assertTrue(data.isDirty()); assertFalse(data.writable());
            assertFalse(data.commit(CaptureStateTest.prepared(2))); assertEquals(1, data.size());
            // Writes stay locked, but the loaded records remain known for admission decisions.
            assertTrue(data.readable()); assertTrue(data.record(CaptureStateTest.prepared(1).animal()).isPresent());
            data.save(path.toFile()); assertTrue(data.isDirty());
        }
    }
    @Test void restartQuarantinesPreparedAndUncompactedReleaseWithoutAutomaticReplay() {
        for (var phase : new Phase[] {Phase.CAPTURE_PREPARED, Phase.RELEASE_PREPARED, Phase.RELEASED}) {
            var path = directory.resolve(phase + ".dat"); var record = CaptureStateTest.prepared(1);
            if (phase != Phase.CAPTURE_PREPARED) record = record.phase(Phase.STORED, "").phase(Phase.RELEASE_PREPARED, "x");
            if (phase == Phase.RELEASED) record = record.phase(Phase.RELEASED, "x");
            var original = new CaptureSavedData(path, new CaptureCheckpoint()); assertTrue(original.commit(record));
            var restarted = new CaptureSavedData(path, new CaptureCheckpoint()); restarted.load();
            var restored = restarted.record(record.animal()).orElseThrow();
            assertEquals(Phase.QUARANTINED, restored.phase()); assertTrue(restored.storedPayload()); assertTrue(restarted.writable());
        }
    }
    @Test void corruptFileIsPreservedAndCannotBeReplacedByAnEmptySave() throws Exception {
        var path = directory.resolve("corrupt.dat"); byte[] corrupt = {4, 5, 6}; Files.write(path, corrupt);
        var data = new CaptureSavedData(path, new CaptureCheckpoint()); data.load(); data.setDirty(); data.save(path.toFile());
        assertFalse(data.writable()); assertFalse(data.readable()); assertArrayEquals(corrupt, Files.readAllBytes(path));
    }
}
