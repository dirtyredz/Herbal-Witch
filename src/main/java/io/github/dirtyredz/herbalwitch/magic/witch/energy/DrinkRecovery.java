package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.level.ServerPlayer;

/**
 * What the two restorative drinks add for witches, called by the drinks themselves once, on the
 * server, alongside their ordinary effects:
 * <ul>
 *   <li>energy tea: {@code teaAmount} energy for a witch, and for everyone an authoritative cooldown
 *       saved with the player (a relog cannot clear it; the vanilla item cooldown only mirrors it);</li>
 *   <li>restful infusion: for a witch, a non-stacking preparation of the next rewarded sleep
 *       (re-drinking refreshes its duration, never its size).</li>
 * </ul>
 */
public final class DrinkRecovery {
    private DrinkRecovery() {
    }

    private static EnergyBalance balance() {
        return WitchRules.server().energy();
    }

    /** Remaining authoritative energy-tea cooldown in ticks (0 when a cup would work now). */
    public static int teaCooldown(ServerPlayer player) {
        return WitchCapability.of(player).map(c -> c.state().teaCooldownTicks()).orElse(0);
    }

    /** The configured cooldown a cup starts. */
    public static int teaCooldownLength() {
        return balance().teaCooldown();
    }

    /** A cup of energy tea was drunk: starts the cooldown; returns the energy units a witch gained. */
    public static int energyTea(ServerPlayer player) {
        WitchCapability.of(player).filter(WitchCapability::writable).ifPresent(c -> c.state().setTeaCooldownTicks(balance().teaCooldown()));
        return WitchStatusService.recover(player, EnergyService.units(balance().teaAmount()));
    }

    /** A restful infusion was drunk: a witch's next rewarded sleep is prepared (refreshing, never stacking). */
    public static boolean restfulInfusion(ServerPlayer player) {
        return WitchCapability.of(player).filter(c -> c.writable() && c.state().initiated()).map(c -> {
            c.state().setRestfulTicks(balance().restfulDuration());
            c.changed();
            return true;
        }).orElse(false);
    }
}
