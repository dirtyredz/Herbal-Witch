package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleValidation;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * What the circle's rituals share. The circle must be complete when one starts; while it runs a cell in
 * an unloaded chunk pauses it (nothing is loaded for it) and the whole circle is rechecked every
 * {@link CircleCatalog#CHECK_INTERVAL} progress ticks and right before completion, a broken or
 * replaced cell ending it. The circle glows while one runs. At completion the offering and the energy
 * are taken together with the ritual's own change, all restored if any part fails synchronously.
 */
public final class CircleRituals {
    private CircleRituals() {
    }

    public static BlockPos altar(AltarSession session) {
        return BlockPos.of(session.site().origin());
    }

    /** Why the circle cannot carry a ritual now, or empty. */
    public static Optional<String> incomplete(ServerLevel level, BlockPos altar) {
        CircleValidation.Report report = CircleValidation.around(level, altar);
        if (!report.missing().isEmpty()) {
            return Optional.of("circle.incomplete");
        }
        return report.unloaded().isEmpty() ? Optional.empty() : Optional.of("circle.unloaded");
    }

    /** A running ritual's circle problem that ends it (a missing cell), checked on schedule. */
    public static Optional<String> broken(AltarSession session) {
        if (session.elapsed() % CircleCatalog.CHECK_INTERVAL != 0) {
            return Optional.empty();
        }
        return AltarSessionService.level(session.site())
                .map(l -> CircleValidation.around(l, altar(session)).missing().isEmpty() ? null : "circle_broken");
    }

    /** Whether a cell lies in an unloaded chunk (the ritual waits). */
    public static boolean paused(AltarSession session) {
        return AltarSessionService.level(session.site())
                .map(l -> !CircleValidation.around(l, altar(session)).unloaded().isEmpty()).orElse(false);
    }

    public static void glow(AltarSession session, boolean on) {
        AltarSessionService.level(session.site()).ifPresent(l -> RitualCircleBlock.setActive(l, altar(session), on));
    }

    /**
     * Takes the offering and {@code units} energy, then runs {@code change}; if the debit or the change
     * fails, the witch's inventory slots and state are put back exactly. Returns whether all of it
     * happened. Practice is awarded only after success.
     */
    public static boolean pay(ServerPlayer witch, AltarOffering offering, int units, BooleanSupplier change) {
        Map<Integer, ItemStack> slots = offering.backup(witch);
        Optional<WitchState> before = WitchStatusService.snapshot(witch);
        if (before.isEmpty()) {
            return false;
        }
        try {
            offering.consume(witch);
            if (WitchStatusService.debit(witch, units) != EnergyService.Result.OK || !change.getAsBoolean()) {
                AltarOffering.restore(witch, slots);
                WitchStatusService.restore(witch, before.get());
                return false;
            }
        } catch (RuntimeException e) {
            AltarOffering.restore(witch, slots);
            WitchStatusService.restore(witch, before.get());
            throw e;
        }
        WitchStatusService.awardPractice(witch, units);
        WitchStatusService.flush(witch);
        return true;
    }
}
