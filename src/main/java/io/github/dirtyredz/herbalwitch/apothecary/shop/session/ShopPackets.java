package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * The commerce messages on the mod's one channel, registered after every earlier feature's (protocol
 * 14): {@link ShopRequest} to the server and {@link ShopUpdate} to its requester. The client handler is
 * installed by client wiring.
 */
public final class ShopPackets {
    private static Consumer<ShopUpdate> client = ignored -> { };

    private ShopPackets() {
    }

    public static void register() {
        Messages.toServer(ShopRequest.class, ShopRequest::encode, ShopRequest::decode, ShopSessionService::request);
        Messages.toClient(ShopUpdate.class, ShopUpdate::encode, ShopUpdate::decode, update -> client.accept(update));
    }

    public static void client(Consumer<ShopUpdate> handler) {
        client = handler;
    }

    public static void send(ServerPlayer player, ShopUpdate update) {
        Messages.send(player, update);
    }

    public static void request(ShopRequest request) {
        Messages.sendToServer(request);
    }
}
