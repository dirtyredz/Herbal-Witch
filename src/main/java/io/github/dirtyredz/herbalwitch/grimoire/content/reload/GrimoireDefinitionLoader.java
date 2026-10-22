package io.github.dirtyredz.herbalwitch.grimoire.content.reload;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads {@code data/<ns>/grimoire_entries/**.json} on server start and {@code /reload}. The whole
 * set is validated first ({@link GrimoireDefinition#parseAll}); a valid set replaces the current one
 * in one assignment, an invalid one is logged with every reason and ignored, so the previous
 * editorial set stays. Editorial failures never keep a removed recipe alive: recipe pages come from
 * the live recipes when the content is rebuilt after the reload.
 */
public final class GrimoireDefinitionLoader extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "grimoire_entries";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<EntryKey, GrimoireDefinition> current = Map.of();

    public GrimoireDefinitionLoader() {
        super(new Gson(), DIRECTORY);
    }

    /** The editorial set in force. */
    public static Map<EntryKey, GrimoireDefinition> current() {
        return current;
    }

    /** Forge bus listener. */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new GrimoireDefinitionLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        Map<String, JsonElement> named = new LinkedHashMap<>();
        files.forEach((id, json) -> named.put(id.toString(), json));
        replace(named);
    }

    /**
     * Validates a reload and, only if it is entirely valid, makes it current. Returns the errors
     * (empty when accepted).
     */
    public static List<String> replace(Map<String, JsonElement> files) {
        GrimoireDefinition.Result result = GrimoireDefinition.parseAll(files);
        if (result.ok()) {
            current = Map.copyOf(result.definitions());
            LOGGER.debug("Loaded {} grimoire definitions", result.definitions().size());
        } else {
            LOGGER.error("Rejected grimoire definitions, keeping the previous {}: {}", current.size(), result.errors());
        }
        return result.errors();
    }
}
