package io.github.dirtyredz.herbalwitch.companions.animals.care.interaction;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;

/** The shared, side-effect-free refusal order for direct care and adoption. */
public final class AnimalCareAccess {
    public enum Reason {
        READY, DISABLED, UNSUPPORTED, RANGE, PERMISSION, FOREIGN_OWNER, READ_ONLY, HURT, SLEEPING, BUSY,
        WRONG_BAIT, COOLDOWN, MAX_TRUST, CAPACITY, HEALTH_FULL, ALREADY_CALM, DANGER, TRUST, OWNED, NO_BAIT, CANCELED
    }
    private AnimalCareAccess() {}

    public static Reason check(ServerPlayer player, Mob mob, Action action) {
        if (!AnimalServerConfig.current().relationships()) return Reason.DISABLED;
        if (!player.isAlive() || player.isSpectator() || mob.isRemoved() || !mob.isAlive() || player.level() != mob.level()
                || !mob.level().isLoaded(mob.blockPosition()) || !player.canReach(mob, 0) || !player.hasLineOfSight(mob)) return Reason.RANGE;
        var profile = AnimalEligibility.profile(mob);
        if (profile.isEmpty()) return Reason.UNSUPPORTED;
        var cap = AnimalCapability.of(mob);
        if (cap.isEmpty() || !cap.get().writable()) return Reason.READ_ONLY;
        if (!AnimalOwnership.of(mob, profile.get()).allows(player.getUUID())) return Reason.FOREIGN_OWNER;
        if (!EntityActionPermission.check(action, player, mob).allowed()) return Reason.PERMISSION;
        return Reason.READY;
    }

    public static Reason awake(Mob mob) {
        if (ControlEffects.held(mob)) return Reason.SLEEPING;
        if (mob.getTarget() != null) return Reason.BUSY;
        if (AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.hurtRemaining() > 0).orElse(false)) return Reason.HURT;
        return Reason.READY;
    }

    /** Calming/attraction always yield to escape and ordered sitting. */
    public static boolean danger(Mob mob) {
        return mob.isOnFire() || mob.isFreezing() || mob.isInLava() || mob.isInWaterOrBubble()
                || mob.getAirSupply() < mob.getMaxAirSupply() / 2 || mob.isPassenger() || mob.isVehicle()
                || mob instanceof TamableAnimal pet && pet.isOrderedToSit();
    }
}
