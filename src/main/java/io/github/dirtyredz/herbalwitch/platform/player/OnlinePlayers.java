package io.github.dirtyredz.herbalwitch.platform.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Server-thread roster shared by features. Eligibility and proximity remain with each consumer. */
public final class OnlinePlayers {
    private static final Roster ROSTER = new Roster();

    private OnlinePlayers() { }

    public static void serverStarted(ServerStartedEvent event) {
        ROSTER.start(() -> event.getServer().getPlayerList().getPlayers());
    }

    public static void serverStopped(ServerStoppedEvent event) { ROSTER.stop(); }
    public static Optional<ServerPlayer> find(UUID id) { return ROSTER.find(id); }
    public static List<ServerPlayer> all() { return ROSTER.all(); }

    /** Adds fake players without replacing another fixture's registrations. Close when the scope ends. */
    public static Registration register(Collection<ServerPlayer> players) { return ROSTER.register(players); }

    public static final class Registration implements AutoCloseable {
        private final Roster roster;
        private final List<ServerPlayer> players;
        private boolean closed;

        private Registration(Roster roster, Collection<ServerPlayer> players) {
            this.roster = roster;
            this.players = new ArrayList<>(players);
        }

        public void add(ServerPlayer player) {
            if (closed) { throw new IllegalStateException("Player registration is closed"); }
            players.add(player);
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                players.clear();
                roster.registrations.remove(this);
            }
        }
    }

    /** Separate instances let lifecycle contracts be tested without resetting a running world's roster. */
    static final class Roster {
        private Supplier<List<ServerPlayer>> online;
        private final List<Registration> registrations = new ArrayList<>();

        void start(Supplier<List<ServerPlayer>> online) {
            stop();
            this.online = online;
        }

        void stop() {
            online = null;
            List.copyOf(registrations).forEach(Registration::close);
        }

        Registration register(Collection<ServerPlayer> players) {
            Registration registration = new Registration(this, players);
            registrations.add(registration);
            return registration;
        }

        Optional<ServerPlayer> find(UUID id) {
            if (online == null) return Optional.empty();
            for (ServerPlayer player : online.get()) if (player.getUUID().equals(id)) return Optional.of(player);
            for (Registration registration : registrations) for (ServerPlayer player : registration.players)
                if (!player.isRemoved() && player.getUUID().equals(id)) return Optional.of(player);
            return Optional.empty();
        }

        List<ServerPlayer> all() {
            if (online == null) { return List.of(); }
            var players = new LinkedHashMap<UUID, ServerPlayer>();
            online.get().forEach(p -> players.putIfAbsent(p.getUUID(), p));
            for (Registration registration : registrations) {
                registration.players.stream().filter(p -> !p.isRemoved())
                        .forEach(p -> players.putIfAbsent(p.getUUID(), p));
            }
            return List.copyOf(players.values());
        }
    }
}
