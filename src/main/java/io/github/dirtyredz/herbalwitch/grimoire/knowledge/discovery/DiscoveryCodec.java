package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.TreeSet;

/**
 * The saved form of a player's discoveries, separate from the unlock policy:
 * {@code {schema: 1, keys: ["recipe|herbalwitch:processing/...", ...]}}, keys sorted so a save is
 * deterministic. Reading never loses data: an absent or empty tag (a player from before the
 * grimoire) migrates to an empty schema-1 state; keys of an unknown kind are retained verbatim; a
 * malformed tag, an unsupported schema or more keys than the ceiling is kept exactly as saved and
 * the state becomes read-only, so this version never overwrites what it cannot read.
 */
public final class DiscoveryCodec {
    public static final int SCHEMA = 1;
    public static final String SCHEMA_KEY = "schema";
    public static final String KEYS = "keys";

    /**
     * What was read.
     *
     * @param state     the knowledge (read-only when {@code preserved} is present)
     * @param preserved the saved tag to write back unchanged, when it could not be read
     * @param problem   why it could not be read, for the log
     */
    public record Loaded(DiscoveryState state, Optional<CompoundTag> preserved, Optional<String> problem) {
        static Loaded ok(DiscoveryState state) {
            return new Loaded(state, Optional.empty(), Optional.empty());
        }

        static Loaded unreadable(CompoundTag tag, String problem) {
            return new Loaded(DiscoveryState.readOnly(), Optional.of(tag.copy()), Optional.of(problem));
        }
    }

    private DiscoveryCodec() {
    }

    public static Loaded read(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return Loaded.ok(new DiscoveryState()); // migration: no grimoire data yet
        }
        if (!tag.contains(SCHEMA_KEY, Tag.TAG_ANY_NUMERIC)) {
            return Loaded.unreadable(tag, "no schema version");
        }
        int schema = tag.getInt(SCHEMA_KEY);
        if (schema != SCHEMA) {
            return Loaded.unreadable(tag, "unsupported schema " + schema + " (this version reads " + SCHEMA + ")");
        }
        if (!(tag.get(KEYS) instanceof ListTag keys)) {
            return Loaded.unreadable(tag, "no key list");
        }
        if (!keys.isEmpty() && keys.getElementType() != Tag.TAG_STRING) {
            return Loaded.unreadable(tag, "keys are not strings");
        }
        if (keys.size() > DiscoveryState.MAX_DISCOVERIES) {
            return Loaded.unreadable(tag, keys.size() + " keys exceed the " + DiscoveryState.MAX_DISCOVERIES + " ceiling");
        }
        DiscoveryState state = new DiscoveryState();
        for (int i = 0; i < keys.size(); i++) {
            String raw = keys.getString(i);
            Optional<EntryKey> key = EntryKey.parse(raw);
            if (key.isPresent()) {
                state.grant(key.get());
            } else {
                state.retain(raw);
            }
        }
        return Loaded.ok(state);
    }

    /** The saved form: the preserved tag unchanged, or schema 1 with every key sorted. */
    public static CompoundTag write(Loaded loaded) {
        if (loaded.preserved().isPresent()) {
            return loaded.preserved().get().copy();
        }
        return write(loaded.state());
    }

    public static CompoundTag write(DiscoveryState state) {
        TreeSet<String> all = new TreeSet<>(state.retained());
        state.known().forEach(k -> all.add(k.serialized()));
        ListTag keys = new ListTag();
        all.forEach(s -> keys.add(StringTag.valueOf(s)));
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_KEY, SCHEMA);
        tag.put(KEYS, keys);
        return tag;
    }
}
