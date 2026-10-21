package io.github.dirtyredz.herbalwitch.botany.conditions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Loads {@code data/<ns>/herb_conditions/**.json} on server start and {@code /reload}. A valid set
 * replaces the current one in a single assignment; an invalid set is logged and ignored, so the
 * previous conditions stay in force. Conditions only affect future foraging and harvests.
 */
public final class HerbConditionReloadListener extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "herb_conditions";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile HerbConditions current = HerbConditions.defaults();

    public HerbConditionReloadListener() {
        super(new Gson(), DIRECTORY);
    }

    public static HerbConditions current() {
        return current;
    }

    /** Forge bus listener. */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new HerbConditionReloadListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        Set<String> known = new HashSet<>();
        for (HerbSpec herb : HerbCatalog.ALL) {
            String id = HerbConditions.plantId(herb);
            if (ForgeRegistries.BLOCKS.containsKey(ResourceLocation.parse(id))) {
                known.add(id);
            }
        }
        Map<String, JsonElement> named = new LinkedHashMap<>();
        files.forEach((id, json) -> named.put(id.toString(), json));
        HerbConditions.Result result = HerbConditions.parse(named, known);
        if (result.ok()) {
            current = result.conditions();
            LOGGER.debug("Loaded {} herb conditions", result.conditions().all().size());
        } else {
            LOGGER.error("Rejected herb conditions, keeping the previous set: {}", result.errors());
        }
    }
}
