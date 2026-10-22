package io.github.dirtyredz.herbalwitch.grimoire.content;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The permanent identity of a grimoire entry and of a discovery: an entry kind plus a resource id
 * ({@code namespace:path}). Recipe entries keep the live recipe's id, herb entries the plant's
 * registry id, station entries the station block's id, guides an editorial id. Never a translated
 * title, a list position or a result item. Written as {@code <kind>|<namespace>:<path>}. Plain Java,
 * so discovery state and its codec are unit-testable without Minecraft.
 *
 * @param kind what the id names
 * @param id   {@code namespace:path}
 */
public record EntryKey(Kind kind, String id) implements Comparable<EntryKey> {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    public static final int MAX_LENGTH = 256;

    /** What an entry describes. The lower-case name is the persisted spelling. */
    public enum Kind {
        GUIDE,
        HERB,
        STATION,
        RECIPE,
        /** A furniture piece's public reference page (decor, 0.4.0). */
        FURNITURE;

        public String serialized() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Optional<Kind> parse(String s) {
            for (Kind kind : values()) {
                if (kind.serialized().equals(s)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }
    }

    public EntryKey {
        if (kind == null || id == null || !ID.matcher(id).matches() || id.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("bad entry key " + kind + " " + id);
        }
    }

    public static EntryKey of(Kind kind, String id) {
        return new EntryKey(kind, id);
    }

    /** {@code <kind>|<namespace>:<path>}. */
    public String serialized() {
        return kind.serialized() + "|" + id;
    }

    /** The key a string names, or empty when it is malformed or names an unknown kind. */
    public static Optional<EntryKey> parse(String s) {
        if (s == null || s.length() > MAX_LENGTH + 16) {
            return Optional.empty();
        }
        int bar = s.indexOf('|');
        if (bar <= 0) {
            return Optional.empty();
        }
        Optional<Kind> kind = Kind.parse(s.substring(0, bar));
        String id = s.substring(bar + 1);
        if (kind.isEmpty() || !ID.matcher(id).matches() || id.length() > MAX_LENGTH) {
            return Optional.empty();
        }
        return Optional.of(new EntryKey(kind.get(), id));
    }

    @Override
    public int compareTo(EntryKey other) {
        int byKind = kind.compareTo(other.kind);
        return byKind != 0 ? byKind : id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return serialized();
    }
}
