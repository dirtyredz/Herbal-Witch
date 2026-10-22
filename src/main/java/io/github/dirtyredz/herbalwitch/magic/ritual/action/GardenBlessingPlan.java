package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Which herb beds a garden blessing would enrich, pure: the {@value #SIZE} x {@value #SIZE} square
 * centred on the chosen bed at that bed's height (at most {@value #MAX} positions). Every position must
 * be loaded (nothing is loaded for it); beds already enriched are skipped; every bed that would change
 * must be one the witch may change; and at least one must change. The same plan is computed when the
 * ritual starts and again, as the preflight, right before it commits.
 */
public final class GardenBlessingPlan {
    public static final int SIZE = 5;
    public static final int MAX = SIZE * SIZE;

    /** Why a plan was refused, or {@link #OK}. */
    public enum Outcome {
        OK,
        UNLOADED,
        NOT_ALLOWED,
        NOTHING
    }

    /** The plan: the outcome and, when OK, the beds to enrich in a fixed order. */
    public record Plan(Outcome outcome, List<BlockPos> changes) {
        public Plan {
            changes = List.copyOf(changes);
        }

        public boolean ok() {
            return outcome == Outcome.OK;
        }
    }

    private GardenBlessingPlan() {
    }

    /** The square's positions around {@code centre}, row by row. */
    public static List<BlockPos> area(BlockPos centre) {
        List<BlockPos> out = new ArrayList<>();
        int r = SIZE / 2;
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                out.add(centre.offset(dx, 0, dz));
            }
        }
        return out;
    }

    /**
     * @param loaded     whether a position is loaded
     * @param enrichable whether a position holds an herb bed that is not yet enriched
     * @param allowed    whether the witch may change that bed
     */
    public static Plan of(BlockPos centre, Predicate<BlockPos> loaded, Predicate<BlockPos> enrichable, Predicate<BlockPos> allowed) {
        List<BlockPos> changes = new ArrayList<>();
        for (BlockPos pos : area(centre)) {
            if (!loaded.test(pos)) {
                return new Plan(Outcome.UNLOADED, List.of());
            }
            if (enrichable.test(pos)) {
                if (!allowed.test(pos)) {
                    return new Plan(Outcome.NOT_ALLOWED, List.of());
                }
                changes.add(pos.immutable());
            }
        }
        return changes.isEmpty() ? new Plan(Outcome.NOTHING, List.of()) : new Plan(Outcome.OK, changes);
    }
}
