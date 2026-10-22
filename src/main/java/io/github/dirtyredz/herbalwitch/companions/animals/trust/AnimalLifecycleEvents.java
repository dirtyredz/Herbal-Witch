package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.UUID;

/** Care time advances only on loaded server mobs with initialized state. No neighborhood scans. */
public final class AnimalLifecycleEvents {
    private AnimalLifecycleEvents() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) { event.register(AnimalCapability.class); }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Mob && !event.getObject().level().isClientSide) {
            var provider = new AnimalProvider();
            event.addCapability(AnimalCapability.ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    public static void tick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide) {
            AnimalCapability.of(mob).filter(AnimalCapability::writable).ifPresent(cap -> cap.state().ifPresent(AnimalState::tick));
        }
    }

    /** Registered at LOWEST without canceled events, after armor/absorption. Never allocates a relationship. */
    public static void damaged(LivingDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        Entity actor = event.getSource().getEntity();
        UUID player = actor instanceof Player ? actor.getUUID()
                : actor instanceof OwnableEntity pet ? pet.getOwnerUUID()
                : actor instanceof AbstractHorse horse ? horse.getOwnerUUID() : null;
        if (io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility.profile(mob).isEmpty()) return;
        AnimalCapability.of(mob).filter(AnimalCapability::writable).ifPresent(cap -> {
            cap.initialize().damaged(player);
            cap.changed();
        });
    }
}
