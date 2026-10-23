package io.github.dirtyredz.herbalwitch.companions.animals.care.interaction;

import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalState;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import java.util.UUID;

/** Keeps ordinary keepers protected even after a datapack revokes participation. Never grants permission. */
public final class AnimalPermissionPolicy {
    private AnimalPermissionPolicy() {}

    public static Denial check(Action action, UUID actor, LivingEntity target) {
        if (!(target instanceof Mob mob)) return action == Action.HARM || action == Action.ANIMAL_SEDATE ? Denial.NONE : Denial.UNSUPPORTED;
        if (AnimalCapability.of(mob).filter(cap -> !cap.writable()).isPresent()) return Denial.UNSUPPORTED;
        UUID keeper = AnimalCapability.of(mob).flatMap(AnimalCapability::state).flatMap(AnimalState::keeper).orElse(null);
        if (keeper != null && !keeper.equals(actor)) return Denial.FOREIGN_PET;
        if (mob instanceof AbstractHorse horse && horse.getOwnerUUID() != null && !horse.getOwnerUUID().equals(actor))
            return Denial.FOREIGN_PET;
        var known = AnimalProfiles.find(EntityType.getKey(mob.getType()).toString());
        if (known.isPresent() && AnimalOwnership.of(mob, known.get()).conflict()) return Denial.OWNER_CONFLICT;
        if (action == Action.HARM || action == Action.FAMILIAR_HARM) return Denial.NONE;
        // Preserve M7 sleep on other vanilla creatures; participating animals additionally obey M8 ownership.
        if (action == Action.ANIMAL_SEDATE && known.isEmpty() && keeper == null) return Denial.NONE;
        var profile = AnimalEligibility.profile(mob);
        if (profile.isEmpty() || !AnimalOwnership.of(mob, profile.get()).allows(actor)) return Denial.UNSUPPORTED;
        return Denial.NONE;
    }
}
