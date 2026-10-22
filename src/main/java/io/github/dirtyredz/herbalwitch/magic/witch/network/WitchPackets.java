package io.github.dirtyredz.herbalwitch.magic.witch.network;

import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

/**
 * The witch feature's two messages, both server to client: the private {@link WitchSnapshot} and the
 * public {@link WitchRulesUpdate}. Requests reach the server only through the altar's menu-bound
 * messages; nothing here lets a client change its status or energy. Transport only.
 */
public final class WitchPackets {
    /** What the client does with an update (installed by client wiring). */
    public interface ClientHandlers {
        void snapshot(WitchSnapshot snapshot);

        void rules(WitchRulesUpdate update);
    }

    private static volatile ClientHandlers client;
    private static volatile BiConsumer<ServerPlayer, Object> observer;

    private WitchPackets() {
    }

    /** Called once at bootstrap, after the grimoire's messages (ids follow registration order). */
    public static void register() {
        Messages.toClient(WitchSnapshot.class, WitchSnapshot::encode, WitchSnapshot::decode, m -> {
            ClientHandlers c = client;
            if (c != null) {
                c.snapshot(m);
            }
        });
        Messages.toClient(WitchRulesUpdate.class, WitchRulesUpdate::encode, WitchRulesUpdate::decode, m -> {
            ClientHandlers c = client;
            if (c != null) {
                c.rules(m);
            }
        });
    }

    public static void installClient(ClientHandlers handlers) {
        client = handlers;
    }

    /** Test seam: sees every witch message the server sends, with its recipient. */
    public static void observe(BiConsumer<ServerPlayer, Object> sink) {
        observer = sink;
    }

    public static void send(ServerPlayer player, Object message) {
        BiConsumer<ServerPlayer, Object> o = observer;
        if (o != null) {
            o.accept(player, message);
        }
        Messages.send(player, message);
    }
}
