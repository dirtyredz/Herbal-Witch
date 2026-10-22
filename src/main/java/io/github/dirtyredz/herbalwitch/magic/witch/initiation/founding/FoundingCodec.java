package io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding;

import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Locale;
import java.util.Optional;

/**
 * The saved founding record: {@code {schema: 1, state, claim?, candidate?, dimension?, origin?,
 * founder?}}. An absent tag is an open world. A tag this version cannot read is preserved unchanged
 * and founding is unavailable until an operator looks at it (never silently reopened).
 */
public final class FoundingCodec {
    public static final int SCHEMA = 1;

    /** What was read: the record, or the preserved tag and why. */
    public record Loaded(FoundingRecord record, Optional<CompoundTag> preserved, Optional<String> problem) {
    }

    private FoundingCodec() {
    }

    public static Loaded read(CompoundTag tag) {
        FoundingRecord record = new FoundingRecord();
        if (tag == null || tag.isEmpty()) {
            return new Loaded(record, Optional.empty(), Optional.empty());
        }
        if (!tag.contains("schema", Tag.TAG_ANY_NUMERIC) || tag.getInt("schema") != SCHEMA) {
            return unreadable(tag, "unsupported or missing schema");
        }
        FoundingRecord.State state;
        try {
            state = FoundingRecord.State.valueOf(tag.getString("state").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return unreadable(tag, "unknown state " + tag.getString("state"));
        }
        boolean attempt = state == FoundingRecord.State.CLAIMED || state == FoundingRecord.State.COMMITTING
                || state == FoundingRecord.State.NEEDS_REVIEW;
        if (attempt && !(tag.hasUUID("claim") && tag.hasUUID("candidate") && tag.contains("dimension", Tag.TAG_STRING)
                && tag.contains("origin", Tag.TAG_LONG))) {
            return unreadable(tag, state + " without its claim");
        }
        RitualSite site = tag.contains("dimension", Tag.TAG_STRING) ? new RitualSite(tag.getString("dimension"), tag.getLong("origin")) : null;
        record.set(state, tag.hasUUID("claim") ? tag.getUUID("claim") : null, tag.hasUUID("candidate") ? tag.getUUID("candidate") : null,
                site, tag.hasUUID("founder") ? tag.getUUID("founder") : null);
        record.loaded();
        return new Loaded(record, Optional.empty(), Optional.empty());
    }

    private static Loaded unreadable(CompoundTag tag, String problem) {
        FoundingRecord closed = new FoundingRecord();
        closed.set(FoundingRecord.State.NEEDS_REVIEW, null, null, null, null);
        return new Loaded(closed, Optional.of(tag.copy()), Optional.of(problem));
    }

    public static CompoundTag write(Loaded loaded) {
        return loaded.preserved().map(CompoundTag::copy).orElseGet(() -> write(loaded.record()));
    }

    public static CompoundTag write(FoundingRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", SCHEMA);
        tag.putString("state", record.state().name().toLowerCase(Locale.ROOT));
        record.claim().ifPresent(id -> tag.putUUID("claim", id));
        record.candidate().ifPresent(id -> tag.putUUID("candidate", id));
        record.site().ifPresent(s -> {
            tag.putString("dimension", s.dimension());
            tag.putLong("origin", s.origin());
        });
        record.founder().ifPresent(id -> tag.putUUID("founder", id));
        return tag;
    }
}
