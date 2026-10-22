package io.github.dirtyredz.herbalwitch.magic.witch.registry;

import io.github.dirtyredz.herbalwitch.magic.witch.command.WitchCommandRegistration;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchServerConfig;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchPackets;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchLifecycleEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Wires witch status and energy (Milestone 6): the server configuration, the personal capability
 * and its lifecycle, the two client messages, energy over time and sleep, initiation consent,
 * founding closure on the first conversion, the operator commands, and what an accepted rules change
 * does (end pending consent, clamp everyone's energy without refilling, resend the rules). The
 * grimoire's witchcraft pages are a grimoire content source; rituals live in {@code magic/ritual}.
 */
public final class WitchRegistration {
    private WitchRegistration() {
    }

    /** Called once from the mod constructor, after the grimoire (message ids follow registration order). */
    public static void register(IEventBus modBus) {
        WitchServerConfig.register(modBus);
        modBus.addListener(WitchLifecycleEvents::registerCapabilities);
        WitchPackets.register();
        WitchStatusService.onConversion(FoundingService::conversionCompleted);
        WitchRules.listen(InitiationEvents::rulesChanged);
        WitchRules.listen(rules -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.getPlayerList().getPlayers().forEach(WitchStatusService::rulesChanged);
            }
        });

        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addGenericListener(Entity.class, WitchLifecycleEvents::attach);
        forge.addListener(WitchLifecycleEvents::clone);
        forge.addListener(WitchLifecycleEvents::loggedIn);
        forge.addListener(WitchLifecycleEvents::respawned);
        forge.addListener(WitchLifecycleEvents::changedDimension);
        forge.addListener(WitchLifecycleEvents::loggedOut);
        forge.addListener(WitchLifecycleEvents::died);
        forge.addListener(EnergyEvents::playerTick);
        forge.addListener(WitchLifecycleEvents::playerTick);
        forge.addListener(EnergyEvents::hurt);
        forge.addListener(EnergyEvents::sleepFinished);
        forge.addListener(InitiationEvents::serverStarted);
        forge.addListener(InitiationEvents::serverStopped);
        forge.addListener(InitiationEvents::serverTick);
        forge.addListener(InitiationEvents::loggedOut);
        forge.addListener(InitiationEvents::died);
        forge.addListener(InitiationEvents::changedDimension);
        forge.addListener(WitchCommandRegistration::onRegisterCommands);
        forge.addListener((ServerStoppedEvent event) -> WitchRules.resetServer());
    }
}
