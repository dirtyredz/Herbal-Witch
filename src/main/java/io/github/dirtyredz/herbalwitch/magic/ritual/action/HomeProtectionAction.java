package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;

import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessions;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

/**
 * Home protection, a circle ritual: a witch with a complete circle chooses an empty ward stone within
 * {@link #REACH} blocks of the altar that she may bind; after the ritual's time the ward receives the
 * same charge as a direct activation (the ritual's offering and energy pay for it, the stone charges
 * nothing more). Every ward rule is checked when it starts and again before it commits; a charged ward
 * is never topped up. Nothing is consumed by a ritual that does not complete.
 */
public final class HomeProtectionAction implements AltarSessionService.Handler {
    public static final double REACH = 8.0;

    /** What a protection session holds. */
    public record Payload(BlockPos stone, AltarOffering offering, int energyUnits) {
    }

    public static String begin(ServerPlayer witch, AltarMenu menu, Optional<BlockPos> stone) {
        if (!WitchStatusService.isWitch(witch)) {
            return "home_protection.not_witch";
        }
        ServerLevel level = witch.serverLevel();
        Optional<String> circle = CircleRituals.incomplete(level, menu.origin());
        if (circle.isPresent()) {
            return circle.get();
        }
        if (stone.isEmpty() || Vec3.atCenterOf(stone.get()).distanceTo(Vec3.atCenterOf(menu.origin())) > REACH) {
            return "home_protection.no_ward";
        }
        Optional<String> refusal = WardService.refusal(witch, level, stone.get());
        if (refusal.isPresent()) {
            return "home_protection." + refusal.get();
        }
        int units = EnergyService.units(WitchRules.server().workings().homeProtectionCost());
        if (WitchStatusService.check(witch, units) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        Optional<AltarOffering> offering = AltarOffering.capture(witch, AltarOffering.requirements(AltarActionSpec.HOME_PROTECTION));
        if (offering.isEmpty()) {
            return "missing_offering";
        }
        RitualSite site = AltarSessionService.site(level, menu.origin());
        if (AltarSessionService.at(site).isPresent() || AltarSessionService.of(witch.getUUID()).isPresent()) {
            return "busy";
        }
        AltarSessions sessions = AltarSessionService.sessions().orElseThrow();
        AltarSession session = new AltarSession(sessions.nextId(), AltarActionSpec.HOME_PROTECTION, site, Optional.of(witch.getUUID()),
                witch.getUUID(), WitchRules.server().workings().homeProtectionTicks(), Map.of(witch.getUUID(), menu.nonce()));
        session.setPayload(new Payload(stone.get().immutable(), offering.get(), units));
        if (AltarSessionService.begin(session).isPresent()) {
            return "busy";
        }
        CircleRituals.glow(session, true);
        return "home_protection.begun";
    }

    @Override
    public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> witch = OnlinePlayers.find(session.subject());
        if (witch.isEmpty() || !p.offering().intact(witch.get())) {
            return Optional.of("inputs_changed");
        }
        if (WitchStatusService.check(witch.get(), p.energyUnits()) != EnergyService.Result.OK) {
            return Optional.of("not_enough_energy");
        }
        return CircleRituals.broken(session);
    }

    @Override
    public boolean paused(MinecraftServer server, AltarSession session) {
        return CircleRituals.paused(session);
    }

    @Override
    public void tick(MinecraftServer server, AltarSession session) {
    }

    @Override
    public void complete(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        ServerPlayer witch = OnlinePlayers.find(session.subject()).orElse(null);
        ServerLevel level = AltarSessionService.level(session.site()).orElse(null);
        if (witch == null || level == null || !p.offering().intact(witch)) {
            AltarSessionService.cancel(session, "inputs_changed");
            return;
        }
        if (CircleRituals.incomplete(level, CircleRituals.altar(session)).isPresent()) {
            AltarSessionService.cancel(session, "circle_broken");
            return;
        }
        if (WardService.refusal(witch, level, p.stone()).isPresent()) {
            AltarSessionService.cancel(session, "ward_changed");
            return;
        }
        if (WitchStatusService.check(witch, p.energyUnits()) != EnergyService.Result.OK) {
            AltarSessionService.cancel(session, "not_enough_energy");
            return;
        }
        if (!AltarSessionService.completeOnce(session)) {
            return;
        }
        CircleRituals.glow(session, false);
        boolean done = CircleRituals.pay(witch, p.offering(), p.energyUnits(),
                () -> "activated".equals(WardService.activateByRitual(witch, level, p.stone())));
        if (!done) {
            AltarSessionService.notice(witch, "cancel.ward_changed");
            return;
        }
        DiscoveryService.grant(witch, AltarEntries.key(AltarActionSpec.HOME_PROTECTION));
        AltarSessionService.notice(witch, "home_protection.done");
    }

    @Override
    public void cancelled(MinecraftServer server, AltarSession session, String reason) {
        CircleRituals.glow(session, false);
    }
}
