package io.github.dirtyredz.herbalwitch.companions.animals.config;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Installs the whole validated configuration on the server thread; reload invalidates inspection and AI lazily. */
public final class AnimalServerConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue RELATIONSHIPS, ATTRACTION, CALMING, ADOPTION, CAPTURES, MODDED;
    private static final ForgeConfigSpec.IntValue RELATIONS, RADIUS, CANDIDATES, ATTRACTED, GOALS, CHECKS, PATHS,
            PAYLOAD, DEPTH, CAPTOR, STORED, LIFETIME, SNAPSHOT, INTERVAL;
    private static volatile AnimalRules current = AnimalRules.DEFAULTS;
    private static long generation;
    private static final java.util.List<Runnable> LISTENERS = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        var b = new ForgeConfigSpec.Builder();
        RELATIONSHIPS = b.define("relationships", true); ATTRACTION = b.define("attraction", true);
        CALMING = b.define("calming", true); ADOPTION = b.define("earnedTaming", true);
        CAPTURES = b.comment("Disables new captures only. Existing records remain releasable/recoverable.").define("newCaptures", true);
        MODDED = b.define("moddedParticipation", false);
        RELATIONS = b.defineInRange("relationshipsPerAnimal", 8, 1, 16); RADIUS = b.defineInRange("baitRadius", 8, 1, 12);
        CANDIDATES = b.defineInRange("baitCandidates", 8, 1, 16); ATTRACTED = b.defineInRange("attractedPerPlayer", 8, 1, 16);
        GOALS = b.defineInRange("activeGoalsPerDimension", 256, 1, 512); CHECKS = b.defineInRange("candidateChecksPerTick", 128, 1, 256);
        PATHS = b.defineInRange("pathsPerTick", 16, 1, 32); PAYLOAD = b.defineInRange("payloadBytes", 32768, 1024, 65536);
        DEPTH = b.defineInRange("nbtDepth", 16, 1, 32); CAPTOR = b.defineInRange("storedPerCaptor", 16, 1, 64);
        STORED = b.defineInRange("storedPerWorld", 128, 1, 512); LIFETIME = b.defineInRange("lifetimeRecords", 4096, 1, 16384);
        SNAPSHOT = b.defineInRange("snapshotBytes", 8 * 1024 * 1024, 65536, 16 * 1024 * 1024);
        INTERVAL = b.defineInRange("transactionInterval", 20, 5, 1200);
        SPEC = b.build();
    }

    private AnimalServerConfig() {}
    public static AnimalRules current() { return current; }
    public static long generation() { return generation; }
    public static void listen(Runnable listener) { LISTENERS.add(listener); }
    public static void tagsUpdated(TagsUpdatedEvent event) { if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) generation++; }
    public static void reset() { current = AnimalRules.DEFAULTS; generation++; }

    public static void register(IEventBus bus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "herbalwitch-animals-server.toml");
        bus.addListener(AnimalServerConfig::changed);
    }

    private static void changed(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) return;
        try {
            var candidate = new AnimalRules(RELATIONSHIPS.get(), ATTRACTION.get(), CALMING.get(), ADOPTION.get(), CAPTURES.get(), MODDED.get(),
                    RELATIONS.get(), RADIUS.get(), CANDIDATES.get(), ATTRACTED.get(), GOALS.get(), CHECKS.get(), PATHS.get(),
                    PAYLOAD.get(), DEPTH.get(), CAPTOR.get(), STORED.get(), LIFETIME.get(), SNAPSHOT.get(), INTERVAL.get());
            Runnable install = () -> {
                current = candidate; generation++;
                io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent.invalidate();
                LISTENERS.forEach(Runnable::run);
            };
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server == null || server.isSameThread()) install.run(); else server.execute(install);
        } catch (RuntimeException ex) {
            LogUtils.getLogger().error("Invalid animal configuration; retaining previous rules", ex);
        }
    }
}
