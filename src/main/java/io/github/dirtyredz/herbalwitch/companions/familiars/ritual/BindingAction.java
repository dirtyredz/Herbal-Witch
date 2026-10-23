package io.github.dirtyredz.herbalwitch.companions.familiars.ritual;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.CircleRituals;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import java.util.*;

public final class BindingAction implements AltarSessionService.Handler {
    private record Payload(Mob source, AltarOffering offering) {}
    public static String begin(ServerPlayer player, AltarMenu menu, Optional<UUID> target) {
        if (!WitchStatusService.isWitch(player)) return "familiar.not_witch";
        var circle = CircleRituals.incomplete(player.serverLevel(), menu.origin()); if (circle.isPresent()) return circle.get();
        if (target.isEmpty() || !(player.serverLevel().getEntity(target.get()) instanceof Mob source)) return "familiar.no_target";
        String refusal = FamiliarEligibility.refusal(player, source, menu.origin()); if (refusal != null) return "familiar." + refusal;
        if (WitchStatusService.check(player, 2500) != EnergyService.Result.OK) return "not_enough_energy";
        var requirements = new ArrayList<>(AltarOffering.requirements(FamiliarActionCatalog.BINDING));
        String bait = FamiliarCatalog.type(EntityType.getKey(source.getType()).toString()).orElseThrow().bait();
        requirements.add(new AltarOffering.Requirement(AnimalItems.bait(Bait.valueOf(bait.toUpperCase(Locale.ROOT))), 1));
        var offering = AltarOffering.capture(player, requirements); if (offering.isEmpty()) return "missing_offering";
        var sessions = AltarSessionService.sessions().orElse(null); if (sessions == null) return "busy";
        var session = new AltarSession(sessions.nextId(), FamiliarActionCatalog.BINDING, AltarSessionService.site(player.level(), menu.origin()),
                Optional.of(player.getUUID()), player.getUUID(), 200, Map.of(player.getUUID(), menu.nonce()));
        session.setPayload(new Payload(source, offering.get()));
        if (AltarSessionService.begin(session).isPresent()) return "busy";
        CircleRituals.glow(session, true); return "familiar.begun";
    }
    @Override public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        var player = OnlinePlayers.find(session.subject()).orElse(null); Payload payload = session.payload();
        if (player == null || !payload.offering().intact(player)) return Optional.of("inputs_changed");
        var problem = FamiliarEligibility.refusal(player, payload.source(), CircleRituals.altar(session));
        if (problem != null) return Optional.of("familiar_" + problem);
        if (WitchStatusService.check(player, 2500) != EnergyService.Result.OK) return Optional.of("not_enough_energy");
        return CircleRituals.broken(session);
    }
    @Override public boolean paused(MinecraftServer server, AltarSession session) { return CircleRituals.paused(session); }
    @Override public void tick(MinecraftServer server, AltarSession session) {}
    @Override public void complete(MinecraftServer server, AltarSession session) {
        var player = OnlinePlayers.find(session.subject()).orElse(null); Payload payload = session.payload();
        if (player == null || invalid(server, session).isPresent() || CircleRituals.incomplete(player.serverLevel(), CircleRituals.altar(session)).isPresent()) {
            AltarSessionService.cancel(session, "inputs_changed"); return;
        }
        if (!AltarSessionService.completeOnce(session)) return;
        CircleRituals.glow(session, false);
        boolean done = FamiliarBindingService.bind(player, payload.source(), CircleRituals.altar(session), payload.offering());
        AltarSessionService.notice(player, done ? "familiar.bound" : "familiar.interrupted");
    }
    @Override public void cancelled(MinecraftServer server, AltarSession session, String reason) { CircleRituals.glow(session, false); }
}
