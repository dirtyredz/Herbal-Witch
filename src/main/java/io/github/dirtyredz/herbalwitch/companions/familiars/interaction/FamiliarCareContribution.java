package io.github.dirtyredz.herbalwitch.companions.familiars.interaction;

import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareContributions;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.FamiliarBondProgress;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public final class FamiliarCareContribution implements AnimalCareContributions.Contribution {
    @Override public boolean allowCappedFeed(ServerPlayer owner, Mob animal) {
        return FamiliarReconciliation.record(animal).filter(r -> r.owner().equals(owner.getUUID())).isPresent();
    }
    @Override public void completed(ServerPlayer owner, Mob animal, boolean healing) {
        if (!allowCappedFeed(owner, animal)) return;
        if (healing && AnimalCapability.of(animal).flatMap(AnimalCapability::state).map(s -> s.hurtRemaining() > 0).orElse(true)) return;
        FamiliarBondProgress.award(animal, 2);
    }
}
