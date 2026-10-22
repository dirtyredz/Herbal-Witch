package io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching;

import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link TeachingService.World} over a running server: players are looked up by id among the
 * online players (plus, in GameTests only, registered fake players, which never join the player
 * list), knowledge and grants go through the discovery service, and fingerprints come from the
 * server's current content.
 */
public final class TeachingWorld implements TeachingService.World {
    private static volatile Collection<ServerPlayer> testPlayers = List.of();
    private final MinecraftServer server;

    public TeachingWorld(MinecraftServer server) {
        this.server = server;
    }

    /** GameTest seam: fake players to treat as online. Pass an empty list to clear. */
    public static void setTestPlayers(Collection<ServerPlayer> players) {
        testPlayers = List.copyOf(players);
    }

    public Optional<ServerPlayer> player(UUID id) {
        ServerPlayer online = server.getPlayerList().getPlayer(id);
        if (online != null) {
            return Optional.of(online);
        }
        return testPlayers.stream().filter(p -> p.getUUID().equals(id) && !p.isRemoved()).findFirst();
    }

    /** Players the teacher could invite now, by name. */
    public List<ServerPlayer> nearby(ServerPlayer teacher) {
        List<ServerPlayer> candidates = new ArrayList<>(server.getPlayerList().getPlayers());
        testPlayers.stream().filter(p -> !candidates.contains(p)).forEach(candidates::add);
        return candidates.stream().filter(p -> p != teacher && close(teacher, p))
                .sorted(Comparator.comparing(p -> p.getGameProfile().getName())).toList();
    }

    private static boolean close(ServerPlayer a, ServerPlayer b) {
        return a.level() == b.level() && a.isAlive() && b.isAlive() && !a.isSpectator() && !b.isSpectator()
                && !a.isRemoved() && !b.isRemoved() && a.distanceToSqr(b) <= TeachingService.RANGE * TeachingService.RANGE;
    }

    @Override
    public boolean inRange(UUID teacher, UUID recipient) {
        Optional<ServerPlayer> a = player(teacher);
        Optional<ServerPlayer> b = player(recipient);
        return a.isPresent() && b.isPresent() && close(a.get(), b.get());
    }

    @Override
    public long readerSession(UUID id) {
        return player(id).filter(p -> p.containerMenu instanceof GrimoireMenu menu && menu.session() != null && menu.stillValid(p))
                .map(p -> ((GrimoireMenu) p.containerMenu).nonce()).orElse(0L);
    }

    @Override
    public boolean knows(UUID id, EntryKey recipe) {
        return player(id).map(p -> DiscoveryService.knows(p, recipe)).orElse(false);
    }

    @Override
    public Optional<String> fingerprint(EntryKey recipe) {
        return ServerGrimoireContent.get(server).recipe(recipe).map(ProcessRecipe::fingerprint);
    }

    @Override
    public boolean grant(UUID recipient, EntryKey recipe) {
        return player(recipient).map(p -> {
            DiscoveryService.grant(p, recipe);
            return DiscoveryService.knows(p, recipe);
        }).orElse(false);
    }
}
