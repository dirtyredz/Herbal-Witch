package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * The discovery capability's lifecycle and the server-side observation of acquisitions:
 * <ul>
 *   <li>attached to every server player and saved with them;</li>
 *   <li>copied on every clone (death and returning from the End), with the old player's
 *       capabilities revived for the copy and invalidated after it;</li>
 *   <li>a private snapshot after login, respawn and a dimension change;</li>
 *   <li>the carried inventory observed at login and every {@link #OBSERVE_INTERVAL} ticks (and when
 *       a reader opens), skipped when nothing changed.</li>
 * </ul>
 */
public final class DiscoveryEvents {
    public static final int OBSERVE_INTERVAL = 20;

    private DiscoveryEvents() {
    }

    /** Mod bus. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(DiscoveryCapability.class);
    }

    /** Forge bus. */
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer) {
            DiscoveryProvider provider = new DiscoveryProvider();
            event.addCapability(DiscoveryCapability.ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    /** Forge bus: knowledge survives death and non-death player replacement alike. */
    public static void clone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        try {
            DiscoveryCapability.of(event.getOriginal()).ifPresent(old ->
                    DiscoveryCapability.of(event.getEntity()).ifPresent(copy -> copy.copyFrom(old)));
        } finally {
            event.getOriginal().invalidateCaps();
        }
    }

    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DiscoveryService.sendSnapshot(player);
            DiscoveryService.observe(player);
        }
    }

    public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DiscoveryService.sendSnapshot(player);
        }
    }

    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DiscoveryService.sendSnapshot(player);
        }
    }

    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player
                && (player.tickCount + player.getId()) % OBSERVE_INTERVAL == 0) {
            DiscoveryService.observe(player);
        }
    }
}
