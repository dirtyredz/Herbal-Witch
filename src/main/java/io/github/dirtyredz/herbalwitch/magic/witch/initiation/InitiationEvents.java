package io.github.dirtyredz.herbalwitch.magic.witch.initiation;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Initiation consent on a running server: one transient {@link InitiationConsent} per server
 * (invitations die with it), passive notifications (a chat line, never a forced screen), and
 * cancellation on expiry, logout, death, dimension change and every accepted rules change. Players
 * are looked up among the online players, plus fake players GameTests register.
 */
public final class InitiationEvents {
    private static final int SWEEP_INTERVAL = 20;
    private static volatile Collection<ServerPlayer> testPlayers = List.of();
    private static MinecraftServer server;
    private static InitiationConsent consent;

    private InitiationEvents() {
    }

    public static Optional<InitiationConsent> consent() {
        return Optional.ofNullable(consent);
    }

    public static void serverStarted(ServerStartedEvent event) {
        start(event.getServer());
    }

    /** A fresh consent service (also for GameTests that need a clean slate). */
    public static void start(MinecraftServer s) {
        server = s;
        consent = new InitiationConsent(s::getTickCount, id -> player(id).map(WitchStatusService::isWitch).orElse(false),
                () -> WitchRules.server().inviteExpiry(), () -> WitchRules.server().inviteCooldown());
    }

    public static void serverStopped(ServerStoppedEvent event) {
        consent = null;
        server = null;
    }

    /** GameTest seam: fake players to treat as online. */
    public static void setTestPlayers(Collection<ServerPlayer> players) {
        testPlayers = List.copyOf(players);
    }

    public static Optional<ServerPlayer> player(UUID id) {
        if (server == null) {
            return Optional.empty();
        }
        ServerPlayer online = server.getPlayerList().getPlayer(id);
        if (online != null) {
            return Optional.of(online);
        }
        return testPlayers.stream().filter(p -> p.getUUID().equals(id) && !p.isRemoved()).findFirst();
    }

    /** A witch invites {@code recipient} at {@code site}; both are told. */
    public static InitiationConsent.Result invite(ServerPlayer initiator, ServerPlayer recipient, RitualSite site) {
        if (consent == null) {
            return InitiationConsent.Result.NO_INVITATION;
        }
        if (!WitchRules.server().initiationEnabled()) {
            return InitiationConsent.Result.DISABLED;
        }
        InitiationConsent.Result result = consent.invite(initiator.getUUID(), recipient.getUUID(), site);
        if (result == InitiationConsent.Result.SENT) {
            recipient.sendSystemMessage(Component.translatable("witch.herbalwitch.initiation.invited", initiator.getDisplayName()));
        }
        return result;
    }

    /** An operator designates {@code recipient}; the recipient is told to accept at an altar. */
    public static InitiationConsent.Result designate(ServerPlayer recipient) {
        if (consent == null) {
            return InitiationConsent.Result.NO_INVITATION;
        }
        InitiationConsent.Result result = consent.designate(recipient.getUUID());
        if (result == InitiationConsent.Result.DESIGNATED) {
            recipient.sendSystemMessage(Component.translatable("witch.herbalwitch.initiation.designated_notice"));
        }
        return result;
    }

    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && consent != null && event.getServer().getTickCount() % SWEEP_INTERVAL == 0) {
            ended(consent.sweep(), InitiationConsent.Result.EXPIRED);
        }
    }

    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancelInvolving(event.getEntity().getUUID());
    }

    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelInvolving(player.getUUID());
        }
    }

    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        cancelInvolving(event.getEntity().getUUID());
    }

    public static void cancelInvolving(UUID player) {
        if (consent != null) {
            ended(consent.cancelInvolving(player), InitiationConsent.Result.WITHDRAWN);
        }
    }

    /** Rules listener: a valid configuration change ends every pending invitation. */
    public static void rulesChanged(WitchRules rules) {
        if (consent != null) {
            ended(consent.cancelAll(), InitiationConsent.Result.WITHDRAWN);
        }
    }

    /** Tells both players an invitation ended and why. */
    public static void ended(List<InitiationOffer> offers, InitiationConsent.Result why) {
        for (InitiationOffer offer : offers) {
            player(offer.recipient()).ifPresent(p -> p.displayClientMessage(Component.translatable(why.messageKey()), true));
            offer.initiator().flatMap(InitiationEvents::player)
                    .ifPresent(p -> p.displayClientMessage(Component.translatable(why.messageKey()), true));
        }
    }
}
