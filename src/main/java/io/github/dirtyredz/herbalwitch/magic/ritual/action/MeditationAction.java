package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessions;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyRecovery;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

/**
 * Meditation at an idle altar, explicitly started: after a settling period it restores energy at an
 * interval (raised by nature and a visible full moon), while the witch keeps still, keeps the altar
 * menu open and stays unhurt; it stops by itself at full capacity. Passive rest does not run at the
 * same time. Costs nothing and earns no practice.
 */
public final class MeditationAction implements AltarSessionService.Handler {
    /** Where the witch sat down. */
    public record Payload(Vec3 anchor) {
    }

    public static String begin(ServerPlayer witch, AltarMenu menu) {
        if (!WitchStatusService.isWitch(witch)) {
            return "meditation.not_witch";
        }
        if (WitchStatusService.energyUnits(witch) >= WitchStatusService.capacityUnits(witch)) {
            return "meditation.full";
        }
        RitualSite site = AltarSessionService.site(witch.level(), menu.origin());
        if (AltarSessionService.at(site).isPresent() || AltarSessionService.of(witch.getUUID()).isPresent()) {
            return "busy";
        }
        AltarSessions sessions = AltarSessionService.sessions().orElseThrow();
        AltarSession session = new AltarSession(sessions.nextId(), AltarActionSpec.MEDITATION, site, Optional.empty(), witch.getUUID(), 0,
                Map.of(witch.getUUID(), menu.nonce()));
        session.setPayload(new Payload(witch.position()));
        if (AltarSessionService.begin(session).isPresent()) {
            return "busy";
        }
        WitchCapability.of(witch).ifPresent(c -> c.setMeditating(true));
        return "meditation.begun";
    }

    @Override
    public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> witch = InitiationEvents.player(session.subject());
        if (witch.isEmpty()) {
            return Optional.of("left");
        }
        ServerPlayer w = witch.get();
        if (w.position().distanceToSqr(p.anchor()) > EnergyRecovery.REST_RADIUS * EnergyRecovery.REST_RADIUS || w.isSprinting()
                || w.isSwimming() || w.getAbilities().flying || w.isFallFlying() || w.isPassenger() || w.isUsingItem()) {
            return Optional.of("moved");
        }
        return Optional.empty();
    }

    @Override
    public void tick(MinecraftServer server, AltarSession session) {
        EnergyBalance b = WitchRules.server().energy();
        if (!EnergyRecovery.meditationGrant(session.elapsed(), b.meditationSettle(), b.meditationInterval())) {
            return;
        }
        ServerPlayer witch = InitiationEvents.player(session.subject()).orElse(null);
        if (witch == null) {
            return;
        }
        int pct = WitchCapability.of(witch).map(c -> EnergyEvents.bonus(witch, c)).orElse(0);
        WitchStatusService.recover(witch, EnergyRecovery.withBonus(EnergyService.units(b.meditationAmount()), pct));
        if (WitchStatusService.energyUnits(witch) >= WitchStatusService.capacityUnits(witch) && AltarSessionService.completeOnce(session)) {
            WitchCapability.of(witch).ifPresent(c -> c.setMeditating(false));
            WitchStatusService.flush(witch);
            AltarSessionService.notice(witch, "meditation.full");
        }
    }

    @Override
    public void complete(MinecraftServer server, AltarSession session) {
        // open-ended: it ends at full capacity or when interrupted
    }

    @Override
    public void cancelled(MinecraftServer server, AltarSession session, String reason) {
        InitiationEvents.player(session.subject()).flatMap(WitchCapability::of).ifPresent(c -> c.setMeditating(false));
    }
}
