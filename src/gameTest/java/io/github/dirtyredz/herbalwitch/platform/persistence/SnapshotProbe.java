package io.github.dirtyredz.herbalwitch.platform.persistence;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/** Test-source-only access: never shipped in the release jar. */
public final class SnapshotProbe implements AutoCloseable {
    public SnapshotProbe(BiConsumer<VerifiedSnapshotWriter.Stage, Path> observer) {
        if (VerifiedSnapshotWriter.PROBE.get()!=null) throw new IllegalStateException("Snapshot probe already installed");
        VerifiedSnapshotWriter.PROBE.set(observer);
    }
    @Override public void close() { VerifiedSnapshotWriter.PROBE.remove(); }
}
