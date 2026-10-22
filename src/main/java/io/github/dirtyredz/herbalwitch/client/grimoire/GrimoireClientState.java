package io.github.dirtyredz.herbalwitch.client.grimoire;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.grimoire.content.input.HerbSummary;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.grimoire.network.ContentUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.network.DiscoveryUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingUpdate;
import io.github.dirtyredz.herbalwitch.platform.network.ChunkAssembly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Everything the client knows about the grimoire for the current connection: the server's content
 * inputs (editorial definitions, herb conditions) and revision, the player's own discoveries, the
 * open reader's counts and nearby players, and pending invitations. Content is rebuilt from the
 * client's recipe manager (vanilla-synchronised) on demand and invalidated on every recipe or tag
 * update; all of it is cleared on disconnect, so nothing leaks into the next world.
 */
public final class GrimoireClientState implements GrimoirePackets.ClientHandlers {
    public static final GrimoireClientState INSTANCE = new GrimoireClientState();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ChunkAssembly<ContentUpdate.Record> contentChunks = new ChunkAssembly<>();
    private final ChunkAssembly<EntryKey> discoveryChunks = new ChunkAssembly<>();
    private Map<EntryKey, GrimoireDefinition> definitions = Map.of();
    private Map<String, HerbSummary> herbs = Map.of();
    private int revision;
    private final Set<EntryKey> discoveries = new HashSet<>();
    private GrimoireContent content;
    private int generation;
    private ReaderUpdate availability;
    private List<ReaderUpdate.Nearby> nearby = List.of();
    private Optional<TeachingUpdate> incoming = Optional.empty();
    private Optional<TeachingUpdate> outgoing = Optional.empty();

    private GrimoireClientState() {
    }

    /** Bumped on every change, so open views know to refresh. */
    public int generation() {
        return generation;
    }

    public int revision() {
        return revision;
    }

    public boolean knows(EntryKey key) {
        return discoveries.contains(key);
    }

    /** The content of the current revision, built from this client's recipes. */
    public GrimoireContent content() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return GrimoireContent.empty();
        }
        if (content == null) {
            content = GrimoireContent.build(revision, new GrimoireContent.Inputs(connection.getRecipeManager(), herbs, definitions));
        }
        return content;
    }

    /** Counts for {@code key} from the open reader, if the server sent them for that reader. */
    public Optional<ReaderUpdate> availability(int containerId, long session, EntryKey key) {
        return Optional.ofNullable(availability).filter(a -> a.containerId() == containerId && a.session() == session
                && a.key().equals(Optional.of(key)));
    }

    public List<ReaderUpdate.Nearby> nearby() {
        return nearby;
    }

    public Optional<TeachingUpdate> incoming() {
        return incoming;
    }

    public Optional<TeachingUpdate> outgoing() {
        return outgoing;
    }

    @Override
    public void reader(ReaderUpdate update) {
        if (update.kind() == ReaderUpdate.Kind.AVAILABILITY) {
            availability = update;
        } else {
            nearby = update.nearby();
        }
        generation++;
    }

    @Override
    public void discovery(DiscoveryUpdate update) {
        if (!update.snapshot()) {
            discoveries.addAll(update.keys());
            generation++;
            return;
        }
        try {
            discoveryChunks.offer(update.header(), update.keys()).ifPresent(all -> {
                discoveries.clear();
                discoveries.addAll(all);
                generation++;
            });
        } catch (IllegalStateException e) {
            LOGGER.warn("Discarded an inconsistent grimoire snapshot: {}", e.getMessage());
        }
    }

    @Override
    public void content(ContentUpdate update) {
        try {
            contentChunks.offer(update.header(), update.records()).ifPresent(all -> {
                Map<EntryKey, GrimoireDefinition> defs = new LinkedHashMap<>();
                Map<String, HerbSummary> conditions = new LinkedHashMap<>();
                for (ContentUpdate.Record record : all) {
                    if (record instanceof ContentUpdate.Definition d) {
                        defs.put(d.definition().key(), d.definition());
                    } else if (record instanceof ContentUpdate.Herb h) {
                        conditions.put(h.condition().herb(), h.condition());
                    }
                }
                definitions = defs;
                herbs = conditions;
                revision = update.header().revision();
                invalidate();
            });
        } catch (IllegalStateException e) {
            LOGGER.warn("Discarded inconsistent grimoire content: {}", e.getMessage());
        }
    }

    @Override
    public void teaching(TeachingUpdate update) {
        switch (update.kind()) {
            case INCOMING -> incoming = Optional.of(update);
            case OUTGOING -> outgoing = Optional.of(update);
            case INCOMING_CLEARED -> incoming = Optional.empty();
            case OUTGOING_CLEARED -> outgoing = Optional.empty();
        }
        generation++;
    }

    /** Recipes or tags changed: rebuild the content on next use. */
    public void invalidate() {
        content = null;
        generation++;
    }

    /** Disconnect: forget this connection entirely. */
    public void reset() {
        contentChunks.reset();
        discoveryChunks.reset();
        definitions = Map.of();
        herbs = Map.of();
        revision = 0;
        discoveries.clear();
        content = null;
        availability = null;
        nearby = List.of();
        incoming = Optional.empty();
        outgoing = Optional.empty();
        generation++;
    }

    static void onRecipesUpdated(RecipesUpdatedEvent event) {
        INSTANCE.invalidate();
    }

    static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            INSTANCE.invalidate();
        }
    }

    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        INSTANCE.reset();
    }
}
