package io.github.dirtyredz.herbalwitch.stations.process;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

/** What a processing station is doing, shown in its UI. */
public enum ProcessStatus {
    MISSING_INPUT("missing_input"),
    NEEDS_HEAT("needs_heat"),
    WORKING("working"),
    OUTPUT_BLOCKED("output_blocked");

    private final String id;

    ProcessStatus(String id) {
        this.id = id;
    }

    public String translationKey() {
        return "status." + HerbalWitch.MOD_ID + ".station." + id;
    }

    public static ProcessStatus byOrdinal(int ordinal) {
        ProcessStatus[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MISSING_INPUT;
    }
}
