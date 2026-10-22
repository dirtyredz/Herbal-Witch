package io.github.dirtyredz.herbalwitch.magic.ritual.session;

import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Altar operations on a running server: one {@link AltarSessions} per server (transient: nothing
 * resumes after a restart), each running session revalidated every tick before it progresses (every
 * participant online, alive, in the altar's dimension, with the altar menu they started from still
 * open and valid, plus the action's own checks such as untouched offering stacks), and cancelled on
 * damage, death, logout, dimension travel, the altar breaking or unloading, and a rules or tag
 * reload. Cancellation consumes nothing. The altar glows while a session runs.
 */
public final class AltarSessionService {
    /** What an action adds to the shared session lifecycle (registered by the ritual registration). */
    public interface Handler {
        /** An extra reason the session can no longer continue, or empty. */
        Optional<String> invalid(MinecraftServer server, AltarSession session);

        /** One loaded tick of progress (after validation). */
        void tick(MinecraftServer server, AltarSession session);

        /** A timed session reached its duration. */
        void complete(MinecraftServer server, AltarSession session);

        /** The session was cancelled (release what it holds; nothing was consumed). */
        void cancelled(MinecraftServer server, AltarSession session, String reason);
    }

    public static final String KEY = "ritual.herbalwitch.";
    private static final Map<AltarActionSpec, Handler> HANDLERS = new EnumMap<>(AltarActionSpec.class);
    private static final Map<UUID, String> NOTICES = new HashMap<>();
    private static AltarSessions sessions;
    private static MinecraftServer server;

    private AltarSessionService() {
    }

    public static void handle(AltarActionSpec action, Handler handler) {
        HANDLERS.put(action, handler);
    }

    public static void serverStarted(ServerStartedEvent event) {
        start(event.getServer());
    }

    /** A fresh registry (also for GameTests). */
    public static void start(MinecraftServer s) {
        server = s;
        sessions = new AltarSessions();
        NOTICES.clear();
    }

    public static void serverStopped(ServerStoppedEvent event) {
        sessions = null;
        server = null;
        NOTICES.clear();
    }

    public static Optional<AltarSessions> sessions() {
        return Optional.ofNullable(sessions);
    }

    public static RitualSite site(Level level, BlockPos origin) {
        return new RitualSite(level.dimension().location().toString(), origin.asLong());
    }

    public static Optional<AltarSession> at(RitualSite site) {
        return sessions == null ? Optional.empty() : sessions.at(site);
    }

    public static Optional<AltarSession> of(UUID player) {
        return sessions == null ? Optional.empty() : sessions.of(player);
    }

    public static boolean busy(ServerLevel level, BlockPos origin) {
        return at(site(level, origin)).isPresent();
    }

    /** The altar level and origin of a site, if that dimension exists. */
    public static Optional<ServerLevel> level(RitualSite site) {
        if (server == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(site.dimension()))));
    }

    /** Registers a new running session and lights the altar, or says why it cannot run. */
    public static Optional<AltarSessions.Refusal> begin(AltarSession session) {
        if (sessions == null) {
            return Optional.of(AltarSessions.Refusal.ALTAR_BUSY);
        }
        Optional<AltarSessions.Refusal> refusal = sessions.start(session);
        if (refusal.isEmpty()) {
            level(session.site()).ifPresent(l -> AltarBlock.setActive(l, BlockPos.of(session.site().origin()), true));
            session.participants().forEach(NOTICES::remove);
        }
        return refusal;
    }

    /** Ends a session as completed, once (before its result is published); false for a duplicate. */
    public static boolean completeOnce(AltarSession session) {
        if (sessions == null || !sessions.complete(session)) {
            return false;
        }
        level(session.site()).ifPresent(l -> AltarBlock.setActive(l, BlockPos.of(session.site().origin()), false));
        return true;
    }

    /** Cancels a running session with a reason ({@code ritual.herbalwitch.cancel.<reason>}); nothing is consumed. */
    public static void cancel(AltarSession session, String reason) {
        if (sessions == null || !sessions.cancel(session)) {
            return;
        }
        level(session.site()).ifPresent(l -> AltarBlock.setActive(l, BlockPos.of(session.site().origin()), false));
        Handler handler = HANDLERS.get(session.action());
        if (handler != null) {
            handler.cancelled(server, session, reason);
        }
        notifyAll(session, "cancel." + reason);
    }

    /** Tells every participant (action bar) and remembers it for their altar screen. */
    public static void notifyAll(AltarSession session, String suffix) {
        for (UUID id : session.participants()) {
            InitiationEvents.player(id).ifPresent(p -> notice(p, suffix));
        }
    }

    public static void notice(ServerPlayer player, String suffix) {
        NOTICES.put(player.getUUID(), KEY + suffix);
        player.displayClientMessage(Component.translatable(KEY + suffix), true);
    }

    /** The last notice for a player's altar screen. */
    public static Optional<String> lastNotice(UUID player) {
        return Optional.ofNullable(NOTICES.get(player));
    }

    // ---------------------------------------------------------------- ticking

    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && sessions != null) {
            tickAll();
        }
    }

    /** One tick of every running session (GameTests may drive it directly). */
    public static void tickAll() {
        for (AltarSession session : sessions.running()) {
            tick(session);
        }
    }

    /** One tick of one session: revalidate, progress, complete when due. */
    public static void tick(AltarSession session) {
        if (!session.running()) {
            return;
        }
        Handler handler = HANDLERS.get(session.action());
        Optional<String> reason = participantProblem(session);
        if (reason.isEmpty() && handler != null) {
            reason = handler.invalid(server, session);
        }
        if (reason.isPresent()) {
            cancel(session, reason.get());
            return;
        }
        session.advance();
        if (handler != null) {
            handler.tick(server, session);
            if (session.running() && session.due()) {
                handler.complete(server, session);
            }
        }
    }

    private static Optional<String> participantProblem(AltarSession session) {
        Optional<ServerLevel> level = level(session.site());
        BlockPos origin = BlockPos.of(session.site().origin());
        if (level.isEmpty() || !level.get().isLoaded(origin) || !(level.get().getBlockState(origin).getBlock() instanceof AltarBlock)) {
            return Optional.of("altar_gone");
        }
        for (Map.Entry<UUID, Long> e : session.menus().entrySet()) {
            Optional<ServerPlayer> p = InitiationEvents.player(e.getKey());
            if (p.isEmpty() || !p.get().isAlive()) {
                return Optional.of("left");
            }
            ServerPlayer player = p.get();
            if (player.level() != level.get()) {
                return Optional.of("left");
            }
            if (!(player.containerMenu instanceof AltarMenu menu) || menu.nonce() != e.getValue() || !menu.origin().equals(origin)) {
                return Optional.of("closed");
            }
            if (!menu.stillValid(player)) {
                return Optional.of("access");
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- interruptions

    public static void cancelInvolving(UUID player, String reason) {
        of(player).ifPresent(s -> cancel(s, reason));
    }

    public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0) {
            cancelInvolving(player.getUUID(), "hurt");
        }
    }

    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelInvolving(player.getUUID(), "left");
        }
    }

    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancelInvolving(event.getEntity().getUUID(), "left");
        NOTICES.remove(event.getEntity().getUUID());
    }

    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        cancelInvolving(event.getEntity().getUUID(), "left");
    }

    /** The altar at {@code origin} broke, lost a cell or unloaded. */
    public static void altarGone(ServerLevel level, BlockPos origin) {
        at(site(level, origin)).ifPresent(s -> cancel(s, "altar_gone"));
    }

    /** A rules change or a data reload: running rituals end (their requirements may have changed). */
    public static void cancelAll(String reason) {
        if (sessions != null) {
            List<AltarSession> running = sessions.running();
            running.forEach(s -> cancel(s, reason));
        }
    }

    public static void tagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            cancelAll("reloaded");
        }
    }
}
