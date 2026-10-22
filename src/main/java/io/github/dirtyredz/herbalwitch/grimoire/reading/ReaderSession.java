package io.github.dirtyredz.herbalwitch.grimoire.reading;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients.IngredientRequirements;

import java.util.List;
import java.util.Optional;

/**
 * The server's state of one open reader: its nonce, the recipe whose ingredients the player is
 * looking at, and what was last sent, so counts are recomputed only when the selection, the
 * inventory or the content changed, and sent only when they differ, at most every
 * {@link ReaderService#UPDATE_INTERVAL} ticks. Never saved; it ends with the menu.
 */
public final class ReaderSession {
    private final long nonce;
    private Optional<EntryKey> selected = Optional.empty();
    private boolean selectionChanged;
    private long fingerprint;
    private int revision = -1;
    private long lastSentTick = Long.MIN_VALUE / 2;
    private Optional<EntryKey> sentKey = Optional.empty();
    private List<IngredientRequirements.Count> sent = List.of();

    public ReaderSession(long nonce) {
        this.nonce = nonce;
    }

    public long nonce() {
        return nonce;
    }

    public Optional<EntryKey> selected() {
        return selected;
    }

    void select(Optional<EntryKey> key) {
        if (!selected.equals(key)) {
            selected = key;
            selectionChanged = true;
        }
    }

    /** Whether the counts must be recomputed. */
    boolean stale(long inventoryFingerprint, int contentRevision) {
        return selectionChanged || inventoryFingerprint != fingerprint || contentRevision != revision;
    }

    boolean due(long tick) {
        return tick - lastSentTick >= ReaderService.UPDATE_INTERVAL;
    }

    /** Records a computation; returns whether it differs from what was last sent (so it must be sent). */
    boolean computed(long tick, long inventoryFingerprint, int contentRevision, EntryKey key, List<IngredientRequirements.Count> counts) {
        selectionChanged = false;
        fingerprint = inventoryFingerprint;
        revision = contentRevision;
        if (sentKey.equals(Optional.of(key)) && sent.equals(counts)) {
            return false;
        }
        sentKey = Optional.of(key);
        sent = List.copyOf(counts);
        lastSentTick = tick;
        return true;
    }

    /** Nothing selected any more: forget what was sent. */
    void cleared(long inventoryFingerprint, int contentRevision) {
        selectionChanged = false;
        fingerprint = inventoryFingerprint;
        revision = contentRevision;
        sentKey = Optional.empty();
        sent = List.of();
    }
}
