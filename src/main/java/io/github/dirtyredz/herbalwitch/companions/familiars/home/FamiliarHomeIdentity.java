package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import java.util.UUID;

/** The command reader needs identity and compatibility, never the home's repairable occupant cache. */
public interface FamiliarHomeIdentity {
    UUID instance();
    UUID owner();
    boolean accepts(String species);
}
