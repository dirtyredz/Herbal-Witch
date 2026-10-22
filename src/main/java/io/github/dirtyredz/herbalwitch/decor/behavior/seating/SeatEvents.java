package io.github.dirtyredz.herbalwitch.decor.behavior.seating;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Seat cleanup the seat itself cannot see coming: a sitter who logs out stands up first (vanilla
 * would otherwise save the seat as the player's vehicle), and one who dies leaves the seat at once.
 * Dimension changes dismount through vanilla; an empty seat removes itself.
 */
public final class SeatEvents {
    private SeatEvents() {
    }

    public static void register(IEventBus forgeBus) {
        forgeBus.addListener(SeatEvents::loggedOut);
        forgeBus.addListener(SeatEvents::died);
    }

    private static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        stand(event.getEntity());
    }

    private static void died(LivingDeathEvent event) {
        stand(event.getEntity());
    }

    private static void stand(Entity entity) {
        if (entity.getVehicle() instanceof SeatEntity seat) {
            entity.stopRiding();
            seat.discard();
        }
    }
}
