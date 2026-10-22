package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;

import java.util.List;

/**
 * Sends the content inputs (editorial definitions and herb conditions) of the current revision:
 * to a joining player, and to everyone after a {@code /reload}, which is when Forge synchronises
 * datapack contents (after recipes and tags settle, so the rebuilt content matches them).
 */
public final class ContentSync {
    private ContentSync() {
    }

    /** Forge bus. */
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        MinecraftServer server = event.getPlayerList().getServer();
        GrimoireContent content = ServerGrimoireContent.get(server);
        List<ServerPlayer> targets = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
        List<ContentUpdate> updates = ContentUpdate.of(content.revision(), GrimoireDefinitionLoader.current(),
                ServerGrimoireContent.herbSummaries());
        for (ServerPlayer player : targets) {
            updates.forEach(u -> GrimoirePackets.send(player, u));
        }
    }
}
