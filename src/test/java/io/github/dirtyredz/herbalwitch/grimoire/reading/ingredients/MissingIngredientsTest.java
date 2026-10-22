package io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pure allocator: variants are stack-compatible groups (split stacks already merged into one
 * total), each requirement draws from one variant it accepts, nothing is counted twice.
 */
class MissingIngredientsTest {
    private static int[] available(MissingIngredients.Allocation a) {
        int[] out = new int[a.required().length];
        for (int r = 0; r < out.length; r++) {
            out[r] = a.available(r);
        }
        return out;
    }

    @Test
    void splitStacksCountTogetherOnceMerged() {
        // two stacks of 2 lavender bundles merged into one variant of 4
        MissingIngredients.Allocation a = MissingIngredients.allocate(new int[] {4}, new boolean[][] {{true}}, new int[] {4});
        assertTrue(a.complete());
        assertEquals(0, a.missing(0));
    }

    @Test
    void oneStackIsNeverClaimedTwice() {
        // two requirements accept the same variant of 3; together they need 4
        MissingIngredients.Allocation a = MissingIngredients.allocate(new int[] {2, 2}, new boolean[][] {{true}, {true}}, new int[] {3});
        assertArrayEquals(new int[] {2, 1}, available(a));
        assertEquals(1, a.missing(1));
        assertFalse(a.complete());
    }

    @Test
    void overlappingAlternativesAreAssignedSoBothAreMet() {
        // requirement 0 accepts A or B, requirement 1 only A; greedy A-for-0 would starve 1
        MissingIngredients.Allocation a = MissingIngredients.allocate(new int[] {2, 2},
                new boolean[][] {{true, true}, {true, false}}, new int[] {2, 2});
        assertTrue(a.complete());
        assertArrayEquals(new int[] {1, 0}, a.variant());
    }

    @Test
    void incompatibleVariantsDoNotAddUpInOneSlot() {
        // one input needs 4; the inventory has 2 of each of two accepted variants (different NBT)
        MissingIngredients.Allocation a = MissingIngredients.allocate(new int[] {4}, new boolean[][] {{true, true}}, new int[] {2, 2});
        assertEquals(2, a.available(0));
        assertEquals(2, a.missing(0));
    }

    @Test
    void baseAndPackageCompeteWithIngredientsWithoutDoubleCounting() {
        // ingredient, base and package all accept the bottle variant (2 carried) and need 1 each
        MissingIngredients.Allocation a = MissingIngredients.allocate(new int[] {1, 1, 1},
                new boolean[][] {{true}, {true}, {true}}, new int[] {2});
        assertArrayEquals(new int[] {1, 1, 0}, available(a));
    }

    @Test
    void shortagesAreReportedPerRequirement() {
        MissingIngredients.Allocation a = MissingIngredients.allocate(new int[] {2, 3}, new boolean[][] {{true, false}, {false, false}},
                new int[] {1, 5});
        assertArrayEquals(new int[] {1, 0}, available(a));
        assertEquals(1, a.missing(0));
        assertEquals(3, a.missing(1));
        assertEquals(-1, a.variant()[1]);
    }

    @Test
    void tiesResolveDeterministically() {
        boolean[][] matches = {{true, true, true}};
        MissingIngredients.Allocation first = MissingIngredients.allocate(new int[] {1}, matches, new int[] {5, 5, 5});
        for (int i = 0; i < 5; i++) {
            assertArrayEquals(first.variant(), MissingIngredients.allocate(new int[] {1}, matches, new int[] {5, 5, 5}).variant());
        }
        assertEquals(0, first.variant()[0], "the first variant in inventory order wins a tie");
    }

    @Test
    void nothingRequiredIsTriviallyComplete() {
        assertTrue(MissingIngredients.allocate(new int[0], new boolean[0][], new int[] {3}).complete());
    }
}
