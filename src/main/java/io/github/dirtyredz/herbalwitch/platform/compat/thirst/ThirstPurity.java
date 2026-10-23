package io.github.dirtyredz.herbalwitch.platform.compat.thirst;

import net.minecraft.world.item.ItemStack;

/**
 * Thirst stores water purity as an int {@code Purity} tag on the stack (WaterPurity.getPurity in
 * 1.4.0). A tea keeps exactly its water's purity: heating does not upgrade it. Water without the
 * tag passes nothing on, so Thirst then applies its configured default to the drink as it would
 * to the water.
 */
public final class ThirstPurity {
    public static final String TAG = "Purity";
    /** Thirst 1.4.0's purified (pristine) level, the only purity villager customers accept. */
    public static final int PRISTINE = 3;

    private ThirstPurity() {
    }

    public static void carry(ItemStack water, ItemStack drink) {
        if (water.hasTag() && water.getTag().contains(TAG)) {
            drink.getOrCreateTag().putInt(TAG, water.getTag().getInt(TAG));
        }
    }
}
