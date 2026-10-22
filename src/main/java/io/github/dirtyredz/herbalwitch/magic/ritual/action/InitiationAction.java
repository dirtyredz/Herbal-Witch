package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarAccess;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessions;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationConsent;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationOffer;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The altar side of consenting initiation. A witch at the altar invites a nearby non-witch who can
 * also use it (consent rules: {@code witch/initiation}); the recipient opens the same altar and
 * explicitly accepts or declines. Acceptance revalidates both players, the altar, the session,
 * status, the initiator's offering (fresh sage, fresh lavender, an amethyst shard) and energy, then
 * takes the invitation and starts a ritual both must stay with (menus open, in range, unhurt). At the
 * end the conversion commits once; the recipient pays nothing. An operator's designation is accepted
 * the same way at any valid altar, without offering, energy or initiator.
 */
public final class InitiationAction implements AltarSessionService.Handler {
    /** What an initiation session holds. */
    public record Payload(AltarOffering offering, InitiationOffer offer, int energyUnits) {
    }

    /** A witch invites {@code target} from this altar; returns the notice suffix. */
    public static String invite(ServerPlayer initiator, AltarMenu menu, UUID target) {
        Optional<ServerPlayer> recipient = InitiationEvents.player(target);
        if (recipient.isEmpty() || !AltarAccess.mayUse(recipient.get(), menu.origin())) {
            return "initiation.not_near";
        }
        if (!WitchStatusService.isWitch(initiator)) {
            return "initiation.not_witch";
        }
        int units = EnergyService.units(WitchRules.server().energy().initiationCost());
        if (WitchStatusService.check(initiator, units) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        if (AltarOffering.capture(initiator, AltarOffering.requirements(AltarActionSpec.INITIATION)).isEmpty()) {
            return "missing_offering";
        }
        RitualSite site = AltarSessionService.site(initiator.level(), menu.origin());
        InitiationConsent.Result result = InitiationEvents.invite(initiator, recipient.get(), site);
        return "consent." + result.name().toLowerCase(java.util.Locale.ROOT);
    }

    public static String withdraw(ServerPlayer initiator) {
        Optional<InitiationOffer> offer = InitiationEvents.consent().flatMap(c -> c.outgoing(initiator.getUUID()));
        InitiationConsent.Result result = InitiationEvents.consent().map(c -> c.withdraw(initiator.getUUID()))
                .orElse(InitiationConsent.Result.NO_INVITATION);
        offer.ifPresent(o -> InitiationEvents.ended(java.util.List.of(o), result));
        return "consent." + result.name().toLowerCase(java.util.Locale.ROOT);
    }

    public static String decline(ServerPlayer recipient, long offerId) {
        Optional<InitiationOffer> offer = InitiationEvents.consent().flatMap(c -> c.incoming(recipient.getUUID()));
        InitiationConsent.Result result = InitiationEvents.consent().map(c -> c.decline(recipient.getUUID(), offerId))
                .orElse(InitiationConsent.Result.NO_INVITATION);
        if (result == InitiationConsent.Result.DECLINED) {
            offer.ifPresent(o -> InitiationEvents.ended(java.util.List.of(o), result));
        }
        return "consent." + result.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** The recipient chose "Accept and begin" for invitation {@code offerId} at this altar. */
    public static String accept(ServerPlayer recipient, AltarMenu menu, long offerId) {
        Optional<InitiationConsent> consent = InitiationEvents.consent();
        if (consent.isEmpty()) {
            return "consent.no_invitation";
        }
        InitiationConsent.Result ready = consent.get().check(recipient.getUUID(), offerId);
        if (ready != InitiationConsent.Result.READY) {
            return "consent." + ready.name().toLowerCase(java.util.Locale.ROOT);
        }
        InitiationOffer offer = consent.get().incoming(recipient.getUUID()).orElseThrow();
        RitualSite site = AltarSessionService.site(recipient.level(), menu.origin());
        if (AltarSessionService.at(site).isPresent() || AltarSessionService.of(recipient.getUUID()).isPresent()) {
            return "busy";
        }
        Map<UUID, Long> menus = new LinkedHashMap<>();
        Optional<ServerPlayer> payer = Optional.empty();
        AltarOffering offering = AltarOffering.none();
        int units = 0;
        if (!offer.administrative()) {
            if (!offer.site().map(site::equals).orElse(false)) {
                return "initiation.other_altar";
            }
            if (!WitchRules.server().initiationEnabled()) {
                return "consent.disabled";
            }
            ServerPlayer initiator = offer.initiator().flatMap(InitiationEvents::player).orElse(null);
            if (initiator == null || !(initiator.containerMenu instanceof AltarMenu theirs) || !theirs.origin().equals(menu.origin())
                    || !theirs.stillValid(initiator)) {
                return "initiation.initiator_away";
            }
            if (!WitchStatusService.isWitch(initiator) || AltarSessionService.of(initiator.getUUID()).isPresent()) {
                return "initiation.initiator_away";
            }
            units = EnergyService.units(WitchRules.server().energy().initiationCost());
            if (WitchStatusService.check(initiator, units) != EnergyService.Result.OK) {
                return "not_enough_energy";
            }
            Optional<AltarOffering> captured = AltarOffering.capture(initiator, AltarOffering.requirements(AltarActionSpec.INITIATION));
            if (captured.isEmpty()) {
                return "missing_offering";
            }
            offering = captured.get();
            payer = Optional.of(initiator);
            menus.put(initiator.getUUID(), theirs.nonce());
        }
        if (!offering.giftFits(recipient, false)) {
            return "cancel.no_room";
        }
        menus.put(recipient.getUUID(), menu.nonce());
        Optional<InitiationOffer> taken = consent.get().take(recipient.getUUID(), offerId);
        if (taken.isEmpty()) {
            return "consent.no_invitation";
        }
        AltarSessions sessions = AltarSessionService.sessions().orElseThrow();
        AltarSession session = new AltarSession(sessions.nextId(), AltarActionSpec.INITIATION, site, payer.map(ServerPlayer::getUUID),
                recipient.getUUID(), WitchRules.server().initiationTicks(), menus);
        session.setPayload(new Payload(offering, taken.get(), units));
        if (AltarSessionService.begin(session).isPresent()) {
            return "busy";
        }
        payer.ifPresent(p -> AltarSessionService.notice(p, "initiation.begun"));
        return "initiation.begun";
    }

    @Override
    public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> payer = session.payer().flatMap(InitiationEvents::player);
        if (session.payer().isPresent()) {
            if (payer.isEmpty() || !p.offering().intact(payer.get())) {
                return Optional.of("inputs_changed");
            }
            if (WitchStatusService.check(payer.get(), p.energyUnits()) != EnergyService.Result.OK) {
                return Optional.of("not_enough_energy");
            }
        }
        return Optional.empty();
    }

    @Override
    public void tick(MinecraftServer server, AltarSession session) {
    }

    @Override
    public void complete(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> payer = session.payer().flatMap(InitiationEvents::player);
        ServerPlayer recipient = InitiationEvents.player(session.subject()).orElse(null);
        if (recipient == null || session.payer().isPresent() && payer.isEmpty()) {
            AltarSessionService.cancel(session, "left");
            return;
        }
        ConversionCommit.Outcome outcome = ConversionCommit.commit(server, session, payer, recipient, p.offering(), p.energyUnits(),
                Optional.empty());
        if (session.running()) {
            AltarSessionService.cancel(session, outcome.suffix().substring("result.".length()));
            return;
        }
        String suffix = outcome == ConversionCommit.Outcome.CONVERTED ? "initiation.done" : outcome.suffix();
        AltarSessionService.notice(recipient, suffix);
        payer.ifPresent(i -> AltarSessionService.notice(i, suffix));
    }

    @Override
    public void cancelled(MinecraftServer server, AltarSession session, String reason) {
        // the invitation was taken at acceptance: an interrupted initiation needs a fresh one
    }
}
