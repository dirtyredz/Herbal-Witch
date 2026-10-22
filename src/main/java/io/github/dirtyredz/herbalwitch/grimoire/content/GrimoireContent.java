package io.github.dirtyredz.herbalwitch.grimoire.content;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.grimoire.content.input.HerbSummary;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The immutable grimoire index of one content revision: every entry the registered sources produce
 * from the live recipes, the server's herb conditions and the editorial definitions, with the
 * editorial overlays applied and dangling links dropped. Built identically on the server and on
 * each client (which receives the server's definitions and conditions, and its recipes through
 * vanilla recipe synchronisation). A removed recipe simply has no entry in the next revision.
 */
public final class GrimoireContent {
    public static final String GUIDE = "guide";
    public static final String STATIONS = "stations";
    public static final String HERBS = "herbs";
    public static final String FURNITURE = "furniture";
    public static final String WITCHCRAFT = "witchcraft";
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Reading order is each source's numeric priority, independent of registry/showcase/network bootstrap order. */
    private static final Contributions<GrimoireContentSource> SOURCES = new Contributions<>();

    /**
     * What every source reads.
     *
     * @param recipes     the side's recipe manager
     * @param herbs       herb conditions by plant id (the server's, never a client's local defaults)
     * @param definitions editorial definitions by entry key
     */
    public record Inputs(RecipeManager recipes, Map<String, HerbSummary> herbs,
                         Map<EntryKey, GrimoireDefinition> definitions) {
        public Inputs {
            herbs = Map.copyOf(herbs);
            definitions = Map.copyOf(definitions);
        }
    }

    private final int revision;
    private final Map<EntryKey, GrimoireEntry> entries;

    private GrimoireContent(int revision, Map<EntryKey, GrimoireEntry> entries) {
        this.revision = revision;
        this.entries = Collections.unmodifiableMap(entries);
    }

    /** Registers a feature source at its own priority (lower first); equal priorities retain insertion order and first-key wins. */
    public static void register(int priority, GrimoireContentSource source) {
        SOURCES.add(priority, source);
    }

    /** The registered sources in index order. */
    public static List<GrimoireContentSource> sources() {
        return SOURCES.ordered();
    }

    public static GrimoireContent empty() {
        return new GrimoireContent(0, new LinkedHashMap<>());
    }

    public static GrimoireContent build(int revision, Inputs inputs) {
        return build(revision, inputs, SOURCES.ordered());
    }

    static GrimoireContent build(int revision, Inputs inputs, List<GrimoireContentSource> sources) {
        Map<EntryKey, GrimoireEntry> built = new LinkedHashMap<>();
        for (GrimoireContentSource source : sources) {
            source.contribute(inputs, entry -> {
                if (built.putIfAbsent(entry.key(), entry) != null) {
                    LOGGER.warn("Two grimoire sources produce {}; keeping the first", entry.key());
                }
            });
        }
        Map<EntryKey, GrimoireEntry> overlaid = new LinkedHashMap<>();
        built.forEach((key, entry) -> {
            GrimoireDefinition definition = inputs.definitions().get(key);
            overlaid.put(key, definition == null || key.kind() == EntryKey.Kind.GUIDE ? entry : entry.withEditorial(definition));
        });
        Map<EntryKey, GrimoireEntry> linked = new LinkedHashMap<>();
        overlaid.forEach((key, entry) -> linked.put(key, entry.withLinksIn(overlaid::containsKey)));
        return new GrimoireContent(revision, linked);
    }

    public int revision() {
        return revision;
    }

    /** Every entry in index order. */
    public List<GrimoireEntry> entries() {
        return new ArrayList<>(entries.values());
    }

    public Optional<GrimoireEntry> get(EntryKey key) {
        return Optional.ofNullable(entries.get(key));
    }

    public boolean contains(EntryKey key) {
        return entries.containsKey(key);
    }

    /** Recipe entries, in index order. */
    public List<GrimoireEntry> recipes() {
        return entries.values().stream().filter(e -> e.recipe().isPresent()).toList();
    }

    /** The live recipe behind a recipe key, if it is present in this revision. */
    public Optional<RecipeView> recipe(EntryKey key) {
        return get(key).flatMap(GrimoireEntry::recipe);
    }

    public int size() {
        return entries.size();
    }
}
