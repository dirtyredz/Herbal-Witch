package io.github.dirtyredz.herbalwitch.client.magic.witch;

import io.github.dirtyredz.herbalwitch.client.magic.witch.hud.EnergyHudLayout;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Presentation settings of the energy HUD ({@code herbalwitch-client.toml}), client only: shown or
 * not, a nine-way anchor, offsets inward from that anchor, scale, the number line, and the recovery
 * indicator. Defaults put it at the bottom left, eight GUI pixels from the left edge and above the
 * chat input. Nothing here affects mechanics.
 */
public final class WitchClientConfig {
    public static final String FILE = "herbalwitch-client.toml";
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<EnergyHudLayout.Anchor> ANCHOR;
    public static final ForgeConfigSpec.IntValue OFFSET_X;
    public static final ForgeConfigSpec.IntValue OFFSET_Y;
    public static final ForgeConfigSpec.DoubleValue SCALE;
    public static final ForgeConfigSpec.BooleanValue NUMERIC;
    public static final ForgeConfigSpec.BooleanValue RECOVERY_INDICATOR;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("The witch energy HUD.").push("hud");
        ENABLED = b.comment("Show the energy HUD (witches only).").define("enabled", true);
        ANCHOR = b.comment("Screen anchor.").defineEnum("anchor", EnergyHudLayout.Anchor.BOTTOM_LEFT);
        OFFSET_X = b.comment("GUI pixels inward from the anchor's side edge.").defineInRange("offsetX", 8, -4096, 4096);
        OFFSET_Y = b.comment("GUI pixels inward from the anchor's top or bottom edge (the default clears the chat input).")
                .defineInRange("offsetY", 18, -4096, 4096);
        SCALE = b.comment("Scale of the HUD.").defineInRange("scale", 1.0, 0.25, 4.0);
        NUMERIC = b.comment("Show energy as numbers above the bar.").define("numeric", true);
        RECOVERY_INDICATOR = b.comment("Show a small icon while rest or meditation restores energy.").define("recoveryIndicator", true);
        b.pop();
        SPEC = b.build();
    }

    private WitchClientConfig() {
    }

    /** Registers the client config during mod construction (client only). */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, FILE);
    }
}
