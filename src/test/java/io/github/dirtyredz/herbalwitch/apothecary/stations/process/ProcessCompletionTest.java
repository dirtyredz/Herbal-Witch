package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessCompletion.Amount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Capacity simulation before a completion commits. Pure JUnit. */
class ProcessCompletionTest {
    private static final Amount EMPTY = Amount.EMPTY;

    @Test
    void resultsMergeOnlyIntoEmptyOrSameRoomySlots() {
        assertTrue(ProcessCompletion.fits(EMPTY, new Amount("tea", 1, 16)));
        assertTrue(ProcessCompletion.fits(new Amount("tea", 15, 16), new Amount("tea", 1, 16)));
        assertFalse(ProcessCompletion.fits(new Amount("tea", 16, 16), new Amount("tea", 1, 16)), "full");
        assertFalse(ProcessCompletion.fits(new Amount("tea", 1, 16), new Amount("tea{Purity:1}", 1, 16)), "different data never stacks");
        assertTrue(ProcessCompletion.fits(new Amount("x", 3, 64), EMPTY), "nothing always fits");
    }

    @Test
    void returnsMustAllFitTheirOneSlot() {
        Amount bottle = new Amount("glass_bottle", 1, 64);
        assertTrue(ProcessCompletion.fitsAll(EMPTY, List.of(bottle, bottle)));
        assertEquals(List.of(new Amount("glass_bottle", 2, 64)), ProcessCompletion.merge(List.of(bottle, bottle)));
        assertFalse(ProcessCompletion.fitsAll(EMPTY, List.of(bottle, new Amount("bowl", 1, 64))), "two kinds need two slots");
        assertFalse(ProcessCompletion.fitsAll(new Amount("glass_bottle", 63, 64), List.of(bottle, bottle)));
    }

    @Test
    void completionNeedsBothOutputAndReturnRoom() {
        Amount salve = new Amount("salve", 1, 16);
        Amount bottle = new Amount("glass_bottle", 1, 64);
        assertTrue(ProcessCompletion.canComplete(EMPTY, salve, EMPTY, List.of(bottle)));
        assertFalse(ProcessCompletion.canComplete(new Amount("salve", 16, 16), salve, EMPTY, List.of(bottle)), "output full");
        assertFalse(ProcessCompletion.canComplete(EMPTY, salve, new Amount("bowl", 1, 64), List.of(bottle)), "return slot taken");
    }
}
