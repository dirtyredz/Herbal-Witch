package io.github.dirtyredz.herbalwitch.apothecary.brewing.application;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Exactly-once container handling after a successful use: one item is used up and one container
 * comes back (into the hand if the stack is gone, else the inventory, else dropped). Creative
 * players keep the item and get no container, like vanilla potions.
 */
public final class ContainerReturns {
    private ContainerReturns() {
    }

    /** Returns what the user should now hold in the used hand. */
    public static ItemStack finish(ItemStack used, LivingEntity user, ItemStack container) {
        if (user instanceof Player player && player.getAbilities().instabuild) {
            return used;
        }
        used.shrink(1);
        if (container.isEmpty()) {
            return used;
        }
        if (used.isEmpty()) {
            return container.copy();
        }
        if (user instanceof Player player && !player.getInventory().add(container.copy())) {
            player.drop(container.copy(), false);
        }
        return used;
    }
}
