package io.github.dirtyredz.herbalwitch.qualification.upgrade;

import com.mojang.authlib.GameProfile;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Loads every saved player file into an offline player object, as a login would, records what the
 * candidate's capabilities decoded, and saves it back through vanilla's player save.
 */
final class UpgradePlayers {
    // The pinned 1.20.1 protected PlayerList#save(ServerPlayer), as the commerce checkpoint uses it.
    private static final Method SAVE = ObfuscationReflectionHelper.findMethod(PlayerList.class, "m_6765_", ServerPlayer.class);

    private UpgradePlayers() {
    }

    static void reload(MinecraftServer server, UpgradeReport report) {
        Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(f -> f.getFileName().toString().matches("[0-9a-f-]{36}\\.dat")).sorted().toList()) {
                reload(server, UUID.fromString(file.getFileName().toString().substring(0, 36)), report);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void reload(MinecraftServer server, UUID id, UpgradeReport report) {
        ServerPlayer player = new ServerPlayer(server, server.overworld(), new GameProfile(id, "upgrade_" + id.toString().substring(0, 8)));
        if (server.getPlayerList().load(player) == null) {
            report.fail("player " + id + " did not load");
            return;
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("uuid", id.toString());
        entry.put("witch", WitchStatusService.isWitch(player));
        entry.put("energyUnits", WitchStatusService.energyUnits(player));
        entry.put("capacityUnits", WitchStatusService.capacityUnits(player));
        entry.put("discoveries", DiscoveryCapability.of(player).map(c -> c.state().size()).orElse(-1));
        report.player(entry);
        try {
            SAVE.invoke(server.getPlayerList(), player);
        } catch (ReflectiveOperationException e) {
            report.fail("player " + id + " could not be saved: " + e);
        }
    }
}
