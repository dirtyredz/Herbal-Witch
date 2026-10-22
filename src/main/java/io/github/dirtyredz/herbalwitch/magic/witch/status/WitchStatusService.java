package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The server's one way to read and change a player's witch status and energy, and to keep that
 * player's client informed. Every change comes from trusted server code with server-derived amounts
 * (a ritual's completion, a drink, a night's sleep); there is no setter a packet can reach. Changes
 * are sent privately, at most every {@link #SYNC_INTERVAL} ticks, at once for conversions and ritual
 * results. Unreadable saved state is never changed. Other features (rituals, later production and
 * familiars) spend and award through here instead of keeping their own energy.
 */
public final class WitchStatusService {
    public static final int SYNC_INTERVAL = 5;
    private static final List<Consumer<ServerPlayer>> CONVERSIONS = new CopyOnWriteArrayList<>();

    private WitchStatusService() {
    }

    public static Optional<WitchCapability> of(Player player) {
        return WitchCapability.of(player);
    }

    public static boolean isWitch(Player player) {
        return of(player).map(c -> c.state().initiated()).orElse(false);
    }

    private static EnergyBalance balance() {
        return WitchRules.server().energy();
    }

    public static int energyUnits(Player player) {
        return of(player).map(c -> c.state().energyUnits()).orElse(0);
    }

    public static int capacityUnits(Player player) {
        return of(player).map(c -> EnergyService.capacityUnits(c.state(), balance())).orElse(0);
    }

    /** Whether a writable witch can pay {@code units} now; mutates nothing. */
    public static EnergyService.Result check(Player player, int units) {
        Optional<WitchCapability> cap = of(player);
        if (cap.isEmpty() || !cap.get().writable()) {
            return EnergyService.Result.NOT_WITCH;
        }
        return EnergyService.check(cap.get().state(), units);
    }

    /** Spends {@code units}; nothing changes unless OK. Spending breaks passive rest. */
    public static EnergyService.Result debit(ServerPlayer player, int units) {
        Optional<WitchCapability> cap = of(player);
        if (cap.isEmpty() || !cap.get().writable()) {
            return EnergyService.Result.NOT_WITCH;
        }
        EnergyService.Result result = EnergyService.debit(cap.get().state(), units);
        if (result == EnergyService.Result.OK && units > 0) {
            cap.get().disturb();
            cap.get().changed();
        }
        return result;
    }

    /** Restores up to {@code units} (clamped to capacity); returns what was added. */
    public static int recover(ServerPlayer player, int units) {
        Optional<WitchCapability> cap = of(player);
        if (cap.isEmpty() || !cap.get().writable()) {
            return 0;
        }
        int added = EnergyService.recover(cap.get().state(), balance(), units);
        if (added > 0) {
            cap.get().changed();
        }
        return added;
    }

    /** Records successful expenditure after a completed operation; returns thresholds newly crossed. */
    public static int awardPractice(ServerPlayer player, int units) {
        Optional<WitchCapability> cap = of(player);
        if (cap.isEmpty() || !cap.get().writable()) {
            return 0;
        }
        int crossed = EnergyService.awardPractice(cap.get().state(), balance(), units);
        cap.get().changed();
        return crossed;
    }

    /** A copy of the durable state, for an operation's rollback. */
    public static Optional<WitchState> snapshot(Player player) {
        return of(player).map(c -> c.state().copy());
    }

    /** Puts back a snapshot taken by {@link #snapshot} (a synchronous failure after mutation). */
    public static void restore(ServerPlayer player, WitchState snapshot) {
        of(player).filter(WitchCapability::writable).ifPresent(c -> {
            c.state().restoreFrom(snapshot);
            c.changed(true);
        });
    }

    /**
     * Completes a conversion: the player becomes a witch with {@code initiationId} and full initial
     * energy. Only conversion operations call this, after their own checks; false (nothing changed)
     * for an existing witch or unreadable state. Conversion listeners (founding closure) run after.
     */
    public static boolean convert(ServerPlayer player, UUID initiationId) {
        Optional<WitchCapability> cap = of(player);
        if (cap.isEmpty() || !cap.get().writable() || cap.get().state().initiated()) {
            return false;
        }
        EnergyService.initiate(cap.get().state(), balance(), initiationId);
        cap.get().changed(true);
        flush(player);
        CONVERSIONS.forEach(l -> l.accept(player));
        return true;
    }

    /** Hears every completed conversion (registered at bootstrap). */
    public static void onConversion(Consumer<ServerPlayer> listener) {
        CONVERSIONS.add(listener);
    }

    /** Applies new rules to an online player: capacity is recomputed and energy clamped, never refilled. */
    public static void rulesChanged(ServerPlayer player) {
        of(player).filter(WitchCapability::writable).ifPresent(c -> {
            EnergyService.clamp(c.state(), balance());
            c.changed(true);
        });
        sendRules(player);
    }

    /** Starts the saved tea cooldown for everyone and recovers energy only for witches. */
    public static int energyTea(ServerPlayer player) {
        of(player).filter(WitchCapability::writable).ifPresent(c -> c.state().setTeaCooldownTicks(balance().teaCooldown()));
        return recover(player, EnergyService.units(balance().teaAmount()));
    }

    /** Refreshes, rather than stacks, a witch's preparation for the next rewarded sleep. */
    public static boolean restfulInfusion(ServerPlayer player) {
        return of(player).filter(c -> c.writable() && c.state().initiated()).map(c -> {
            c.state().setRestfulTicks(balance().restfulDuration());
            c.changed();
            return true;
        }).orElse(false);
    }

    /** Online countdowns run before rest eligibility and preserve the existing expiry notification. */
    public static void tickCountdowns(ServerPlayer player) {
        of(player).filter(c -> c.writable() && player.getServer() != null).ifPresent(c -> {
            WitchState state = c.state();
            if (state.teaCooldownTicks() > 0) {
                state.setTeaCooldownTicks(state.teaCooldownTicks() - 1);
            }
            if (state.restfulTicks() > 0) {
                state.setRestfulTicks(state.restfulTicks() - 1);
                if (state.restfulTicks() == 0) { c.changed(); }
            }
        });
    }

    public static SleepRecovery.Outcome completedSleep(ServerPlayer player, long newDayTime, boolean sleptLongEnough) {
        Optional<WitchCapability> cap = of(player);
        if (cap.isEmpty() || !cap.get().writable()) { return SleepRecovery.Outcome.NONE; }
        SleepRecovery.Outcome outcome = SleepRecovery.completed(cap.get().state(), balance(), newDayTime, sleptLongEnough);
        if (outcome.rewarded()) { cap.get().changed(true); }
        return outcome;
    }

    /** Login, respawn or dimension change: a new epoch and a full snapshot. */
    public static void resync(ServerPlayer player) { WitchStatusSync.resync(player); }
    public static void flush(ServerPlayer player) { WitchStatusSync.flush(player); }
    public static void tick(ServerPlayer player) { WitchStatusSync.tick(player); }
    public static void sendRules(ServerPlayer player) { WitchStatusSync.sendRules(player); }
}
