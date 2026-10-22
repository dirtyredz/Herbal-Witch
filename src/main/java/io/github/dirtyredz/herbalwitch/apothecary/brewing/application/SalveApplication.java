package io.github.dirtyredz.herbalwitch.apothecary.brewing.application;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Healing salve on yourself: 4 health (2 hearts), then a 10 s cooldown. Only when hurt. */
public final class SalveApplication {
    public static final float HEAL = 4.0F;
    public static final int COOLDOWN = 10 * TickTime.SECOND;
    public static final int USE_TICKS = 32;

    private SalveApplication() {
    }

    public static boolean canApply(Player player) {
        return player.getHealth() < player.getMaxHealth();
    }

    public static void apply(Player player, Item salve) {
        player.heal(HEAL);
        player.getCooldowns().addCooldown(salve, COOLDOWN);
    }
}
