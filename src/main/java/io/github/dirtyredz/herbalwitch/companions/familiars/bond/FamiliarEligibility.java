package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;

public final class FamiliarEligibility {
    private FamiliarEligibility() {}
    public static String refusal(ServerPlayer player, Mob mob, BlockPos altar) {
        var store = FamiliarStore.get(player.server);
        if (!store.ready()) return "indexing";
        if (store.creature(mob.getUUID()).isPresent()) return "already_bound";
        var species = FamiliarCatalog.type(EntityType.getKey(mob.getType()).toString()).orElse(null);
        var profile = AnimalEligibility.profile(mob).orElse(null);
        if (species == null || profile == null) return "species";
        var rules = io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.current();
        if (!rules.binding() || !rules.species().contains(species.key())) return "disabled";
        if (player.level() != mob.level() || !mob.level().isLoaded(mob.blockPosition()) || !player.isAlive() || player.isSpectator()
                || !mob.isAlive() || mob.isRemoved() || mob.isNoAi() || player.distanceToSqr(Vec3.atCenterOf(altar)) > 16
                || mob.distanceToSqr(Vec3.atCenterOf(altar)) > 16 || !player.hasLineOfSight(mob)) return "range";
        if (mob instanceof AgeableMob ageable && ageable.isBaby() || mob.isPassenger() || mob.isVehicle() || mob.isLeashed()
                || mob.getTarget() != null || mob.isOnFire() || mob.isInLava() || mob.isFreezing() || mob.getAirSupply() < mob.getMaxAirSupply() / 2
                || ControlEffects.held(mob) || AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.hurtRemaining() > 0).orElse(false)) return "danger";
        if (AnimalCapability.of(mob).filter(AnimalCapability::writable).isEmpty()) return "care_read_only";
        var owner = AnimalOwnership.of(mob, profile);
        if (!owner.allows(player.getUUID()) || !species.key().equals("bat") && !owner.ownedBy(player.getUUID())) return "owner";
        var relationship = AnimalTrustService.relationship(mob, player.getUUID());
        if (relationship.trust() < species.trust() || relationship.feeds() < species.feeds()) return "trust";
        if (!EntityActionPermission.check(EntityActionContext.Action.ANIMAL_CARE, player, mob).allowed()) return "permission";
        var captures = CaptureSavedData.get(player.server);
        if (!captures.writable()) return "capture_read_only";
        var capture = captures.record(mob.getUUID()).orElse(null);
        if (capture != null && (capture.phase() != CaptureRecord.Phase.RELEASED || capture.storedPayload()
                || capture.generation() != CaptureReconciliation.generation(mob) || !capture.type().equals(EntityType.getKey(mob.getType()).toString()))) return "capture_lineage";
        return null;
    }
}
