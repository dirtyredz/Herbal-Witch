package io.github.dirtyredz.herbalwitch.companions.familiars.ritual;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.FamiliarUpgrades;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.*;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public final class FamiliarUpgradeAction implements AltarSessionService.Handler {
    private record Payload(UUID bond, AltarOffering offering) {}
    private final AltarActionSpec action;
    private FamiliarUpgradeAction(AltarActionSpec action) { this.action = action; }
    public static void register() {
        for (var action : List.of(FamiliarActionCatalog.HEARTWOOD, FamiliarActionCatalog.SPIRIT_WELL, FamiliarActionCatalog.HARMONY)) {
            var handler = new FamiliarUpgradeAction(action); AltarActions.register(action, handler::begin); AltarSessionService.handle(action, handler);
        }
    }
    private String begin(ServerPlayer owner, AltarMenu menu, Optional<UUID> target) {
        if (target.isEmpty() || !FamiliarUpgrades.eligible(owner, target.get(), menu.origin(), action)) return "familiar.no_target";
        var circle = CircleRituals.incomplete(owner.serverLevel(), menu.origin()); if (circle.isPresent()) return circle.get();
        if (WitchStatusService.check(owner, action.energy() * 100) != EnergyService.Result.OK) return "not_enough_energy";
        var requirements = new ArrayList<>(AltarOffering.requirements(action));
        if (action == FamiliarActionCatalog.HARMONY) {
            var species = FamiliarStore.get(owner.server).record(target.get()).orElseThrow().species();
            var bait = FamiliarCatalog.find(species).orElseThrow().bait();
            requirements.add(new AltarOffering.Requirement(AnimalItems.bait(Bait.valueOf(bait.toUpperCase(Locale.ROOT))), 1));
        }
        var offering = AltarOffering.capture(owner, requirements); if (offering.isEmpty()) return "missing_offering";
        var sessions = AltarSessionService.sessions().orElse(null); if (sessions == null) return "busy";
        var session = new AltarSession(sessions.nextId(), action, AltarSessionService.site(owner.level(), menu.origin()), Optional.of(owner.getUUID()),
                owner.getUUID(), action.ticks(), Map.of(owner.getUUID(), menu.nonce()));
        session.setPayload(new Payload(target.get(), offering.get())); if (AltarSessionService.begin(session).isPresent()) return "busy";
        CircleRituals.glow(session, true); return "familiar.begun";
    }
    @Override public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        var owner = OnlinePlayers.find(session.subject()).orElse(null); Payload payload = session.payload();
        if (owner == null || !payload.offering().intact(owner) || !FamiliarUpgrades.eligible(owner, payload.bond(), CircleRituals.altar(session), action)) return Optional.of("inputs_changed");
        if (WitchStatusService.check(owner, action.energy() * 100) != EnergyService.Result.OK) return Optional.of("not_enough_energy");
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
        boolean success = FamiliarUpgrades.apply(owner, payload.bond(), CircleRituals.altar(session), action, payload.offering());
        AltarSessionService.notice(owner, success ? "familiar.upgraded" : "familiar.interrupted");
    }
    @Override public void cancelled(MinecraftServer server, AltarSession session, String reason) { CircleRituals.glow(session, false); }
}
