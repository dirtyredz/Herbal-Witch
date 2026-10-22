package io.github.dirtyredz.herbalwitch.companions.animals.care.interaction;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import static io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason.*;

/** Direct use commits once, server-side. No failed operation consumes a preparation or grants trust. */
public final class AnimalCareService {
    public static final float HEALING = 4;
    private AnimalCareService() {}

    public static AnimalCareAccess.Reason feed(ServerPlayer player, Mob mob, InteractionHand hand, Bait bait) {
        var denial = AnimalCareAccess.check(player, mob, Action.ANIMAL_CARE);
        if (denial != READY) return denial;
        if (AnimalCareAccess.awake(mob) != READY) return AnimalCareAccess.awake(mob);
        if (AnimalEligibility.profile(mob).orElseThrow().bait() != bait || !player.getItemInHand(hand).is(AnimalItems.bait(bait))) return WRONG_BAIT;
        var view = AnimalTrustService.relationship(mob, player.getUUID());
        if (view.trust() >= 100) return MAX_TRUST;
        if (view.remaining(AnimalTrustService.clock(mob)) > 0) return COOLDOWN;
        if (!AnimalTrustService.feed(mob, player.getUUID())) return CAPACITY;
        consume(player, hand, ItemStack.EMPTY);
        particles(player, mob);
        return READY;
    }

    public static AnimalCareAccess.Reason heal(ServerPlayer player, Mob mob, InteractionHand hand) {
        var denial = AnimalCareAccess.check(player, mob, Action.ANIMAL_CARE);
        if (denial != READY) return denial;
        if (!player.getItemInHand(hand).is(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE))) return UNSUPPORTED;
        if (mob.getHealth() >= mob.getMaxHealth()) return HEALTH_FULL;
        if (AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.healRemaining() > 0).orElse(false)) return COOLDOWN;
        float before = mob.getHealth();
        mob.heal(HEALING);
        if (mob.getHealth() <= before) return CANCELED;
        AnimalTrustService.healed(mob);
        consume(player, hand, new ItemStack(Items.BOWL));
        return READY;
    }

    public static AnimalCareAccess.Reason calm(ServerPlayer player, Mob mob, InteractionHand hand) {
        var denial = AnimalCareAccess.check(player, mob, Action.ANIMAL_CARE);
        if (denial != READY) return denial;
        if (!AnimalServerConfig.current().calming()) return DISABLED;
        if (!AnimalEligibility.profile(mob).orElseThrow().goalAi() || !mob.getType().is(AnimalTags.CALMABLE)
                || !player.getItemInHand(hand).is(AnimalItems.CALMING.get())) return UNSUPPORTED;
        if (io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects.held(mob)) return SLEEPING;
        if (mob.getTarget() != null) return BUSY;
        if (AnimalCareAccess.danger(mob)) return DANGER;
        var state = AnimalCapability.of(mob).orElseThrow().state();
        if (state.map(s -> s.calmRemaining() > 0).orElse(false)) return ALREADY_CALM;
        if (state.map(s -> s.calmLockoutRemaining() > 0).orElse(false)) return COOLDOWN;
        if (!io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalGoalInstaller.prepareCalm(mob)) return CAPACITY;
        AnimalTrustService.calm(mob, player.getUUID());
        mob.getNavigation().stop();
        consume(player, hand, ItemStack.EMPTY);
        return READY;
    }

    private static void consume(ServerPlayer player, InteractionHand hand, ItemStack container) {
        player.setItemInHand(hand, ContainerReturns.finish(player.getItemInHand(hand), player, container));
    }

    private static void particles(ServerPlayer player, Mob mob) {
        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(),
                4, 0.15, 0.15, 0.15, 0);
    }
}
