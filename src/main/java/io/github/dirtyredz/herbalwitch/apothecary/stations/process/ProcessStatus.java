package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

/**
 * What a processing station is doing, shown in its UI. Ordinals travel in menu data, so values are
 * only ever appended (Milestone 7 added the three authorization states after the original four).
 */
public enum ProcessStatus {
    /** Waiting: nothing matches yet. */
    MISSING_INPUT("missing_input"),
    NEEDS_HEAT("needs_heat"),
    WORKING("working"),
    OUTPUT_BLOCKED("output_blocked"),
    /** A magical recipe matches but no witch has started this batch. */
    NEEDS_WITCH("needs_witch"),
    /** The batch's witch is away (out of range, offline, elsewhere) or must resume it. */
    NEEDS_OPERATOR("needs_operator"),
    /** The batch's witch cannot afford it now. */
    NEEDS_ENERGY("needs_energy");

    private final String id;

    ProcessStatus(String id) {
        this.id = id;
    }

    public String translationKey() {
        return "status." + HerbalWitch.MOD_ID + ".station." + id;
    }

    /** Whether the status holds a job back (everything but working). */
    public boolean blocked() {
        return this != WORKING;
    }

    public static ProcessStatus byOrdinal(int ordinal) {
        ProcessStatus[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MISSING_INPUT;
    }
}
