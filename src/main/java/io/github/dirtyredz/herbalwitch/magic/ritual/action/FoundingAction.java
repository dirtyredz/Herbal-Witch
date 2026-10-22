package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessions;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Founding the first lineage: a non-witch, alone, at any altar in any permitted dimension, at any
 * time and moon. The offering (four fresh starter herbs, an amethyst shard and any valid signed book)
 * is captured from the candidate's own inventory, the world's exclusive founding claim is taken on
 * the server thread (a competing attempt fails without consuming anything), and after the ritual's
 * loaded ticks the conversion commits through {@link ConversionCommit}, with the durable claim
 * persisted first. Book contents are never read, broadcast or logged.
 */
public final class FoundingAction implements AltarSessionService.Handler {
    /** What a founding session holds. */
    public record Payload(AltarOffering offering, UUID claim) {
    }

    /** The candidate confirmed "Found a lineage" at the altar; returns the notice suffix. */
    public static String begin(ServerPlayer candidate, AltarMenu menu) {
        MinecraftServer server = candidate.getServer();
        if (server == null) {
            return "busy";
        }
        if (!WitchRules.server().foundingEnabled()) {
            return "founding.disabled";
        }
        if (WitchStatusService.isWitch(candidate)) {
            return "founding.already_witch";
        }
        if (!FoundingService.open(server)) {
            return "founding.closed";
        }
        RitualSite site = AltarSessionService.site(candidate.level(), menu.origin());
        if (AltarSessionService.at(site).isPresent() || AltarSessionService.of(candidate.getUUID()).isPresent()) {
            return "busy";
        }
        Optional<AltarOffering> offering = AltarOffering.capture(candidate, AltarOffering.requirements(AltarActionSpec.FOUNDING));
        if (offering.isEmpty()) {
            return "missing_offering";
        }
        Optional<UUID> claim = FoundingService.claim(server, candidate, site);
        if (claim.isEmpty()) {
            return "founding.closed";
        }
        AltarSessions sessions = AltarSessionService.sessions().orElseThrow();
        AltarSession session = new AltarSession(sessions.nextId(), AltarActionSpec.FOUNDING, site, Optional.of(candidate.getUUID()),
                candidate.getUUID(), WitchRules.server().foundingTicks(), Map.of(candidate.getUUID(), menu.nonce()));
        session.setPayload(new Payload(offering.get(), claim.get()));
        if (AltarSessionService.begin(session).isPresent()) {
            FoundingService.release(server, claim.get());
            return "busy";
        }
        return "founding.begun";
    }

    @Override
    public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> candidate = InitiationEvents.player(session.subject());
        if (candidate.isEmpty() || !p.offering().intact(candidate.get())) {
            return Optional.of("inputs_changed");
        }
        if (!FoundingService.holds(server, p.claim())) {
            return Optional.of("claim_lost");
        }
        return Optional.empty();
    }

    @Override
    public void tick(MinecraftServer server, AltarSession session) {
    }

    @Override
    public void complete(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        ServerPlayer candidate = InitiationEvents.player(session.subject()).orElse(null);
        if (candidate == null) {
            AltarSessionService.cancel(session, "left");
            return;
        }
        ConversionCommit.Outcome outcome = ConversionCommit.commit(server, session, Optional.of(candidate), candidate, p.offering(), 0,
                Optional.of(p.claim()));
        if (session.running()) {
            AltarSessionService.cancel(session, outcome.suffix().substring("result.".length()));
            return;
        }
        AltarSessionService.notice(candidate, outcome == ConversionCommit.Outcome.CONVERTED ? "founding.done" : outcome.suffix());
    }

    @Override
    public void cancelled(MinecraftServer server, AltarSession session, String reason) {
        Payload p = session.payload();
        FoundingService.release(server, p.claim());
    }
}
