package io.github.dirtyredz.herbalwitch.magic.witch.config;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The server configuration of initiation and energy ({@code herbalwitch-server.toml}, per world).
 * Each value has its own range; the whole set is then validated together (thresholds strictly
 * increasing, final capacity bounded) and installed as {@link WitchRules} only when valid, on the
 * server thread. An invalid edit keeps the previous rules and is logged.
 */
public final class WitchServerConfig {
    public static final String FILE = "herbalwitch-server.toml";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue FOUNDING_ENABLED;
    private static final ForgeConfigSpec.BooleanValue INITIATION_ENABLED;
    private static final ForgeConfigSpec.IntValue FOUNDING_TICKS;
    private static final ForgeConfigSpec.IntValue INITIATION_TICKS;
    private static final ForgeConfigSpec.IntValue ATTUNEMENT_TICKS;
    private static final ForgeConfigSpec.IntValue INVITE_EXPIRY;
    private static final ForgeConfigSpec.IntValue INVITE_COOLDOWN;
    private static final ForgeConfigSpec.IntValue INITIAL_CAPACITY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> THRESHOLDS;
    private static final ForgeConfigSpec.IntValue CAPACITY_PER_THRESHOLD;
    private static final ForgeConfigSpec.IntValue INITIATION_COST;
    private static final ForgeConfigSpec.IntValue ATTUNEMENT_COST;
    private static final ForgeConfigSpec.IntValue REST_INTERVAL;
    private static final ForgeConfigSpec.IntValue REST_AMOUNT;
    private static final ForgeConfigSpec.IntValue MEDITATION_SETTLE;
    private static final ForgeConfigSpec.IntValue MEDITATION_INTERVAL;
    private static final ForgeConfigSpec.IntValue MEDITATION_AMOUNT;
    private static final ForgeConfigSpec.IntValue SLEEP_AMOUNT;
    private static final ForgeConfigSpec.IntValue TEA_AMOUNT;
    private static final ForgeConfigSpec.IntValue TEA_COOLDOWN;
    private static final ForgeConfigSpec.IntValue RESTFUL_BONUS;
    private static final ForgeConfigSpec.IntValue RESTFUL_DURATION;
    private static final ForgeConfigSpec.IntValue NATURE_BONUS;
    private static final ForgeConfigSpec.IntValue MOON_BONUS;
    private static final ForgeConfigSpec.IntValue BONUS_CAP;

