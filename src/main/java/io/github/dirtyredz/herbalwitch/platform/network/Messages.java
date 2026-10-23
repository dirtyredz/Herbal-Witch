package io.github.dirtyredz.herbalwitch.platform.network;

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
 * Registration and delivery helpers every feature's messages share on {@link HerbalWitchNetwork}:
 * one fixed direction per message (Forge drops a message arriving the wrong way), handlers run on
 * the main thread, a request names its sender (never a player id from the payload), and a server
 * message goes to one player only. Fake players have no connection; they are skipped. No policy.
 */
public final class Messages {
    private Messages() {
    }

    /** A client-to-server message; {@code handler} receives the real sender. */
    public static <M> void toServer(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                    BiConsumer<ServerPlayer, M> handler) {
        HerbalWitchNetwork.CHANNEL.messageBuilder(type, HerbalWitchNetwork.nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(encoder).decoder(decoder)
                .consumerMainThread((M message, Supplier<NetworkEvent.Context> context) -> {
                    ServerPlayer sender = context.get().getSender();
                    if (sender != null) {
                        handler.accept(sender, message);
                    }
                }).add();
    }

    /** A server-to-client message; {@code handler} is installed later by client wiring. */
    public static <M> void toClient(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                    Consumer<M> handler) {
        HerbalWitchNetwork.CHANNEL.messageBuilder(type, HerbalWitchNetwork.nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(encoder).decoder(decoder)
                .consumerMainThread((M message, Supplier<NetworkEvent.Context> context) -> handler.accept(message)).add();
    }

    /** Sends to one connected player; fake players (GameTests) have no client and receive nothing. */
    public static void send(ServerPlayer player, Object message) {
        if (!(player instanceof FakePlayer) && player.connection != null) {
            HerbalWitchNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    public static void tracking(net.minecraft.world.entity.Entity entity, Object message) {
        HerbalWitchNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
    public static void sendToServer(Object message) {
        HerbalWitchNetwork.CHANNEL.sendToServer(message);
    }
}
