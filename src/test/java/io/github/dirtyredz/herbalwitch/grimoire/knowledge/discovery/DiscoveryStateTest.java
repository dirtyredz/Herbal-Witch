package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Personal knowledge: idempotent grants, typed keys that never collide, the ceiling and read-only states. */
class DiscoveryStateTest {
    private static final EntryKey TEA = EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/steeping/recovery_tea");

    @Test
    void grantingTwiceIsANoOp() {
        DiscoveryState state = new DiscoveryState();
        assertEquals(DiscoveryState.Grant.ADDED, state.grant(TEA));
        assertEquals(DiscoveryState.Grant.ALREADY_KNOWN, state.grant(TEA));
        assertEquals(1, state.size());
        assertTrue(state.knows(TEA));
    }

    @Test
    void theSameIdOfAnotherKindIsAnotherDiscovery() {
        DiscoveryState state = new DiscoveryState();
        EntryKey herb = EntryKey.of(EntryKey.Kind.HERB, "herbalwitch:botany/temperate/lavender/plant");
        EntryKey recipe = EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:botany/temperate/lavender/plant");
        state.grant(herb);
        assertFalse(state.knows(recipe));
        state.grant(recipe);
        assertEquals(2, state.size());
        assertEquals(List.of(herb, recipe), List.copyOf(state.known()), "sorted by kind, then id");
    }

    @Test
    void keysRoundTripAndRejectMalformedText() {
        assertEquals(TEA, EntryKey.parse(TEA.serialized()).orElseThrow());
        for (String bad : List.of("", "recipe", "recipe|", "|herbalwitch:x", "spell|herbalwitch:x", "recipe|Herbal:x", "recipe|nonamespace")) {
            assertTrue(EntryKey.parse(bad).isEmpty(), bad);
        }
        assertThrows(IllegalArgumentException.class, () -> EntryKey.of(EntryKey.Kind.HERB, "UPPER:case"));
    }

    @Test
    void theCeilingRefusesInsteadOfTruncating() {
        DiscoveryState state = new DiscoveryState();
        for (int i = 0; i < DiscoveryState.MAX_DISCOVERIES; i++) {
            assertEquals(DiscoveryState.Grant.ADDED, state.grant(EntryKey.of(EntryKey.Kind.RECIPE, "test:r" + i)));
        }
        assertEquals(DiscoveryState.Grant.FULL, state.grant(TEA));
        assertEquals(DiscoveryState.MAX_DISCOVERIES, state.size());
        assertTrue(state.knows(EntryKey.of(EntryKey.Kind.RECIPE, "test:r0")), "nothing was dropped to make room");
    }

    @Test
    void aReadOnlyStateRecordsNothingAndCopiesStayIndependent() {
        DiscoveryState locked = DiscoveryState.readOnly();
        assertEquals(DiscoveryState.Grant.READ_ONLY, locked.grant(TEA));
        assertTrue(locked.copy().isReadOnly());
        DiscoveryState original = new DiscoveryState();
        original.grant(TEA);
        DiscoveryState copy = original.copy();
        copy.grant(EntryKey.of(EntryKey.Kind.HERB, "herbalwitch:botany/wet/mint/plant"));
        assertEquals(1, original.size());
        assertEquals(2, copy.size());
    }
}
