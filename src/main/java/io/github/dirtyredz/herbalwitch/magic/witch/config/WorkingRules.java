package io.github.dirtyredz.herbalwitch.magic.witch.config;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;

import java.util.ArrayList;
import java.util.List;

/**
 * The validated numbers of a witch's workings (Milestone 7): what magical processing, the circle's
 * rituals, wards and charms cost or last. Part of {@link WitchRules}, so they are configured in the
 * same file, validated with it as a whole, synced to clients (the grimoire and viewers quote them),
 * fingerprinted with it, and an accepted change cancels rituals and invalidates authorised batches
 * whose cost changed. Plain Java; effect durations and charm strengths are fixed in code.
 *
 * @param processingPercent   multiplier applied to every magical recipe's energy, percent
 * @param gardenBlessingCost  energy the garden blessing spends
 * @param gardenBlessingTicks loaded ticks the garden blessing lasts
 * @param homeProtectionCost  energy the home protection ritual spends
 * @param homeProtectionTicks loaded ticks the home protection ritual lasts
 * @param wardActivationCost  energy a direct ward activation spends
 * @param wardChargeSeconds   loaded seconds of protection one activation grants
 * @param wardRadius          horizontal ward radius without a focus
 * @param wardFocusRadius     horizontal radius with a hearth or stove focus
 * @param wardHeight          vertical reach above and below the stone
 * @param wardsPerOwner       bound wards one witch may hold
 * @param wardsPerDimension   loaded active wards one dimension may hold
 * @param charmChargeSeconds  equipped seconds a fresh charm lasts
 * @param preparationPvp      whether harmful preparations may affect other players (server PvP and teams still apply)
 */
public record WorkingRules(int processingPercent, int gardenBlessingCost, int gardenBlessingTicks, int homeProtectionCost,
                           int homeProtectionTicks, int wardActivationCost, int wardChargeSeconds, int wardRadius, int wardFocusRadius,
                           int wardHeight, int wardsPerOwner, int wardsPerDimension, int charmChargeSeconds, boolean preparationPvp) {
    /** Hard ceilings the configuration may not exceed (docs/catalogue: ward bounds). */
    public static final int MAX_PERCENT = 1000;
    public static final int MAX_WARD_RADIUS = 16;
    public static final int MAX_WARD_HEIGHT = 8;
    public static final int MAX_WARDS_PER_OWNER = 16;
    public static final int MAX_WARDS_PER_DIMENSION = 512;
    public static final int MAX_SECONDS = 86_400;

    /** The milestone defaults (docs/catalogue/products.md, witchcraft.md). */
    public static final WorkingRules DEFAULTS = new WorkingRules(100, 15, 200, 20, 200, 20, 600, 8, 12, 4, 4, 256, 300, false);

    /** Why these numbers are unusable; empty when valid. */
    public List<String> problems() {
        List<String> out = new ArrayList<>();
        range(out, "processingPercent", processingPercent, 0, MAX_PERCENT);
        range(out, "gardenBlessingCost", gardenBlessingCost, 0, EnergyBalance.MAX_ENERGY);
        range(out, "gardenBlessingTicks", gardenBlessingTicks, 1, EnergyBalance.MAX_TICKS);
        range(out, "homeProtectionCost", homeProtectionCost, 0, EnergyBalance.MAX_ENERGY);
        range(out, "homeProtectionTicks", homeProtectionTicks, 1, EnergyBalance.MAX_TICKS);
        range(out, "wardActivationCost", wardActivationCost, 0, EnergyBalance.MAX_ENERGY);
        range(out, "wardChargeSeconds", wardChargeSeconds, 1, MAX_SECONDS);
        range(out, "wardRadius", wardRadius, 1, MAX_WARD_RADIUS);
        range(out, "wardFocusRadius", wardFocusRadius, 1, MAX_WARD_RADIUS);
        range(out, "wardHeight", wardHeight, 1, MAX_WARD_HEIGHT);
        range(out, "wardsPerOwner", wardsPerOwner, 1, MAX_WARDS_PER_OWNER);
        range(out, "wardsPerDimension", wardsPerDimension, 1, MAX_WARDS_PER_DIMENSION);
        range(out, "charmChargeSeconds", charmChargeSeconds, 1, MAX_SECONDS);
        if (wardFocusRadius < wardRadius) {
            out.add("wardFocusRadius must be at least wardRadius");
        }
        return out;
    }

    private static void range(List<String> out, String name, int value, int min, int max) {
        if (value < min || value > max) {
            out.add(name + " " + value + " outside " + min + ".." + max);
        }
    }

    /** Every number, in wire and fingerprint order (the boolean as 0 or 1 last). */
    public int[] numbers() {
        return new int[] {processingPercent, gardenBlessingCost, gardenBlessingTicks, homeProtectionCost, homeProtectionTicks,
                wardActivationCost, wardChargeSeconds, wardRadius, wardFocusRadius, wardHeight, wardsPerOwner, wardsPerDimension,
                charmChargeSeconds, preparationPvp ? 1 : 0};
    }

    /** The inverse of {@link #numbers}. */
    public static WorkingRules of(int[] v) {
        if (v.length != 14) {
            throw new IllegalArgumentException("expected 14 numbers, got " + v.length);
        }
        return new WorkingRules(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12], v[13] != 0);
    }
}
