package io.github.dirtyredz.herbalwitch.magic.witch.command;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.PracticeProgress;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationConsent;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingRecord;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingSavedData;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * What the operator commands do (registration lives in {@link WitchCommandRegistration}): inspect a
 * player's status, energy, practice and recovery gates; designate an online non-witch, who must still
 * accept at an altar; inspect the founding record; close an interrupted founding claim without
 * replaying anything. Each returns a command result (1 success, 0 refused).
 */
public final class WitchCommand {
    public static final String KEY = "commands.herbalwitch.witch.";

    private WitchCommand() {
    }

    public static int inspect(CommandSourceStack source, ServerPlayer player) {
        Optional<WitchCapability> cap = WitchCapability.of(player);
        if (cap.isEmpty()) {
            source.sendFailure(Component.translatable(KEY + "no_status", player.getDisplayName()));
            return 0;
        }
        WitchState s = cap.get().state();
        EnergyBalance balance = WitchRules.server().energy();
        int capacity = EnergyService.capacityUnits(s, balance) / EnergyBalance.UNITS;
        Component status = Component.translatable(KEY + (s.initiated() ? "status.witch" : "status.not_witch"));
        source.sendSuccess(() -> Component.translatable(KEY + "inspect", player.getDisplayName(), status,
                s.energyUnits() / EnergyBalance.UNITS, capacity, s.practiceSpent() / EnergyBalance.UNITS,
                PracticeProgress.next(balance, s.practiceSpent())), false);
        source.sendSuccess(() -> Component.translatable(KEY + "inspect.gates", s.teaCooldownTicks(), s.restfulTicks(),
                s.lastSleepRewardDay()), false);
        cap.get().problem().ifPresent(p -> source.sendSuccess(() -> Component.translatable(KEY + "inspect.unreadable", p), false));
        return 1;
    }

    public static int designate(CommandSourceStack source, ServerPlayer player) {
        InitiationConsent.Result result = InitiationEvents.designate(player);
        if (result == InitiationConsent.Result.DESIGNATED) {
            source.sendSuccess(() -> Component.translatable(KEY + "designated", player.getDisplayName()), true);
            return 1;
        }
        source.sendFailure(Component.translatable(result.messageKey()));
        return 0;
    }

    public static int foundingInspect(CommandSourceStack source) {
        FoundingSavedData data = FoundingService.data(source.getServer());
        FoundingRecord r = data.record();
        String claim = r.claim().map(UUID::toString).orElse("-");
        String candidate = r.candidate().map(UUID::toString).orElse("-");
        String site = r.site().map(s -> s.dimension() + " " + net.minecraft.core.BlockPos.of(s.origin()).toShortString()).orElse("-");
        source.sendSuccess(() -> Component.translatable(KEY + "founding.inspect", r.state().name().toLowerCase(java.util.Locale.ROOT),
                claim, candidate, site), false);
        data.problem().ifPresent(p -> source.sendSuccess(() -> Component.translatable(KEY + "inspect.unreadable", p), false));
        return 1;
    }

    public static int foundingResolve(CommandSourceStack source, UUID claim) {
        if (FoundingService.resolveClose(source.getServer(), claim)) {
            source.sendSuccess(() -> Component.translatable(KEY + "founding.closed", claim.toString()), true);
            return 1;
        }
        source.sendFailure(Component.translatable(KEY + "founding.not_review", claim.toString()));
        return 0;
    }
}
