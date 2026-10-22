package io.github.dirtyredz.herbalwitch.companions.animals.care.taming;

import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfile;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import java.util.UUID;

/** Native ownership is authoritative. A conflict is preserved and refused, never silently repaired. */
public final class AnimalOwnership {
    public record View(UUID owner, boolean conflict, boolean known, boolean nativeOwner) {
        public boolean allows(UUID actor) { return known && !conflict && (owner == null || owner.equals(actor)); }
        public boolean ownedBy(UUID actor) { return known && !conflict && actor.equals(owner); }
    }

    private AnimalOwnership() {}

    public static View of(Mob mob, AnimalProfile profile) {
        UUID nativeId = mob instanceof OwnableEntity pet ? pet.getOwnerUUID()
                : mob instanceof AbstractHorse horse ? horse.getOwnerUUID() : null;
        UUID keeper = AnimalCapability.of(mob).flatMap(AnimalCapability::state).flatMap(AnimalState::keeper).orElse(null);
        boolean conflict = nativeId != null && keeper != null && !nativeId.equals(keeper);
        return new View(nativeId == null ? keeper : nativeId, conflict,
                profile.ownership() != AnimalProfile.Ownership.UNKNOWN, nativeId != null);
    }
}
