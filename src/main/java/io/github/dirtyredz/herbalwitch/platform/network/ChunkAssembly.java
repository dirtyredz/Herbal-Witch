package io.github.dirtyredz.herbalwitch.platform.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Assembles one snapshot from its chunks atomically on the receiving side. Every chunk names the
 * snapshot's revision, its index, the chunk count and the declared record total; a chunk that
 * disagrees with the snapshot in progress (another revision, total or count, an index out of order)
 * restarts or rejects it, and the records become visible only when the last chunk arrives and the
 * record count matches the declaration. A half-received snapshot is never exposed.
 *
 * @param <T> record type
 */
public final class ChunkAssembly<T> {
    /** Header every chunk carries. */
    public record Header(int revision, int index, int chunks, int totalRecords) {
        public Header {
            if (chunks < 1 || index < 0 || index >= chunks || totalRecords < 0 || totalRecords > PayloadBounds.MAX_TOTAL_RECORDS) {
                throw new IllegalArgumentException("bad chunk header " + index + "/" + chunks + " of " + totalRecords);
            }
        }
    }

    private Header started;
    private final List<T> records = new ArrayList<>();

    /**
     * Offers one chunk. Returns the complete snapshot when this chunk completes it; empty while it
     * is still assembling. Throws {@link IllegalStateException} for a chunk that does not belong (the
     * assembly is reset, so the next snapshot's first chunk starts cleanly).
     */
    public Optional<List<T>> offer(Header header, List<T> chunk) {
        if (chunk.size() > PayloadBounds.MAX_CHUNK_RECORDS) {
            reset();
            throw new IllegalStateException("chunk of " + chunk.size() + " records");
        }
        if (header.index() == 0) {
            reset();
            started = header;
        } else if (started == null || started.revision() != header.revision() || started.chunks() != header.chunks()
                || started.totalRecords() != header.totalRecords() || expectedIndex() != header.index()) {
            reset();
            throw new IllegalStateException("chunk " + header.index() + " of revision " + header.revision()
                    + " does not continue the snapshot in progress");
        }
        records.addAll(chunk);
        if (records.size() > header.totalRecords()) {
            reset();
            throw new IllegalStateException("snapshot exceeds its declared " + header.totalRecords() + " records");
        }
        if (header.index() < header.chunks() - 1) {
            started = new Header(header.revision(), header.index(), header.chunks(), header.totalRecords());
            return Optional.empty();
        }
        if (records.size() != header.totalRecords()) {
            int got = records.size();
            reset();
            throw new IllegalStateException("snapshot declared " + header.totalRecords() + " records, received " + got);
        }
        List<T> done = List.copyOf(records);
        reset();
        return Optional.of(done);
    }

    private int expectedIndex() {
        return started.index() + 1;
    }

    public void reset() {
        started = null;
        records.clear();
    }
}
