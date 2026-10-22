package io.github.dirtyredz.herbalwitch.stations.process.matching;

/**
 * Exact shapeless slot assignment, pure: each ingredient takes its own matching slot and every
 * occupied slot is used, so a stray item blocks a recipe and three-slot and one-slot recipes stay
 * unambiguous. Matching never consumes or reserves anything.
 */
public final class ProcessMatcher {
    private ProcessMatcher() {
    }

    /**
     * Assigns each ingredient its own matching slot so that every occupied slot is used; returns the
     * slot index per ingredient, or null. {@code matches[i][s]}: ingredient i is satisfied by slot s.
     */
    public static int[] assign(boolean[][] matches, boolean[] occupied) {
        int occupiedCount = 0;
        for (boolean o : occupied) {
            occupiedCount += o ? 1 : 0;
        }
        if (matches.length != occupiedCount) {
            return null;
        }
        int[] chosen = new int[matches.length];
        return assign(matches, occupied, new boolean[occupied.length], chosen, 0) ? chosen : null;
    }

    private static boolean assign(boolean[][] matches, boolean[] occupied, boolean[] used, int[] chosen, int i) {
        if (i == matches.length) {
            return true;
        }
        for (int s = 0; s < occupied.length; s++) {
            if (occupied[s] && !used[s] && matches[i][s]) {
                used[s] = true;
                chosen[i] = s;
                if (assign(matches, occupied, used, chosen, i + 1)) {
                    return true;
                }
                used[s] = false;
            }
        }
        return false;
    }
}
