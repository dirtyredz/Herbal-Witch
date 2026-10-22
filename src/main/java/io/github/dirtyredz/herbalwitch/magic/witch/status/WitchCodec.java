package io.github.dirtyredz.herbalwitch.magic.witch.status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

/**
 * The saved form of a player's witch state, separate from the rules:
 * {@code {schema: 1, initiated, initiation?, energy, practice, tea, restful, sleepDay}}. Reading never
 * loses data: an absent or empty tag (a player from before Milestone 6) is a fresh non-witch; a tag
 * without a schema, with an unsupported schema, or with a malformed field is kept exactly as saved,
 * the player is treated as a non-witch whose state cannot change, and the problem is reported, so
 * this version never overwrites what it cannot read.
 */
public final class WitchCodec {
    public static final int SCHEMA = 1;
    public static final String SCHEMA_KEY = "schema";
    static final String INITIATED = "initiated";
    static final String INITIATION = "initiation";
    static final String ENERGY = "energy";
    static final String PRACTICE = "practice";
    static final String TEA = "tea";
    static final String RESTFUL = "restful";
    static final String SLEEP_DAY = "sleepDay";
    /** Sanity ceilings for saved numbers (far above any valid configuration). */
    static final int MAX_UNITS = 100_000_000;
    static final int MAX_TICKS = 100_000_000;

    /**
     * What was read.
     *
     * @param state     the state (a fresh non-witch when {@code preserved} is present)
     * @param preserved the saved tag to write back unchanged, when it could not be read
     * @param problem   why it could not be read, for the log and the inspect command
     */
    public record Loaded(WitchState state, Optional<CompoundTag> preserved, Optional<String> problem) {
        static Loaded ok(WitchState state) {
            return new Loaded(state, Optional.empty(), Optional.empty());
        }

        static Loaded unreadable(CompoundTag tag, String problem) {
            return new Loaded(new WitchState(), Optional.of(tag.copy()), Optional.of(problem));
        }

        /** Whether this state may change (false for preserved, unreadable data). */
        public boolean writable() {
            return preserved.isEmpty();
        }

        /** An independent copy (player cloning). */
        public Loaded copy() {
            return new Loaded(state.copy(), preserved.map(CompoundTag::copy), problem);
        }
    }

    private WitchCodec() {
    }

    public static Loaded read(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return Loaded.ok(new WitchState());
        }
        if (!tag.contains(SCHEMA_KEY, Tag.TAG_ANY_NUMERIC)) {
            return Loaded.unreadable(tag, "no schema version");
        }
        int schema = tag.getInt(SCHEMA_KEY);
        if (schema != SCHEMA) {
            return Loaded.unreadable(tag, "unsupported schema " + schema + " (this version reads " + SCHEMA + ")");
        }
        for (String key : new String[] {ENERGY, PRACTICE, TEA, RESTFUL}) {
            if (!tag.contains(key, Tag.TAG_INT)) {
                return Loaded.unreadable(tag, "missing or malformed " + key);
            }
        }
        if (!tag.contains(INITIATED, Tag.TAG_BYTE) || !tag.contains(SLEEP_DAY, Tag.TAG_LONG)) {
            return Loaded.unreadable(tag, "missing or malformed status fields");
        }
        int energy = tag.getInt(ENERGY);
        int practice = tag.getInt(PRACTICE);
        int tea = tag.getInt(TEA);
        int restful = tag.getInt(RESTFUL);
        long sleepDay = tag.getLong(SLEEP_DAY);
        if (energy < 0 || energy > MAX_UNITS || practice < 0 || practice > MAX_UNITS || tea < 0 || tea > MAX_TICKS
                || restful < 0 || restful > MAX_TICKS || sleepDay < WitchState.NEVER) {
            return Loaded.unreadable(tag, "a field is out of range");
        }
        boolean initiated = tag.getBoolean(INITIATED);
        boolean hasId = tag.hasUUID(INITIATION);
        if (initiated != hasId) {
            return Loaded.unreadable(tag, initiated ? "a witch without an initiation id" : "an initiation id without witch status");
        }
        if (!initiated && (energy != 0 || practice != 0)) {
            return Loaded.unreadable(tag, "energy or practice on a non-witch");
        }
        WitchState state = new WitchState();
        if (initiated) {
            state.initiate(tag.getUUID(INITIATION));
        }
        state.setEnergyUnits(energy);
        state.setPracticeSpent(practice);
        state.setTeaCooldownTicks(tea);
        state.setRestfulTicks(restful);
        state.setLastSleepRewardDay(sleepDay);
        return Loaded.ok(state);
    }

    /** The saved form: the preserved tag unchanged, or schema 1. */
    public static CompoundTag write(Loaded loaded) {
        return loaded.preserved().map(CompoundTag::copy).orElseGet(() -> write(loaded.state()));
    }

    public static CompoundTag write(WitchState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_KEY, SCHEMA);
        tag.putBoolean(INITIATED, state.initiated());
        state.initiationId().ifPresent(id -> tag.putUUID(INITIATION, id));
        tag.putInt(ENERGY, state.energyUnits());
        tag.putInt(PRACTICE, state.practiceSpent());
        tag.putInt(TEA, state.teaCooldownTicks());
        tag.putInt(RESTFUL, state.restfulTicks());
        tag.putLong(SLEEP_DAY, state.lastSleepRewardDay());
        return tag;
    }
}
