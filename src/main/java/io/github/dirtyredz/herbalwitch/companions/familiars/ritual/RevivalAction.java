package io.github.dirtyredz.herbalwitch.companions.familiars.ritual;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarActionCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.recovery.FamiliarRevival;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.CircleRituals;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public final class RevivalAction implements AltarSessionService.Handler {
    private record Payload(UUID bond, AltarOffering offering) {}
    public static String begin(ServerPlayer owner, AltarMenu menu, Optional<UUID> target) {
        if (target.isEmpty() || !FamiliarRevival.eligible(owner, target.get(), menu.origin())) return "familiar.no_target";
        var circle = CircleRituals.incomplete(owner.serverLevel(), menu.origin()); if (circle.isPresent()) return circle.get();
        if (WitchStatusService.check(owner, 5000) != EnergyService.Result.OK) return "not_enough_energy";
        var offering = AltarOffering.capture(owner, AltarOffering.requirements(FamiliarActionCatalog.REVIVAL)); if (offering.isEmpty()) return "missing_offering";
        var sessions = AltarSessionService.sessions().orElse(null); if (sessions == null) return "busy";
        var session = new AltarSession(sessions.nextId(), FamiliarActionCatalog.REVIVAL, AltarSessionService.site(owner.level(), menu.origin()),
                Optional.of(owner.getUUID()), owner.getUUID(), 400, Map.of(owner.getUUID(), menu.nonce()));
        session.setPayload(new Payload(target.get(), offering.get()));
        if (AltarSessionService.begin(session).isPresent()) return "busy";
        CircleRituals.glow(session, true); return "familiar.begun";
    }
    @Override public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        var owner = OnlinePlayers.find(session.subject()).orElse(null); Payload payload = session.payload();
        if (owner == null || !payload.offering().intact(owner) || !FamiliarRevival.eligible(owner, payload.bond(), CircleRituals.altar(session))) return Optional.of("inputs_changed");
        if (WitchStatusService.check(owner, 5000) != EnergyService.Result.OK) return Optional.of("not_enough_energy");
        return CircleRituals.broken(session);
    }
    @Override public boolean paused(MinecraftServer server, AltarSession session) { return CircleRituals.paused(session); }
    @Override public void tick(MinecraftServer server, AltarSession session) {}
    @Override public void complete(MinecraftServer server, AltarSession session) {
        var owner = OnlinePlayers.find(session.subject()).orElse(null); Payload payload = session.payload();
        if (owner == null || invalid(server, session).isPresent() || CircleRituals.incomplete(owner.serverLevel(), CircleRituals.altar(session)).isPresent()) {
            AltarSessionService.cancel(session, "inputs_changed"); return;
        }
        if (!AltarSessionService.completeOnce(session)) return;
        CircleRituals.glow(session, false);
        boolean success = FamiliarRevival.revive(owner, payload.bond(), CircleRituals.altar(session), payload.offering());
        AltarSessionService.notice(owner, success ? "familiar.revived" : "familiar.interrupted");
    }
    @Override public void cancelled(MinecraftServer server, AltarSession session, String reason) { CircleRituals.glow(session, false); }
}
