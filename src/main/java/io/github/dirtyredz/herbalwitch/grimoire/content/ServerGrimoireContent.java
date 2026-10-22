package io.github.dirtyredz.herbalwitch.grimoire.content;

import io.github.dirtyredz.herbalwitch.grimoire.content.input.HerbSummary;

import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The server's current grimoire content. It is rebuilt (with a new revision) whenever its inputs
 * have settled into something new: another recipe manager, tag generation, editorial set or herb
 * condition set, which is what a {@code /reload} produces. Listeners (content sync to clients,
 * teaching invalidation) hear every new revision.
 */
public final class ServerGrimoireContent {
    private static final CopyOnWriteArrayList<Consumer<GrimoireContent>> LISTENERS = new CopyOnWriteArrayList<>();
    private static GrimoireContent current = GrimoireContent.empty();
    private static RecipeManager recipes;
    private static long tagGeneration = -1;
    private static Map<EntryKey, GrimoireDefinition> definitions;
    private static Map<String, HerbSummary> herbs;
    private static java.util.function.Supplier<Map<String, HerbSummary>> herbSource = Map::of;
    private static java.util.function.LongSupplier recipeGeneration = () -> 0;
    private static int revision;

    private ServerGrimoireContent() {
    }

    /** Bootstrap adapters supply neutral facts; stable supplier identity results drive invalidation. */
    public static void herbSource(java.util.function.Supplier<Map<String, HerbSummary>> source) {
        herbSource = source;
    }

    public static void recipeGeneration(java.util.function.LongSupplier source) {
        recipeGeneration = source;
    }

    /** Called with each newly built revision, on the server thread. */
    public static void listen(Consumer<GrimoireContent> listener) {
        LISTENERS.add(listener);
    }

    /** The current content, rebuilt first if any input changed since it was built. */
    public static GrimoireContent get(MinecraftServer server) {
        GrimoireContent built;
        synchronized (ServerGrimoireContent.class) {
            if (fresh(server)) {
                return current;
            }
            recipes = server.getRecipeManager();
            tagGeneration = recipeGeneration.getAsLong();
            definitions = GrimoireDefinitionLoader.current();
            herbs = herbSource.get();
            current = built = GrimoireContent.build(++revision, new GrimoireContent.Inputs(recipes, herbSummaries(), definitions));
        }
        LISTENERS.forEach(l -> l.accept(built));
        return built;
    }

    private static boolean fresh(MinecraftServer server) {
        return current.revision() != 0 && recipes == server.getRecipeManager() && tagGeneration == recipeGeneration.getAsLong()
                && definitions == GrimoireDefinitionLoader.current() && herbs == herbSource.get();
    }

    /** The server's herb conditions by plant id: what clients receive instead of their local defaults. */
    public static Map<String, HerbSummary> herbSummaries() {
        return herbSource.get();
    }

    /** Forgets everything when the server stops, so the next world starts from revision 1. */
    public static synchronized void reset() {
        current = GrimoireContent.empty();
        recipes = null;
        tagGeneration = -1;
        definitions = null;
        herbs = null;
        revision = 0;
    }
}
