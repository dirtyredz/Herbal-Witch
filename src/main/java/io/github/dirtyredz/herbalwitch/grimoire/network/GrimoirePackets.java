package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

/**
 * Registers the grimoire's six messages on the mod channel ({@link Messages}: one fixed direction
 * each) and routes them to handlers wired by the feature's registration: server handlers for
 * requests, a client handler installed by client-only code. This class holds no policy and imports no
 * gameplay service, so transport and rules stay apart.
 */
public final class GrimoirePackets {
    /** What the server does with a request; the player is always the sender. */
    public interface ServerHandlers {
        void reader(ServerPlayer sender, ReaderRequest request);

        void teaching(ServerPlayer sender, TeachingRequest request);
    }

    /** What the client does with an update (installed by client wiring; never on a dedicated server). */
    public interface ClientHandlers {
        void reader(ReaderUpdate update);

        void discovery(DiscoveryUpdate update);

        void content(ContentUpdate update);

        void teaching(TeachingUpdate update);
    }

    private static volatile ServerHandlers server;
    private static volatile ClientHandlers client;
    private static volatile BiConsumer<ServerPlayer, Object> observer;

    private GrimoirePackets() {
    }

    /** Called once at bootstrap. */
    public static void register(ServerHandlers handlers) {
        server = handlers;
        Messages.toServer(ReaderRequest.class, ReaderRequest::encode, ReaderRequest::decode, (p, m) -> server.reader(p, m));
        Messages.toServer(TeachingRequest.class, TeachingRequest::encode, TeachingRequest::decode, (p, m) -> server.teaching(p, m));
        Messages.toClient(ReaderUpdate.class, ReaderUpdate::encode, ReaderUpdate::decode, m -> withClient(c -> c.reader(m)));
        Messages.toClient(DiscoveryUpdate.class, DiscoveryUpdate::encode, DiscoveryUpdate::decode, m -> withClient(c -> c.discovery(m)));
        Messages.toClient(ContentUpdate.class, ContentUpdate::encode, ContentUpdate::decode, m -> withClient(c -> c.content(m)));
        Messages.toClient(TeachingUpdate.class, TeachingUpdate::encode, TeachingUpdate::decode, m -> withClient(c -> c.teaching(m)));
    }

    private static void withClient(java.util.function.Consumer<ClientHandlers> action) {
        ClientHandlers c = client;
        if (c != null) {
            action.accept(c);
        }
    }

    /** Client wiring installs its handler during client setup. */
    public static void installClient(ClientHandlers handlers) {
        client = handlers;
    }

    /** Test seam: sees every message the server sends, with its recipient. */
    public static void observe(BiConsumer<ServerPlayer, Object> sink) {
        observer = sink;
    }

    /** Sends to one player only. Fake players have no client; they are only observed. */
    public static void send(ServerPlayer player, Object message) {
        BiConsumer<ServerPlayer, Object> o = observer;
        if (o != null) {
            o.accept(player, message);
        }
        Messages.send(player, message);
    }

    public static void sendToServer(Object message) {
        Messages.sendToServer(message);
    }
}
