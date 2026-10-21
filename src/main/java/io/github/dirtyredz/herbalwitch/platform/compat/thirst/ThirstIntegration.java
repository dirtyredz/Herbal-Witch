package io.github.dirtyredz.herbalwitch.platform.compat.thirst;

import io.github.dirtyredz.herbalwitch.brewing.product.metadata.DrinkMetadata;

/**
 * Thirst Was Taken 1.4.0 support (inspected artifact: ThirstWasTaken-1.20.1-1.4.0.jar). The kettle
 * drinks are registered through Thirst's RegisterThirstValueEvent (its ThirstHelper.addDrink is a
 * no-op in 1.4.0), so Thirst's own finished-use handler hydrates exactly once and its config
 * overrides still apply. The water's purity rides from the water bottle into the drink.
 */
public final class ThirstIntegration {
    private static volatile boolean active;

    private ThirstIntegration() {
    }

    public static void register() {
        ThirstDrinkRegistration.register();
        DrinkMetadata.install(ThirstPurity::carry);
        active = true;
    }

    /** Whether the adapter was installed (Thirst present). */
    public static boolean active() {
        return active;
    }
}
