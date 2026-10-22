package io.github.dirtyredz.herbalwitch.platform.compat;

import io.github.dirtyredz.herbalwitch.platform.compat.curios.CuriosCharmAdapter;
import io.github.dirtyredz.herbalwitch.platform.compat.thirst.ThirstIntegration;
import net.minecraftforge.fml.ModList;

/**
 * Loads optional server-side adapters only when their mod is present, so absent mods never
 * classload foreign types. Farmer's Delight needs no code: its heat contract is a block tag
 * (data/server/compat). Recipe viewers are client plugins discovered by the viewers themselves.
 */
public final class CompatibilityRegistration {
    public static final String THIRST = "thirst";
    public static final String FARMERS_DELIGHT = "farmersdelight";
    public static final String CURIOS = "curios";

    private CompatibilityRegistration() {
    }

    public static void register() {
        if (ModList.get().isLoaded(THIRST)) {
            ThirstIntegration.register();
        }
        if (ModList.get().isLoaded(CURIOS)) {
            CuriosCharmAdapter.register();
        }
    }
}
