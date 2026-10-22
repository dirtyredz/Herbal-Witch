package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;

/** The working rules the ward package reads (radius, height, charge, limits); the one place it reaches the config. */
public final class WardContext {
    private WardContext() { }

    public static WorkingRules rules() {
        return WitchRules.server().workings();
    }
}
