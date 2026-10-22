package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import io.github.dirtyredz.herbalwitch.magic.witch.status.SleepRecovery;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;

import java.util.Optional;

/**
 * Energy over time on the server: the per-tick countdowns (tea cooldown, restful preparation), which
 * only advance while the player is online, passive rest for witches standing still, damage breaking
 * rest, and the daily reward of a completed night's sleep. Nothing here grants anything offline or
 * on login, and nothing reads a position outside the player's own loaded chunk.
 */
public final class EnergyEvents {
    /** The environmental bonus is evaluated at most this often per player. */
    public static final int BONUS_INTERVAL = 20;

    private EnergyEvents() {
    }

    private static EnergyBalance balance() {
        return WitchRules.server().energy();
    }

    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            tick(player);
        }
    }

    /** One online server tick of a player's energy. */
    public static void tick(ServerPlayer player) {
        Optional<WitchCapability> cap = WitchCapability.of(player);
        if (cap.isEmpty() || !cap.get().writable() || player.getServer() == null) {
            return;
        }
        WitchCapability c = cap.get();
        WitchState state = c.state();
        if (state.teaCooldownTicks() > 0) {
            state.setTeaCooldownTicks(state.teaCooldownTicks() - 1);
        }
        if (state.restfulTicks() > 0) {
            state.setRestfulTicks(state.restfulTicks() - 1);
            if (state.restfulTicks() == 0) {
                c.changed();
            }
        }
        boolean disturbed = c.takeDisturbed();
        if (!state.initiated() || c.meditating()) {
            c.rest().reset();
            return;
        }
        boolean wasResting = c.rest().resting();
        disturbed |= player.isSprinting() || player.isSwimming() || player.getAbilities().flying || player.isFallFlying()
                || player.isPassenger() || player.isUsingItem() || player.hurtTime > 0 || player.isSpectator() || !player.isAlive();
        EnergyBalance b = balance();
        if (c.rest().tick(player.getX(), player.getY(), player.getZ(), disturbed, b.restInterval())) {
            int pct = bonus(player, c);
            WitchStatusService.recover(player, EnergyRecovery.withBonus(EnergyService.units(b.restAmount()), pct));
        }
        if (wasResting != c.rest().resting()) {
            c.changed();
        }
    }

    /** The player's environmental bonus percent, cached for {@link #BONUS_INTERVAL} ticks. */
    public static int bonus(ServerPlayer player, WitchCapability cap) {
        return cap.bonusPercent(player.getServer().getTickCount(), BONUS_INTERVAL, () -> RecoveryConditions.bonusPercent(player, balance()));
    }

    /** Damage breaks rest. */
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WitchCapability.of(player).ifPresent(WitchCapability::disturb);
        }
    }

    /**
     * The server's sleep-completion path: every player of the level who slept long enough earns the
     * day's reward once (the event fires before vanilla wakes them).
     */
    public static void sleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            sleptThrough(player, event.getNewTime(), player.isSleepingLongEnough());
        }
    }

    /** One player's completed sleep (the seam GameTests drive, since fake players never tick their sleep counter). */
    public static SleepRecovery.Outcome sleptThrough(ServerPlayer player, long newDayTime, boolean sleptLongEnough) {
        Optional<WitchCapability> cap = WitchCapability.of(player);
        if (cap.isEmpty() || !cap.get().writable()) {
            return new SleepRecovery.Outcome(false, 0, false);
        }
        SleepRecovery.Outcome outcome = SleepRecovery.completed(cap.get().state(), balance(), newDayTime, sleptLongEnough);
        if (outcome.rewarded()) {
            cap.get().changed(true);
        }
        return outcome;
    }
}
