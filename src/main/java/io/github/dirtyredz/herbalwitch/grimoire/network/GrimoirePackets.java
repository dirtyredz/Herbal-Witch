package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.platform.network.HerbalWitchNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registers the grimoire's six messages on the mod channel, each with one fixed direction (Forge
 * drops a message arriving the wrong way), and routes them to handlers wired by the feature's
 * registration: server handlers for requests, a client handler installed by client-only code. This
 * class holds no policy and imports no gameplay service, so transport and rules stay apart.
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
        toServer(ReaderRequest.class, ReaderRequest::encode, ReaderRequest::decode, (p, m) -> server.reader(p, m));
        toServer(TeachingRequest.class, TeachingRequest::encode, TeachingRequest::decode, (p, m) -> server.teaching(p, m));
        toClient(ReaderUpdate.class, ReaderUpdate::encode, ReaderUpdate::decode, m -> client.reader(m));
        toClient(DiscoveryUpdate.class, DiscoveryUpdate::encode, DiscoveryUpdate::decode, m -> client.discovery(m));
        toClient(ContentUpdate.class, ContentUpdate::encode, ContentUpdate::decode, m -> client.content(m));
        toClient(TeachingUpdate.class, TeachingUpdate::encode, TeachingUpdate::decode, m -> client.teaching(m));
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
        if (!(player instanceof FakePlayer) && player.connection != null) {
            HerbalWitchNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    public static void sendToServer(Object message) {
        HerbalWitchNetwork.CHANNEL.sendToServer(message);
    }

    private static <M> void toServer(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                     BiConsumer<ServerPlayer, M> handler) {
        HerbalWitchNetwork.CHANNEL.messageBuilder(type, HerbalWitchNetwork.nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(encoder).decoder(decoder)
                .consumerMainThread((M message, Supplier<NetworkEvent.Context> context) -> {
                    ServerPlayer sender = context.get().getSender();
                    if (sender != null && server != null) {
                        handler.accept(sender, message);
                    }
                }).add();
    }

    private static <M> void toClient(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                     Consumer<M> handler) {
        HerbalWitchNetwork.CHANNEL.messageBuilder(type, HerbalWitchNetwork.nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(encoder).decoder(decoder)
                .consumerMainThread((M message, Supplier<NetworkEvent.Context> context) -> {
                    if (client != null) {
                        handler.accept(message);
                    }
                }).add();
    }
}
