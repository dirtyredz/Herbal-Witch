package io.github.dirtyredz.herbalwitch.magic.ritual.menu;

import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleValidation;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarPackets;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationConsent;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationOffer;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Keeps each open altar screen informed, privately: every {@link #INTERVAL} ticks the server builds
 * what that player's screen shows ({@link AltarUpdate}) and sends it only when it differs from the
 * last one sent to that menu. Read-only: building it changes nothing.
 */
public final class AltarMenuSync {
    public static final int INTERVAL = 5;
    private static final Map<ServerPlayer, AltarUpdate> SENT = new WeakHashMap<>();

    private AltarMenuSync() {
    }

    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.containerMenu instanceof AltarMenu menu)) {
            SENT.remove(player);
            return;
        }
        if ((player.tickCount + player.getId()) % INTERVAL == 0) {
            AltarUpdate update = build(player, menu);
            if (!update.equals(SENT.get(player))) {
                SENT.put(player, update);
                AltarPackets.send(player, update);
            }
        }
    }

    /** What this player's altar screen shows now. */
    public static AltarUpdate build(ServerPlayer player, AltarMenu menu) {
        RitualSite site = AltarSessionService.site(player.level(), menu.origin());
        Optional<AltarUpdate.Activity> activity = AltarSessionService.at(site).map(s -> activity(player, s));
        Optional<InitiationConsent> consent = InitiationEvents.consent();
        long now = player.getServer() == null ? 0 : player.getServer().getTickCount();
        Optional<AltarUpdate.Invitation> incoming = consent.flatMap(c -> c.incoming(player.getUUID()))
                .map(o -> invitation(o, o.initiator(), now, site));
        Optional<AltarUpdate.Invitation> outgoing = consent.flatMap(c -> c.outgoing(player.getUUID()))
                .map(o -> invitation(o, Optional.of(o.recipient()), now, site));
        List<AltarUpdate.Nearby> nearby = WitchStatusService.isWitch(player) ? nearby(player, menu) : List.of();
        Map<AltarActionSpec, List<AltarUpdate.Count>> counts = new LinkedHashMap<>();
        for (AltarActionSpec action : List.of(AltarActionSpec.FOUNDING, AltarActionSpec.INITIATION, AltarActionSpec.ATTUNEMENT,
                AltarActionSpec.GARDEN_BLESSING, AltarActionSpec.HOME_PROTECTION)) {
            counts.put(action, AltarOffering.counts(player, AltarOffering.requirements(action)).stream()
                    .map(c -> new AltarUpdate.Count(c.have(), c.need())).toList());
        }
        boolean foundingOpen = player.getServer() != null && FoundingService.open(player.getServer());
        return new AltarUpdate(menu.containerId, menu.nonce(), foundingOpen, activity, incoming, outgoing, nearby, counts,
                AltarSessionService.lastNotice(player.getUUID()), circle(player, menu));
    }

    /** The circle around this altar, as its screen lists it. */
    private static AltarUpdate.Circle circle(ServerPlayer player, AltarMenu menu) {
        CircleValidation.Report report = CircleValidation.around(player.level(), menu.origin());
        return new AltarUpdate.Circle(report.missing(), report.unloaded());
    }

    private static AltarUpdate.Activity activity(ServerPlayer player, AltarSession s) {
        boolean mine = s.involves(player.getUUID());
        String with = s.participants().stream().filter(id -> !id.equals(player.getUUID())).findFirst()
                .flatMap(OnlinePlayers::find).map(p -> p.getGameProfile().getName()).orElse("");
        return new AltarUpdate.Activity(s.action(), s.elapsed(), s.duration(), mine, with);
    }

    private static AltarUpdate.Invitation invitation(InitiationOffer o, Optional<UUID> other, long now, RitualSite here) {
        String name = other.flatMap(OnlinePlayers::find).map(p -> p.getGameProfile().getName()).orElse("");
        return new AltarUpdate.Invitation(o.id(), name, o.administrative(), o.ticksLeft(now), o.site().map(here::equals).orElse(false));
    }

    /** Non-witches who could use this altar too, by name, at most {@link AltarUpdate#MAX_NEARBY}. */
    private static List<AltarUpdate.Nearby> nearby(ServerPlayer witch, AltarMenu menu) {
        return candidates(witch).stream()
                .filter(p -> p != witch && !WitchStatusService.isWitch(p) && AltarAccess.mayUse(p, menu.origin()))
                .sorted(Comparator.comparing(p -> p.getGameProfile().getName()))
                .limit(AltarUpdate.MAX_NEARBY)
                .map(p -> new AltarUpdate.Nearby(p.getUUID(), p.getGameProfile().getName())).toList();
    }

    private static List<ServerPlayer> candidates(ServerPlayer witch) {
        return witch.serverLevel().getEntitiesOfClass(ServerPlayer.class, witch.getBoundingBox().inflate(12), Player::isAlive);
    }
}
