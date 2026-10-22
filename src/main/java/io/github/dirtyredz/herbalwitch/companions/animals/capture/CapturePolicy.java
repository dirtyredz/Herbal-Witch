package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.payload.VanillaCaptureAdapters;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.TrustRules;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.util.FakePlayer;

public final class CapturePolicy {
    private CapturePolicy() {}
    public static String check(ServerPlayer player, Mob mob, InteractionHand hand) {
        if (player instanceof FakePlayer || player.isSpectator() || !player.isAlive()) return "actor";
        if (!AnimalServerConfig.current().captures()) return "disabled";
        var stack = player.getItemInHand(hand);
        if (!stack.is(AnimalItems.CHARM.get()) || stack.getCount() != 1) return "item";
        if (AnimalCareAccess.check(player, mob, Action.ANIMAL_CAPTURE) != AnimalCareAccess.Reason.READY) return "permission";
        if (!VanillaCaptureAdapters.supported(mob)) return "unsupported";
        var profile = AnimalEligibility.profile(mob).orElseThrow();
        var owner = AnimalOwnership.of(mob, profile);
        if (!owner.allows(player.getUUID())) return "owner";
        if (!owner.ownedBy(player.getUUID()) && !TrustRules.earned(AnimalTrustService.relationship(mob, player.getUUID()), false)) return "trust";
        var state = AnimalCapability.of(mob).orElseThrow().state();
        if (mob.getTarget() != null || mob.isLeashed() || mob.isPassenger() || mob.isVehicle() || mob.isOnFire() || mob.isInLava()
                || mob.isFreezing() || mob.getAirSupply() < mob.getMaxAirSupply() / 2 || state.map(s -> s.hurtRemaining() > 0).orElse(false)) return "danger";
        if (!mob.hasEffect(PreparationEffects.SLEEP.get()) && !state.map(s -> s.calmRemaining() > 0).orElse(false)) return "settle";
        return null;
    }
}
