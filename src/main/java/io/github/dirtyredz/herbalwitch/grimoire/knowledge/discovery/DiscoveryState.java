package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * One player's durable knowledge: a deduplicated, sorted set of typed discovery keys, plus saved
 * strings this version cannot read (a key of a kind added by a later version) kept verbatim so a
 * downgrade never loses them. Keys whose content is currently absent (a datapack recipe removed for
 * now) stay known; the grimoire hides them until their content returns. Plain Java; no
 * synchronisation metadata (revisions and sessions live with the capability, never on disk).
 */
public final class DiscoveryState {
    /** The most discoveries one player can hold, readable or retained. */
    public static final int MAX_DISCOVERIES = 4096;

    /** What a grant did. */
    public enum Grant {
        ADDED,
        ALREADY_KNOWN,
        /** Refused: the ceiling is reached. Knowledge is never truncated to make room. */
        FULL,
        /** Refused: the saved data could not be read, so it is preserved untouched. */
        READ_ONLY
    }

    private final TreeSet<EntryKey> known = new TreeSet<>();
    private final TreeSet<String> retained = new TreeSet<>();
    private final boolean readOnly;

    public DiscoveryState() {
        this(false);
    }

    private DiscoveryState(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** A state that refuses every grant, for saved data this version must not overwrite. */
    public static DiscoveryState readOnly() {
        return new DiscoveryState(true);
    }

    public Grant grant(EntryKey key) {
        if (readOnly) {
            return Grant.READ_ONLY;
        }
        if (known.contains(key)) {
            return Grant.ALREADY_KNOWN;
        }
        if (size() >= MAX_DISCOVERIES) {
            return Grant.FULL;
        }
        known.add(key);
        return Grant.ADDED;
    }

    /** Keeps a saved string this version cannot parse (loading only). False when the ceiling is reached. */
    boolean retain(String raw) {
        if (retained.contains(raw)) {
            return true;
        }
        if (size() >= MAX_DISCOVERIES) {
            return false;
        }
        retained.add(raw);
        return true;
    }

    public boolean knows(EntryKey key) {
        return known.contains(key);
    }

    /** Every readable discovery, sorted. */
    public Set<EntryKey> known() {
        return Collections.unmodifiableSet(known);
    }

    /** Saved strings kept verbatim because this version cannot read them, sorted. */
    public Set<String> retained() {
        return Collections.unmodifiableSet(retained);
    }

    public int size() {
        return known.size() + retained.size();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /** An independent copy (player cloning), read-only if this one is. */
    public DiscoveryState copy() {
        DiscoveryState copy = new DiscoveryState(readOnly);
        copy.known.addAll(known);
        copy.retained.addAll(retained);
        return copy;
    }
}
