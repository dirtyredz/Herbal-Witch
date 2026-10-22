package io.github.dirtyredz.herbalwitch.companions.animals.care.taming;

import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.TrustRules;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import static io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason.*;

/** Deterministic additional taming route. Vanilla food and native follow/sit/riding behavior are untouched. */
public final class AnimalTamingService {
    private AnimalTamingService() {}

    public static AnimalCareAccess.Reason requirements(ServerPlayer player, Mob mob) {
        var denial = AnimalCareAccess.check(player, mob, Action.ANIMAL_ADOPT);
        if (denial != READY) return denial;
        if (!AnimalServerConfig.current().adoption()) return DISABLED;
        var profile = AnimalEligibility.profile(mob).orElseThrow();
        if (!profile.adoptable()) return UNSUPPORTED;
        if (AnimalOwnership.of(mob, profile).owner() != null) return OWNED;
        if (AnimalCareAccess.awake(mob) != READY) return AnimalCareAccess.awake(mob);
        if (!TrustRules.earned(AnimalTrustService.relationship(mob, player.getUUID()), profile.nativeTaming())) return TRUST;
        if (bait(player, mob).isEmpty()) return NO_BAIT;
        return READY;
    }

    public static AnimalCareAccess.Reason adopt(ServerPlayer player, Mob mob) {
        var reason = requirements(player, mob);
        if (reason != READY) return reason;
        var profile = AnimalEligibility.profile(mob).orElseThrow();
        if (!(mob instanceof Animal animal)) return UNSUPPORTED;
        if (profile.nativeTaming() && ForgeEventFactory.onAnimalTame(animal, player)) return CANCELED;
        // Event handlers may have changed inventory, ownership, location, tags or state.
        reason = requirements(player, mob);
        if (reason != READY) return reason;
        ItemStack bait = bait(player, mob);
        if (profile.nativeTaming()) {
            if (mob instanceof TamableAnimal pet) pet.tame(player);
            else if (mob instanceof AbstractHorse horse) horse.tameWithName(player);
            else return UNSUPPORTED;
        } else if (!AnimalTrustService.keeper(mob, player.getUUID())) return CANCELED;
        mob.setPersistenceRequired();
        if (!player.getAbilities().instabuild) bait.shrink(1);
        return READY;
    }

    private static ItemStack bait(ServerPlayer player, Mob mob) {
        var item = AnimalItems.bait(AnimalEligibility.profile(mob).orElseThrow().bait());
        for (ItemStack stack : player.getInventory().items) if (stack.is(item)) return stack;
        for (ItemStack stack : player.getInventory().offhand) if (stack.is(item)) return stack;
        return ItemStack.EMPTY;
    }
}
