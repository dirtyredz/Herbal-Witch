package io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching;

import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingRequest;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ReaderService;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Teaching on a running server: the one {@link TeachingService} per server (invitations are
 * transient and die with it), request handling from open readers, private notifications to the two
 * players involved, the nearby-player list for teachers at research blocks, and cancellation on
 * logout, death, dimension change, expiry, a closed teacher reader or a content reload.
 */
public final class TeachingEvents {
    private static final int SWEEP_INTERVAL = 20;
    private static TeachingService service;
    private static TeachingWorld world;
    private static final Map<UUID, List<ReaderUpdate.Nearby>> NEARBY_SENT = new HashMap<>();

    private TeachingEvents() {
    }

    public static Optional<TeachingService> service() {
        return Optional.ofNullable(service);
    }

    public static void serverStarted(ServerStartedEvent event) {
        start(event.getServer());
    }

    /** Starts a fresh service for the server (also used by GameTests that need a clean slate). */
    public static void start(MinecraftServer server) {
        world = new TeachingWorld(server);
        service = new TeachingService(server::getTickCount, world);
        NEARBY_SENT.clear();
    }

    public static void serverStopped(ServerStoppedEvent event) {
        service = null;
        world = null;
        NEARBY_SENT.clear();
    }

    /** A request from an open reader; anything stale or invalid is dropped before any change. */
    public static void handle(ServerPlayer sender, TeachingRequest request) {
        if (service == null) {
            return;
        }
        Optional<GrimoireMenu> menu = ReaderService.validMenu(sender, request.containerId(), request.session(), request.revision());
        if (menu.isEmpty()) {
            return;
        }
        UUID self = sender.getUUID();
        switch (request.op()) {
            case INVITE -> {
                if (request.recipe().isEmpty() || request.other().isEmpty()) {
                    return;
                }
                if (!menu.get().access().atResearchBlock()) {
                    tell(sender, Component.translatable(TeachingService.Result.NO_READER.messageKey()));
                    return;
                }
                TeachingService.Result result = service.invite(self, request.other().get(), request.recipe().get());
                if (result == TeachingService.Result.SENT) {
                    service.outgoing(self).ifPresent(TeachingEvents::announce);
                } else {
                    tell(sender, Component.translatable(result.messageKey(), title(sender, request.recipe().get())));
                }
            }
            case ACCEPT -> {
                if (request.recipe().isEmpty() || request.other().isEmpty()) {
                    return;
                }
                Optional<TeachingInvitation> pending = service.incoming(self);
                TeachingService.Result result = service.accept(self, request.other().get(), request.recipe().get());
                Component recipe = title(sender, request.recipe().get());
                tell(sender, Component.translatable(result.messageKey(), recipe));
                if (result != TeachingService.Result.NO_INVITATION && service.incoming(self).isEmpty()) {
                    pending.ifPresent(i -> {
                        cleared(i);
                        world.player(i.teacher()).ifPresent(t -> tell(t, Component.translatable(result.messageKey(), recipe)));
                    });
                }
            }
            case DECLINE -> request.other().ifPresent(teacher -> {
                Optional<TeachingInvitation> pending = service.incoming(self);
                if (service.decline(self, teacher) == TeachingService.Result.DECLINED) {
                    pending.ifPresent(i -> {
                        cleared(i);
                        notifyBoth(i, TeachingService.Result.DECLINED);
                    });
                }
            });
            case CANCEL -> {
                Optional<TeachingInvitation> pending = service.outgoing(self);
                if (service.cancel(self) == TeachingService.Result.CANCELLED) {
                    pending.ifPresent(i -> {
                        cleared(i);
                        notifyBoth(i, TeachingService.Result.CANCELLED);
                    });
                }
            }
        }
    }

    /** A reader opened: send this player's invitation state and, at a research block, who is nearby. */
    public static void readerOpened(ServerPlayer player, GrimoireMenu menu) {
        if (service == null) {
            return;
        }
        long now = player.getServer().getTickCount();
        service.incoming(player.getUUID()).ifPresentOrElse(i -> GrimoirePackets.send(player, update(TeachingUpdate.Kind.INCOMING,
                i, i.teacher(), now)), () -> GrimoirePackets.send(player, TeachingUpdate.cleared(TeachingUpdate.Kind.INCOMING_CLEARED)));
        service.outgoing(player.getUUID()).ifPresentOrElse(i -> GrimoirePackets.send(player, update(TeachingUpdate.Kind.OUTGOING,
                i, i.recipient(), now)), () -> GrimoirePackets.send(player, TeachingUpdate.cleared(TeachingUpdate.Kind.OUTGOING_CLEARED)));
        NEARBY_SENT.remove(player.getUUID());
        sendNearby(player, menu);
    }

