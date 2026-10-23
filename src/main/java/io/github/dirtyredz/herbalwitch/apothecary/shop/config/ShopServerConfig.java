package io.github.dirtyredz.herbalwitch.apothecary.shop.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@code herbalwitch-commerce-server.toml}: the commerce settings, validated as a whole into
 * {@link ShopRules} on the server thread. An invalid edit keeps the previous rules; a valid change
 * bumps the revision and notifies listeners (sessions and customer visits are invalidated).
 */
public final class ShopServerConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue SHOPS_PER_OWNER, SHOPS_PER_WORLD, PROCEEDS, WINDOW, WORLD, OWNER, SHOP, VILLAGER,
            INTERVAL, DURATION, CONCURRENT, LIFETIME;
    private static final ForgeConfigSpec.BooleanValue NPC;
    private static volatile ShopRules current = ShopRules.DEFAULTS;
    private static long revision;
    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    static {
        ShopRules d = ShopRules.DEFAULTS;
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("shops");
        SHOPS_PER_OWNER = b.defineInRange("shopsPerOwner", d.shopsPerOwner(), 1, 16);
        SHOPS_PER_WORLD = b.defineInRange("shopsPerWorld", d.shopsPerWorld(), 1, 4096);
        PROCEEDS = b.comment("Emeralds a shop's proceeds may hold. Lowering it blocks new sales, never deletes a balance.")
                .defineInRange("proceedsCapacity", (int) d.proceedsCapacity(), 64, 1_000_000);
        LIFETIME = b.comment("Customer request lifetime, in economy windows.").defineInRange("orderLifetimeWindows", d.orderLifetimeWindows(), 1, 28);
        b.pop();
        b.push("villagerCustomers");
        NPC = b.comment("Nearby adult villagers may buy eligible preparations from open shops whose owner enabled it.").define("enabled", d.npcService());
        WINDOW = b.comment("Economy window in running server ticks; budgets reset only when a window has fully elapsed.")
                .defineInRange("economyWindow", d.economyWindow(), 24_000, 168_000);
        WORLD = b.defineInRange("worldSpending", d.worldSpending(), 0, 1024);
        OWNER = b.comment("Emeralds villagers may spend per window across all of one owner's shops.").defineInRange("ownerSpending", d.ownerSpending(), 0, 128);
        SHOP = b.defineInRange("shopSpending", d.shopSpending(), 0, 64);
        VILLAGER = b.comment("Purchases one villager may make per window across all shops.").defineInRange("villagerPurchases", d.villagerPurchases(), 0, 4);
        INTERVAL = b.defineInRange("visitInterval", d.visitInterval(), 200, 24_000);
        DURATION = b.defineInRange("visitDuration", d.visitDuration(), 40, 600);
        CONCURRENT = b.defineInRange("concurrentVisits", d.concurrentVisits(), 1, 32);
        b.pop();
        SPEC = b.build();
    }

    private ShopServerConfig() {
    }

    public static ShopRules current() {
        return current;
    }

    public static long revision() {
        return revision;
    }

    public static void listen(Runnable listener) {
        LISTENERS.add(listener);
    }

    public static void register(IEventBus bus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "herbalwitch-commerce-server.toml");
        bus.addListener(ShopServerConfig::changed);
    }

    /** Installs a validated snapshot on the server thread (also the GameTest seam for live changes). */
    public static void install(ShopRules next) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !server.isSameThread()) throw new IllegalStateException("Commerce rules must install on the server thread");
        if (next.equals(current)) return;
        current = Objects.requireNonNull(next);
        revision++;
        LISTENERS.forEach(Runnable::run);
        io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent.invalidate();
    }

    private static void changed(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) return;
        try {
            ShopRules next = new ShopRules(SHOPS_PER_OWNER.get(), SHOPS_PER_WORLD.get(), PROCEEDS.get(), NPC.get(), WINDOW.get(), WORLD.get(),
                    OWNER.get(), SHOP.get(), VILLAGER.get(), INTERVAL.get(), DURATION.get(), CONCURRENT.get(), LIFETIME.get());
            Runnable apply = () -> install(next);
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server == null || server.isSameThread()) apply.run();
            else server.execute(apply);
        } catch (RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(ShopServerConfig.class).error("Invalid commerce configuration; retaining previous rules", failure);
        }
    }
}
