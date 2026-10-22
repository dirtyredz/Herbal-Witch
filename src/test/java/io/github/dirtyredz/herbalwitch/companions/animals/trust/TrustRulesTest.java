package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TrustRulesTest {
    private static final UUID PLAYER = new UUID(0, 1);

    @Test void personalFeedsHaveExactLoadedCooldownAndThresholds() {
        var state = new AnimalState();
        assertTrue(state.feed(PLAYER, 8));
        assertFalse(state.feed(PLAYER, 8));
        for (int i = 0; i < 1199; i++) state.tick();
        assertFalse(state.feed(PLAYER, 8));
        state.tick();
        assertTrue(state.feed(PLAYER, 8));
        for (int feed = 2; feed < 5; feed++) {
            for (int i = 0; i < 1200; i++) state.tick();
            assertTrue(state.feed(PLAYER, 8));
            assertEquals(feed >= 3, TrustRules.earned(state.relationship(PLAYER), false));
            assertEquals(feed == 4, TrustRules.earned(state.relationship(PLAYER), true));
        }
        for (int i = 0; i < 1200; i++) state.tick();
        assertFalse(state.feed(PLAYER, 8));
    }

    @Test void inspectionDoesNotAllocateAndCapacityNeverEvicts() {
        var state = new AnimalState();
        assertEquals(AnimalRelationshipView.EMPTY, state.relationship(PLAYER));
        assertEquals(0, state.relationshipCount());
        for (int i = 0; i < 8; i++) assertTrue(state.feed(new UUID(0, i), 8));
        assertFalse(state.feed(new UUID(0, 9), 8));
        assertEquals(8, state.relationshipCount());
        assertEquals(20, state.relationship(PLAYER).trust());
    }

    @Test void damageIsPersonalAndDoesNotRevokeKeeperOrInventHistory() {
        var state = new AnimalState();
        state.keeper = PLAYER;
        var other = new UUID(0, 2);
        state.feed(PLAYER, 8);
        state.feed(other, 8);
        state.damaged(PLAYER);
        assertEquals(0, state.relationship(PLAYER).trust());
        assertEquals(1, state.relationship(PLAYER).feeds());
        assertEquals(20, state.relationship(other).trust());
        assertEquals(600, state.hurtRemaining());
        assertEquals(PLAYER, state.keeper().orElseThrow());
        state.damaged(new UUID(0, 99));
        assertEquals(2, state.relationshipCount());
    }
}
