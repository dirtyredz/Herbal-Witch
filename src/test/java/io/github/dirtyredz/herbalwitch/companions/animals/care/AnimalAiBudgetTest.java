package io.github.dirtyredz.herbalwitch.companions.animals.care;

import io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalAiBudget;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AnimalAiBudgetTest {
    @Test void hardPerTickLimitsResetOnlyAtNextTick() {
        var b = new AnimalAiBudget(); var r = AnimalRules.DEFAULTS; b.begin(1);
        for (int i = 0; i < 128; i++) assertTrue(b.candidate(r));
        for (int i = 0; i < 16; i++) assertTrue(b.path(r));
        b.begin(1); assertFalse(b.candidate(r)); assertFalse(b.path(r)); assertEquals(128, b.checks()); assertEquals(16, b.paths());
        b.begin(2); assertTrue(b.candidate(r)); assertTrue(b.path(r));
    }
    @Test void holderAndDimensionLeasesBoundActualActiveGoalsAndCalmCanReplaceBait() {
        var b = new AnimalAiBudget(); var r = AnimalRules.DEFAULTS; UUID holder = new UUID(1, 1);
        for (int i = 0; i < 8; i++) assertTrue(b.claim(new UUID(0, i), holder, r));
        assertFalse(b.claim(new UUID(0, 8), holder, r));
        assertTrue(b.claim(new UUID(0, 0), null, r)); b.release(new UUID(0, 0), holder);
        assertEquals(8, b.active()); assertTrue(b.claim(new UUID(0, 8), holder, r));
        for (int i = 9; i < 256; i++) assertTrue(b.claim(new UUID(0, i), null, r));
        assertFalse(b.claim(new UUID(0, 256), null, r)); b.releaseAll(new UUID(0, 0)); assertTrue(b.claim(new UUID(0, 256), null, r));
    }
}
