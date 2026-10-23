package io.github.dirtyredz.herbalwitch.companions.animals.capture.data;

import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotEnvelope;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;

import java.io.IOException;
import java.nio.file.Path;

/** Capture's existing envelope and failure policy over the shared verified filesystem writer. */
public final class CaptureCheckpoint {
    private static final SnapshotEnvelope FORMAT = new SnapshotEnvelope(0x48574338, 1);
    public enum Stage { BEFORE_WRITE, BEFORE_FORCE, BEFORE_REPLACE, BEFORE_VERIFY }
    @FunctionalInterface public interface Fault { void at(Stage stage) throws IOException; }
    private final VerifiedSnapshotWriter writer;
    public CaptureCheckpoint() { this(stage -> {}); }
    public CaptureCheckpoint(Fault fault) {
        writer = new VerifiedSnapshotWriter(FORMAT, stage -> fault.at(Stage.valueOf(stage.name())));
    }

    public boolean write(Path destination, byte[] snapshot, int maximum) {
        try {
            writer.write(destination, snapshot, maximum);
            return true;
        } catch (IOException | RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(CaptureCheckpoint.class).error("Capture checkpoint failed; transport is locked until restart and inspection", failure);
            return false;
        }
    }

    public byte[] read(Path path, int maximum) throws IOException { return writer.read(path, maximum); }
}
