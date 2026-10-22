package io.github.dirtyredz.herbalwitch.companions.animals.species;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;

/** Tested behavior/serialization families. Tags can remove support, but cannot manufacture an adapter. */
public record AnimalProfile(String id, Bait bait, Ownership ownership, boolean goalAi, boolean capturable) {
    public enum Ownership { KEEPER, TAMEABLE, HORSE, TRUST_ONLY, UNKNOWN }
    public boolean nativeTaming() { return ownership == Ownership.TAMEABLE || ownership == Ownership.HORSE; }
    public boolean adoptable() { return ownership != Ownership.TRUST_ONLY && ownership != Ownership.UNKNOWN; }
}
