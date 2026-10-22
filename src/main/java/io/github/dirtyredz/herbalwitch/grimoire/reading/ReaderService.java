package io.github.dirtyredz.herbalwitch.grimoire.reading;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients.IngredientRequirements;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderRequest;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.research.Familiarity;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.research.ResearchService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Server side of reading: validates every request against the open reader (menu id, session nonce,
 * content revision, access still valid) before anything changes, keeps each session's selection,
 * computes the selected recipe's ingredient counts against the main inventory and offhand (never
 * reserving or consuming anything) and sends only changed counts, at most every
 * {@link #UPDATE_INTERVAL} ticks. Research requests are decided by {@link ResearchService} and
 * granted through {@link DiscoveryService}.
 */
public final class ReaderService {
    public static final int UPDATE_INTERVAL = 5;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<BiConsumer<ServerPlayer, GrimoireMenu>> OPENED = new CopyOnWriteArrayList<>();

    private ReaderService() {
    }

    /** Other parts of the grimoire (teaching) hear when a reader opens. */
    public static void onOpened(BiConsumer<ServerPlayer, GrimoireMenu> listener) {
        OPENED.add(listener);
    }

    /** A reader just opened: observe the inventory (an acquisition trigger) and tell listeners. */
    public static void opened(ServerPlayer player, GrimoireMenu menu) {
        DiscoveryService.observe(player);
        OPENED.forEach(l -> l.accept(player, menu));
    }

    /** The player's open reader, if the request names it exactly and it is still valid. */
    public static Optional<GrimoireMenu> validMenu(ServerPlayer player, int containerId, long session, int revision) {
        if (!(player.containerMenu instanceof GrimoireMenu menu) || menu.containerId != containerId || menu.nonce() != session
                || menu.session() == null || !menu.stillValid(player) || player.getServer() == null
                || ServerGrimoireContent.get(player.getServer()).revision() != revision) {
            return Optional.empty();
        }
        return Optional.of(menu);
    }

    public static void handle(ServerPlayer player, ReaderRequest request) {
        Optional<GrimoireMenu> menu = validMenu(player, request.containerId(), request.session(), request.revision());
        if (menu.isEmpty()) {
            LOGGER.debug("Ignored a stale or invalid grimoire request from {}", player.getGameProfile().getName());
            return;
        }
        GrimoireContent content = ServerGrimoireContent.get(player.getServer());
        switch (request.op()) {
            case SELECT -> {
                Optional<EntryKey> key = request.key().filter(k -> content.recipe(k).isPresent());
                menu.get().session().select(key);
            }
            case RESEARCH -> request.key().ifPresent(key -> research(player, menu.get(), content, key));
        }
    }

    /** Researches one recipe at the reader's research block; returns the outcome. */
    public static ResearchService.Result research(ServerPlayer player, GrimoireMenu menu, GrimoireContent content, EntryKey key) {
        ResearchService.Result result = ResearchService.check(player, content, key, menu.access().atResearchBlock()
                && menu.stillValid(player), k -> DiscoveryService.knows(player, k));
        if (result == ResearchService.Result.RESEARCHED) {
            DiscoveryService.grant(player, key);
        }
        player.displayClientMessage(Component.translatable(result.messageKey(),
                content.get(key).map(e -> e.title()).orElse(Component.literal(key.id()))), true);
        return result;
    }

    /** Forge bus. */
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            tick(player);
        }
    }

    /** Recomputes and sends the open reader's counts when due and changed. */
    public static void tick(ServerPlayer player) {
        if (!(player.containerMenu instanceof GrimoireMenu menu) || menu.session() == null || player.getServer() == null) {
            return;
        }
        ReaderSession session = menu.session();
        long tick = player.getServer().getTickCount();
        if (!session.due(tick)) {
            return;
        }
        GrimoireContent content = ServerGrimoireContent.get(player.getServer());
        List<ItemStack> carried = Familiarity.carried(player);
        long fingerprint = Familiarity.fingerprint(carried);
        if (!session.stale(fingerprint, content.revision())) {
            return;
        }
        Optional<EntryKey> selected = session.selected().filter(k -> content.recipe(k).isPresent());
        if (selected.isEmpty()) {
            session.cleared(fingerprint, content.revision());
            return;
        }
        List<IngredientRequirements.Count> counts = IngredientRequirements.count(
                content.recipe(selected.get()).orElseThrow().description(), carried);
        if (session.computed(tick, fingerprint, content.revision(), selected.get(), counts)) {
            GrimoirePackets.send(player, ReaderUpdate.availability(menu.containerId, menu.nonce(), selected.get(),
                    counts.stream().map(c -> new ReaderUpdate.Line(c.required(), c.available())).toList()));
        }
    }
}
