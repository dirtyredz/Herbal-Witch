package io.github.dirtyredz.herbalwitch.brewing.product.metadata;

import net.minecraft.world.item.ItemStack;

/**
 * Water-to-drink metadata: what a drink inherits from the water it was steeped in. The default
 * carries nothing; an optional integration (Thirst Was Taken's purity) installs its own carrier.
 * No foreign types appear here.
 */
public final class DrinkMetadata {
    /** Copies whatever the drink should keep from its water. */
    @FunctionalInterface
    public interface Carrier {
        void carry(ItemStack water, ItemStack drink);
    }

    private static volatile Carrier carrier = (water, drink) -> {
    };

    private DrinkMetadata() {
    }

    public static void install(Carrier installed) {
        carrier = installed;
    }

    /** The drink with its water's metadata; never modifies {@code water}. */
    public static ItemStack carry(ItemStack water, ItemStack drink) {
        ItemStack out = drink.copy();
        carrier.carry(water, out);
        return out;
    }
}
