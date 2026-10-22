package io.github.dirtyredz.herbalwitch.companions.animals.species;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import java.util.List;
import java.util.Optional;
import static io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfile.Ownership.*;

/** Exact registry identities, never inheritance: trader llamas and mooshrooms are not in this list. */
public final class AnimalProfiles {
    public static final List<AnimalProfile> ALL = List.of(
            profile("cow", Bait.GRAIN, KEEPER, true, true),
            profile("sheep", Bait.GRAIN, KEEPER, true, true),
            profile("goat", Bait.GRAIN, KEEPER, false, true),
            profile("pig", Bait.ROOT, KEEPER, true, true),
            profile("rabbit", Bait.ROOT, KEEPER, true, true),
            profile("chicken", Bait.SEED, KEEPER, true, true),
            profile("horse", Bait.GRAIN, HORSE, true, true),
            profile("donkey", Bait.GRAIN, HORSE, true, true),
            profile("mule", Bait.GRAIN, HORSE, true, true),
            profile("llama", Bait.GRAIN, HORSE, true, true),
            profile("cat", Bait.FISH, TAMEABLE, true, true),
            profile("wolf", Bait.MEAT, TAMEABLE, true, true),
            profile("fox", Bait.BERRY, KEEPER, true, true),
            profile("parrot", Bait.SEED, TAMEABLE, true, true),
            profile("axolotl", Bait.FISH, KEEPER, false, true),
            profile("bat", Bait.BERRY, TRUST_ONLY, false, false));

    private AnimalProfiles() {}
    private static AnimalProfile profile(String id, Bait bait, AnimalProfile.Ownership owner, boolean ai, boolean capture) {
        return new AnimalProfile("minecraft:" + id, bait, owner, ai, capture);
    }
    public static Optional<AnimalProfile> find(String id) { return ALL.stream().filter(p -> p.id().equals(id)).findFirst(); }
}
