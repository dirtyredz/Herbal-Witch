package io.github.dirtyredz.herbalwitch.magic.charm.equipment;

import io.github.dirtyredz.herbalwitch.brewing.effect.repellent.RepellentProfiles;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmSpec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * How much of one hit a charm removes, pure and applied once to the final damage: only blows whose
 * attacker is a mob (never a player, a fall or the environment), and for a family amulet only that
 * family's mobs: {@code percent} of the hit, at most {@code cap} health. Armour and other effects
 * have already done their part; nothing is reflected.
 */
public final class CharmProtection {
    private CharmProtection() {
    }

    /** Whether the charm guards against blows from {@code attacker}. */
    public static boolean guards(CharmSpec charm, LivingEntity attacker) {
        if (!(attacker instanceof Mob) || attacker instanceof Player) {
            return false;
        }
        if (charm.everyMob()) {
            return true;
        }
        for (RepellentProfiles profile : RepellentProfiles.values()) {
            if (profile.tag().location().getPath().equals(charm.family())) {
                return profile.matches(attacker);
            }
        }
        return false;
    }

    /** The damage left after the charm, never negative. */
    public static float reduce(CharmSpec charm, float damage) {
        if (damage <= 0) {
            return damage;
        }
        float removed = Math.min(damage * charm.percent() / 100.0F, charm.cap());
        return Math.max(0, damage - removed);
    }
}
