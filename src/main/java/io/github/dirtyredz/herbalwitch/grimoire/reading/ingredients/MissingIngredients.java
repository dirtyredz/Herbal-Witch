package io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients;

import java.util.Arrays;

/**
 * The shopping-list allocator, pure and non-consuming. The carried inventory is reduced to
 * stack-compatible variants (same item and tag) with total counts; each requirement needs its count
 * from ONE variant it accepts, because a station input slot holds one kind of stack. Variants are
 * shared across every requirement (primary, base and package) without double counting, and the
 * allocation maximises first the number of fully met requirements, then the items allocated. Ties
 * resolve deterministically: requirements in order, variants in inventory order.
 */
public final class MissingIngredients {
    /** Search budget; beyond it the best allocation found so far (at least the greedy one) stands. */
    static final int MAX_NODES = 50_000;

    /**
     * The result: for each requirement the variant it draws from (or -1) and how many it gets.
     *
     * @param variant   chosen variant index per requirement, -1 when none matches or none is left
     * @param allocated items allocated per requirement (at most its count)
     * @param required  the counts the recipe asks for
     */
    public record Allocation(int[] variant, int[] allocated, int[] required) {
        public int available(int requirement) {
            return allocated[requirement];
        }

        public int missing(int requirement) {
            return required[requirement] - allocated[requirement];
        }

        /** Every requirement fully met (advisory: says nothing of slot arrangement, heat or output space). */
        public boolean complete() {
            for (int r = 0; r < required.length; r++) {
                if (missing(r) > 0) {
                    return false;
                }
            }
            return true;
        }
    }

    private final int[] required;
    private final boolean[][] matches;
    private final int[] remaining;
    private final int[] choice;
    private int[] bestChoice;
    private long bestScore = -1;
    private int nodes;

    private MissingIngredients(int[] required, boolean[][] matches, int[] variantCounts) {
        this.required = required.clone();
        this.matches = matches;
        this.remaining = variantCounts.clone();
        this.choice = new int[required.length];
    }

    /**
     * @param required      count per requirement (each at least 1)
     * @param matches       {@code matches[requirement][variant]}: the requirement accepts the variant
     * @param variantCounts total count carried of each variant
     */
    public static Allocation allocate(int[] required, boolean[][] matches, int[] variantCounts) {
        if (matches.length != required.length) {
            throw new IllegalArgumentException("one match row per requirement");
        }
        for (boolean[] row : matches) {
            if (row.length != variantCounts.length) {
                throw new IllegalArgumentException("one match column per variant");
            }
        }
        MissingIngredients search = new MissingIngredients(required, matches, variantCounts);
        search.search(0, 0, 0);
        int[] allocated = new int[required.length];
        int[] left = variantCounts.clone();
        for (int r = 0; r < required.length; r++) {
            int v = search.bestChoice[r];
            if (v >= 0) {
                allocated[r] = Math.min(required[r], left[v]);
                left[v] -= allocated[r];
            }
        }
        return new Allocation(search.bestChoice.clone(), allocated, required.clone());
    }

    /** Depth-first over requirements; score = fully met * 2^32 + items allocated. */
    private void search(int r, int met, long items) {
        nodes++;
        if (r == required.length) {
            long score = ((long) met << 32) + items;
            if (score > bestScore) {
                bestScore = score;
                bestChoice = choice.clone();
            }
            return;
        }
        long optimistic = ((long) (met + required.length - r) << 32) + items + remainingRequired(r);
        if (optimistic <= bestScore || (nodes > MAX_NODES && bestChoice != null)) {
            return;
        }
        for (int v = 0; v < remaining.length; v++) {
            if (!matches[r][v] || remaining[v] <= 0) {
                continue;
            }
            int take = Math.min(required[r], remaining[v]);
            remaining[v] -= take;
            choice[r] = v;
            search(r + 1, met + (take == required[r] ? 1 : 0), items + take);
            remaining[v] += take;
        }
        choice[r] = -1;
        search(r + 1, met, items);
    }

    private long remainingRequired(int from) {
        return Arrays.stream(required, from, required.length).asLongStream().sum();
    }
}
