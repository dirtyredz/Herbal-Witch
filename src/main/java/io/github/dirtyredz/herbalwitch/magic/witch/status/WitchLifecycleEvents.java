package io.github.dirtyredz.herbalwitch.magic.witch.status;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * The witch capability's lifecycle:
 * <ul>
 *   <li>attached to every server player and saved with them;</li>
 *   <li>copied exactly once on every clone (death and returning from the End), whatever
 *       {@code keepInventory} says, with the old player's capabilities revived for the copy and
 *       invalidated after it: death neither refills energy nor removes witch status;</li>
 *   <li>a new epoch and full private snapshot after login, respawn and a dimension change;</li>
 *   <li>activities (rest, meditation) cleared on death, logout and dimension change;</li>
 *   <li>pending changes sent every server tick when due.</li>
 * </ul>
 */
public final class WitchLifecycleEvents {
    private WitchLifecycleEvents() {
    }

    /** Mod bus. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WitchCapability.class);
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer) {
            WitchProvider provider = new WitchProvider();
            event.addCapability(WitchCapability.ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    public static void clone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        try {
            WitchCapability.of(event.getOriginal()).ifPresent(old ->
                    WitchCapability.of(event.getEntity()).ifPresent(copy -> copy.copyFrom(old)));
        } finally {
            event.getOriginal().invalidateCaps();
        }
    }

    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WitchStatusService.sendRules(player);
            WitchStatusService.resync(player);
        }
    }

    public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WitchStatusService.resync(player);
        }
    }

    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WitchCapability.of(player).ifPresent(WitchCapability::clearActivities);
            WitchStatusService.resync(player);
        }
    }

    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        WitchCapability.of(event.getEntity()).ifPresent(WitchCapability::clearActivities);
    }

    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WitchCapability.of(player).ifPresent(WitchCapability::clearActivities);
        }
    }

    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            WitchStatusService.tick(player);
        }
    }
}
