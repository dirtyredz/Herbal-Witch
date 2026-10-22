package io.github.dirtyredz.herbalwitch.magic.ritual.network;

import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The altar's two messages: {@link AltarRequest} (client to server, intent from an open altar menu)
 * and {@link AltarUpdate} (server to client, that player's screen state). Transport only; the
 * server handler and the client handler are installed by the feature's registration and by client
 * wiring.
 */
public final class AltarPackets {
    private static volatile BiConsumer<ServerPlayer, AltarRequest> server;
    private static volatile Consumer<AltarUpdate> client;
    private static volatile BiConsumer<ServerPlayer, Object> observer;

    private AltarPackets() {
    }

    /** Called once at bootstrap, after the witch messages. */
    public static void register(BiConsumer<ServerPlayer, AltarRequest> handler) {
        server = handler;
        Messages.toServer(AltarRequest.class, AltarRequest::encode, AltarRequest::decode, (p, m) -> server.accept(p, m));
        Messages.toClient(AltarUpdate.class, AltarUpdate::encode, AltarUpdate::decode, m -> {
            Consumer<AltarUpdate> c = client;
            if (c != null) {
                c.accept(m);
            }
        });
    }

    public static void installClient(Consumer<AltarUpdate> handler) {
        client = handler;
    }

    /** Test seam: sees every altar message the server sends, with its recipient. */
    public static void observe(BiConsumer<ServerPlayer, Object> sink) {
        observer = sink;
    }

    public static void send(ServerPlayer player, AltarUpdate update) {
        BiConsumer<ServerPlayer, Object> o = observer;
        if (o != null) {
            o.accept(player, update);
        }
        Messages.send(player, update);
    }

    public static void sendToServer(AltarRequest request) {
        Messages.sendToServer(request);
    }
}
