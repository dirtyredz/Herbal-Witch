package io.github.dirtyredz.herbalwitch.companions.familiars.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.*;

public final class FamiliarServerConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue BINDING, PUBLIC;
    private static final Map<String, ForgeConfigSpec.BooleanValue> SPECIES = new LinkedHashMap<>();
    private static final ForgeConfigSpec.IntValue MOBILE, BYTES, INTERVAL, SCANS, PATHS, GUARD, SENSE, OFFLINE;
    private static volatile FamiliarRules current = FamiliarRules.DEFAULTS;
    private static long revision;
    private static final List<Runnable> LISTENERS = new java.util.concurrent.CopyOnWriteArrayList<>();
    static {
        var b = new ForgeConfigSpec.Builder();
        BINDING = b.comment("New bindings only; existing bonds remain usable and recoverable.").define("newBindings", true);
        PUBLIC = b.comment("Permit retrieval of unowned loose objects; the default accepts owner-attributed drops only.").define("publicRetrieval", false);
        MOBILE = b.defineInRange("mobilePerOwner", 3, 1, 8); BYTES = b.defineInRange("recordBytes", 128 * 1024, 4096, 128 * 1024);
        INTERVAL = b.defineInRange("transactionInterval", 5, 5, 1200); SCANS = b.defineInRange("scansPerDimensionTick", 8, 1, 32);
        PATHS = b.defineInRange("pathsPerDimensionTick", 4, 1, 16); GUARD = b.defineInRange("guardRadius", 16, 1, 24);
        SENSE = b.defineInRange("senseRadius", 32, 1, 48); OFFLINE = b.defineInRange("offlineGuardsPerOwner", 8, 1, 8);
        b.push("bindingSpecies"); FamiliarRules.DEFAULTS.species().stream().sorted().forEach(key -> SPECIES.put(key, b.define(key, true))); b.pop();
        SPEC = b.build();
    }
    private FamiliarServerConfig() {}
    public static FamiliarRules current() { return current; }
    public static long revision() { return revision; }
    public static void listen(Runnable listener) { LISTENERS.add(listener); }
    public static void register(IEventBus bus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "herbalwitch-familiars-server.toml"); bus.addListener(FamiliarServerConfig::changed);
    }
    static void install(FamiliarRules next) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !server.isSameThread()) throw new IllegalStateException("Familiar rules must install on the server thread");
        current = Objects.requireNonNull(next); revision++; LISTENERS.forEach(Runnable::run);
        io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent.invalidate();
    }
    private static void changed(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) return;
        try {
            Set<String> species = new HashSet<>(); SPECIES.forEach((key, enabled) -> { if (enabled.get()) species.add(key); });
            var next = new FamiliarRules(BINDING.get(), species, MOBILE.get(), BYTES.get(), INTERVAL.get(), SCANS.get(), PATHS.get(), GUARD.get(), SENSE.get(), OFFLINE.get(), PUBLIC.get());
            Runnable install = () -> install(next);
            var server = ServerLifecycleHooks.getCurrentServer(); if (server == null || server.isSameThread()) install.run(); else server.execute(install);
        } catch (RuntimeException failure) { org.slf4j.LoggerFactory.getLogger(FamiliarServerConfig.class).error("Invalid familiar configuration; retaining previous snapshot", failure); }
    }
}