    private static void sendNearby(ServerPlayer player, GrimoireMenu menu) {
        if (!menu.access().atResearchBlock() || world == null) {
            return;
        }
        List<ReaderUpdate.Nearby> nearby = world.nearby(player).stream().limit(ReaderUpdate.MAX_NEARBY)
                .map(p -> new ReaderUpdate.Nearby(p.getUUID(), p.getGameProfile().getName())).toList();
        if (!nearby.equals(NEARBY_SENT.get(player.getUUID()))) {
            NEARBY_SENT.put(player.getUUID(), nearby);
            GrimoirePackets.send(player, ReaderUpdate.nearby(menu.containerId, menu.nonce(), nearby));
        }
    }

    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || service == null || event.getServer().getTickCount() % SWEEP_INTERVAL != 0) {
            return;
        }
        sweep();
    }

    /** Ends invitations that expired or whose teacher closed the reader; refreshes teachers' nearby lists. */
    public static void sweep() {
        if (service == null) {
            return;
        }
        for (TeachingInvitation i : service.sweep()) {
            cleared(i);
            notifyBoth(i, TeachingService.Result.EXPIRED);
        }
        for (ServerPlayer player : List.copyOf(worldPlayers())) {
            if (player.containerMenu instanceof GrimoireMenu menu && menu.session() != null) {
                sendNearby(player, menu);
            } else {
                NEARBY_SENT.remove(player.getUUID());
            }
        }
    }

    private static List<ServerPlayer> worldPlayers() {
        return world == null ? List.of() : NEARBY_SENT.keySet().stream().map(world::player).flatMap(Optional::stream).toList();
    }

    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancelInvolving(event.getEntity().getUUID());
        NEARBY_SENT.remove(event.getEntity().getUUID());
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
        if (service == null) {
            return;
        }
        for (TeachingInvitation i : service.cancelInvolving(player)) {
            cleared(i);
            notifyBoth(i, TeachingService.Result.CANCELLED);
        }
    }

    /** Content listener: a reload voids offers whose recipe vanished or changed. */
    public static void contentChanged(GrimoireContent content) {
        if (service == null) {
            return;
        }
        for (TeachingInvitation i : service.invalidate(k -> content.recipe(k).map(RecipeView::fingerprint))) {
            cleared(i);
            notifyBoth(i, TeachingService.Result.RECIPE_CHANGED);
        }
    }

    private static void announce(TeachingInvitation i) {
        long now = world.player(i.teacher()).map(p -> (long) p.getServer().getTickCount()).orElse(i.createdTick());
        world.player(i.recipient()).ifPresent(r -> {
            Component teacher = world.player(i.teacher()).map(ServerPlayer::getDisplayName).orElse(Component.literal("?"));
            r.sendSystemMessage(Component.translatable("grimoire.herbalwitch.teaching.invited", teacher, title(r, i.recipe())));
            GrimoirePackets.send(r, update(TeachingUpdate.Kind.INCOMING, i, i.teacher(), now));
        });
        world.player(i.teacher()).ifPresent(t -> {
            tell(t, Component.translatable(TeachingService.Result.SENT.messageKey(), title(t, i.recipe())));
            GrimoirePackets.send(t, update(TeachingUpdate.Kind.OUTGOING, i, i.recipient(), now));
        });
    }

    private static TeachingUpdate update(TeachingUpdate.Kind kind, TeachingInvitation i, UUID other, long now) {
        String name = world.player(other).map(p -> p.getGameProfile().getName()).orElse("?");
        return new TeachingUpdate(kind, Optional.of(other), name, Optional.of(i.recipe()), i.ticksLeft(now));
    }

    private static void cleared(TeachingInvitation i) {
        world.player(i.recipient()).ifPresent(p -> GrimoirePackets.send(p, TeachingUpdate.cleared(TeachingUpdate.Kind.INCOMING_CLEARED)));
        world.player(i.teacher()).ifPresent(p -> GrimoirePackets.send(p, TeachingUpdate.cleared(TeachingUpdate.Kind.OUTGOING_CLEARED)));
    }

    private static void notifyBoth(TeachingInvitation i, TeachingService.Result result) {
        for (UUID id : List.of(i.teacher(), i.recipient())) {
            world.player(id).ifPresent(p -> tell(p, Component.translatable(result.messageKey(), title(p, i.recipe()))));
        }
    }

    private static void tell(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }

    private static Component title(ServerPlayer player, EntryKey recipe) {
        return player.getServer() == null ? Component.literal(recipe.id())
                : ServerGrimoireContent.get(player.getServer()).get(recipe).map(e -> e.title()).orElse(Component.literal(recipe.id()));
    }
}
