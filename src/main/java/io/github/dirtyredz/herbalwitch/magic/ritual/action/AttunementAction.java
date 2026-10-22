package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessions;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.PracticeProgress;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;

/**
 * Attunement, the optional solo practice: a fresh sage and a little energy after an uninterrupted
 * stay at the altar. Its result is practice towards the next capacity threshold and a visible
 * completion; at full practice it still works and says it brings no further capacity. Consumes
 * nothing unless it completes; practice is awarded only after the spend succeeds.
 */
public final class AttunementAction implements AltarSessionService.Handler {
    /** What an attunement session holds. */
    public record Payload(AltarOffering offering, int energyUnits) {
    }

    public static String begin(ServerPlayer witch, AltarMenu menu) {
        if (!WitchStatusService.isWitch(witch)) {
            return "attunement.not_witch";
        }
        int units = EnergyService.units(WitchRules.server().energy().attunementCost());
        if (WitchStatusService.check(witch, units) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        Optional<AltarOffering> offering = AltarOffering.capture(witch, AltarOffering.requirements(AltarActionSpec.ATTUNEMENT));
        if (offering.isEmpty()) {
            return "missing_offering";
        }
        RitualSite site = AltarSessionService.site(witch.level(), menu.origin());
        if (AltarSessionService.at(site).isPresent() || AltarSessionService.of(witch.getUUID()).isPresent()) {
            return "busy";
        }
        AltarSessions sessions = AltarSessionService.sessions().orElseThrow();
        AltarSession session = new AltarSession(sessions.nextId(), AltarActionSpec.ATTUNEMENT, site, Optional.of(witch.getUUID()),
                witch.getUUID(), WitchRules.server().attunementTicks(), Map.of(witch.getUUID(), menu.nonce()));
        session.setPayload(new Payload(offering.get(), units));
        return AltarSessionService.begin(session).isPresent() ? "busy" : "attunement.begun";
    }

    @Override
    public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> witch = InitiationEvents.player(session.subject());
        if (witch.isEmpty() || !p.offering().intact(witch.get())) {
            return Optional.of("inputs_changed");
        }
        if (WitchStatusService.check(witch.get(), p.energyUnits()) != EnergyService.Result.OK) {
            return Optional.of("not_enough_energy");
        }
        return Optional.empty();
    }

    @Override
    public void tick(MinecraftServer server, AltarSession session) {
    }

    @Override
    public void complete(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        ServerPlayer witch = InitiationEvents.player(session.subject()).orElse(null);
        if (witch == null || !p.offering().intact(witch)) {
            AltarSessionService.cancel(session, "inputs_changed");
            return;
        }
        if (WitchStatusService.check(witch, p.energyUnits()) != EnergyService.Result.OK) {
            AltarSessionService.cancel(session, "not_enough_energy");
            return;
        }
        if (!AltarSessionService.completeOnce(session)) {
            return;
        }
        p.offering().consume(witch);
        WitchStatusService.debit(witch, p.energyUnits());
        WitchStatusService.awardPractice(witch, p.energyUnits());
        DiscoveryService.grant(witch, AltarEntries.key(AltarActionSpec.ATTUNEMENT));
        WitchStatusService.flush(witch);
        boolean complete = WitchCapability.of(witch)
                .map(c -> PracticeProgress.complete(WitchRules.server().energy(), c.state().practiceSpent())).orElse(false);
        AltarSessionService.notice(witch, complete ? "attunement.done_max" : "attunement.done");
    }

    @Override
    public void cancelled(MinecraftServer server, AltarSession session, String reason) {
        // nothing was consumed
    }
}