    static {
        WitchRules d = WitchRules.DEFAULTS;
        EnergyBalance e = d.energy();
        int maxE = EnergyBalance.MAX_ENERGY;
        int maxT = EnergyBalance.MAX_TICKS;
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Becoming a witch: founding the first lineage and initiating others.").push("initiation");
        FOUNDING_ENABLED = b.comment("Whether a survival player may found the first lineage at an altar.").define("foundingEnabled", d.foundingEnabled());
        INITIATION_ENABLED = b.comment("Whether witches may initiate consenting players.").define("initiationEnabled", d.initiationEnabled());
        FOUNDING_TICKS = b.comment("Ticks the founding ritual lasts.").defineInRange("foundingTicks", d.foundingTicks(), 1, maxT);
        INITIATION_TICKS = b.comment("Ticks an initiation lasts.").defineInRange("initiationTicks", d.initiationTicks(), 1, maxT);
        INVITE_EXPIRY = b.comment("Ticks an invitation stays open.").defineInRange("inviteExpiry", d.inviteExpiry(), 1, maxT);
        INVITE_COOLDOWN = b.comment("Ticks between one witch's invitations.").defineInRange("inviteCooldown", d.inviteCooldown(), 0, maxT);
        INITIATION_COST = b.comment("Energy the initiating witch pays.").defineInRange("initiationCost", e.initiationCost(), 0, maxE);
        b.pop();
        b.comment("Energy capacity and practice.").push("capacity");
        INITIAL_CAPACITY = b.comment("Capacity of a new witch.").defineInRange("initialCapacity", e.initialCapacity(), 1, maxE);
        THRESHOLDS = b.comment("Successfully spent energy at which capacity grows (strictly increasing).")
                .defineList("practiceThresholds", e.thresholds(), o -> o instanceof Integer i && i > 0 && i <= maxE);
        CAPACITY_PER_THRESHOLD = b.comment("Capacity gained at each threshold.").defineInRange("capacityPerThreshold", e.capacityPerThreshold(), 0, maxE);
        ATTUNEMENT_COST = b.comment("Energy an attunement spends.").defineInRange("attunementCost", e.attunementCost(), 0, maxE);
        ATTUNEMENT_TICKS = b.comment("Ticks an attunement lasts.").defineInRange("attunementTicks", d.attunementTicks(), 1, maxT);
        b.pop();
        b.comment("Recovering energy.").push("recovery");
        REST_INTERVAL = b.comment("Ticks of still rest per passive grant.").defineInRange("restInterval", e.restInterval(), 1, maxT);
        REST_AMOUNT = b.comment("Energy per passive grant.").defineInRange("restAmount", e.restAmount(), 0, maxE);
        MEDITATION_SETTLE = b.comment("Ticks of settling before meditation grants.").defineInRange("meditationSettle", e.meditationSettle(), 0, maxT);
        MEDITATION_INTERVAL = b.comment("Ticks per meditation grant.").defineInRange("meditationInterval", e.meditationInterval(), 1, maxT);
        MEDITATION_AMOUNT = b.comment("Energy per meditation grant.").defineInRange("meditationAmount", e.meditationAmount(), 0, maxE);
        SLEEP_AMOUNT = b.comment("Energy for a completed night's sleep, once per day.").defineInRange("sleepAmount", e.sleepAmount(), 0, maxE);
        TEA_AMOUNT = b.comment("Energy from a cup of energy tea.").defineInRange("teaAmount", e.teaAmount(), 0, maxE);
        TEA_COOLDOWN = b.comment("Ticks before the next cup of energy tea.").defineInRange("teaCooldown", e.teaCooldown(), 0, maxT);
        RESTFUL_BONUS = b.comment("Extra energy a restful infusion prepares for the next rewarded sleep.").defineInRange("restfulBonus", e.restfulBonus(), 0, maxE);
        RESTFUL_DURATION = b.comment("Online ticks a restful preparation lasts.").defineInRange("restfulDuration", e.restfulDuration(), 1, maxT * 10);
        NATURE_BONUS = b.comment("Percent added to rest and meditation in nature biomes.").defineInRange("natureBonus", e.natureBonus(), 0, 100);
        MOON_BONUS = b.comment("Percent added under a visible full moon at night.").defineInRange("moonBonus", e.moonBonus(), 0, 100);
        BONUS_CAP = b.comment("Combined bonus ceiling, percent.").defineInRange("bonusCap", e.bonusCap(), 0, 100);
        b.pop();
        SPEC = b.build();
    }

    private WitchServerConfig() {
    }

    /** The configured values as rules (not yet validated as a whole). */
    public static WitchRules read() {
        List<Integer> thresholds = new ArrayList<>();
        THRESHOLDS.get().forEach(thresholds::add);
        EnergyBalance energy = new EnergyBalance(INITIAL_CAPACITY.get(), thresholds, CAPACITY_PER_THRESHOLD.get(), INITIATION_COST.get(),
                ATTUNEMENT_COST.get(), REST_INTERVAL.get(), REST_AMOUNT.get(), MEDITATION_SETTLE.get(), MEDITATION_INTERVAL.get(),
                MEDITATION_AMOUNT.get(), SLEEP_AMOUNT.get(), TEA_AMOUNT.get(), TEA_COOLDOWN.get(), RESTFUL_BONUS.get(),
                RESTFUL_DURATION.get(), NATURE_BONUS.get(), MOON_BONUS.get(), BONUS_CAP.get());
        return new WitchRules(FOUNDING_ENABLED.get(), INITIATION_ENABLED.get(), FOUNDING_TICKS.get(), INITIATION_TICKS.get(),
                ATTUNEMENT_TICKS.get(), INVITE_EXPIRY.get(), INVITE_COOLDOWN.get(), energy);
    }

    /** Mod bus: the server config loaded or changed on disk. Applied on the server thread. */
    public static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Runnable apply = WitchServerConfig::apply;
        if (server != null) {
            server.execute(apply);
        } else {
            apply.run();
        }
    }

    private static void apply() {
        List<String> problems = WitchRules.applyServer(read());
        if (!problems.isEmpty()) {
            LOGGER.error("Ignored invalid witch configuration in {}; keeping the previous settings: {}", FILE, problems);
        }
    }

    /** Called once from the feature's registration, during mod construction. */
    public static void register(net.minecraftforge.eventbus.api.IEventBus modBus) {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, FILE);
        modBus.addListener(WitchServerConfig::onConfig);
    }
}
