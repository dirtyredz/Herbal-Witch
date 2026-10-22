package io.github.dirtyredz.herbalwitch.magic.ritual.session;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Running altar operations: one per altar and per player, exactly-once completion or cancellation, ordering and tick progress. */
class AltarSessionTest {
    private static final UUID P1 = UUID.nameUUIDFromBytes("p1".getBytes());
    private static final UUID P2 = UUID.nameUUIDFromBytes("p2".getBytes());
    private static final UUID P3 = UUID.nameUUIDFromBytes("p3".getBytes());
    private static final RitualSite ALTAR_A = new RitualSite("minecraft:overworld", 1);
    private static final RitualSite ALTAR_B = new RitualSite("minecraft:overworld", 2);

    private AltarSessions sessions;

    @BeforeEach
    void setUp() {
        sessions = new AltarSessions();
    }

    private AltarSession session(AltarActionSpec action, RitualSite site, UUID player, int duration) {
        return new AltarSession(sessions.nextId(), action, site, Optional.of(player), player, duration, Map.of(player, 7L));
    }

    @Test
    void aStartedSessionIsRegisteredByAltarAndPlayer() {
        AltarSession s = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100);
        assertTrue(sessions.start(s).isEmpty());
        assertTrue(s.running());
        assertEquals(s, sessions.at(ALTAR_A).orElseThrow());
        assertEquals(s, sessions.of(P1).orElseThrow());
        assertEquals(1, sessions.size());
        assertTrue(sessions.at(ALTAR_B).isEmpty());
        assertTrue(sessions.of(P2).isEmpty());
    }

    @Test
    void oneRunningSessionPerAltar() {
        sessions.start(session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100));
        AltarSession second = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P2, 100);
        assertEquals(AltarSessions.Refusal.ALTAR_BUSY, sessions.start(second).orElseThrow());
        assertEquals(1, sessions.size());
        assertTrue(sessions.of(P2).isEmpty(), "a refused session registers nothing");
    }

    @Test
    void oneRunningSessionPerPlayerEvenAtAnotherAltar() {
        sessions.start(session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100));
        AltarSession elsewhere = session(AltarActionSpec.MEDITATION, ALTAR_B, P1, 0);
        assertEquals(AltarSessions.Refusal.PLAYER_BUSY, sessions.start(elsewhere).orElseThrow());
        assertTrue(sessions.at(ALTAR_B).isEmpty());
    }

    @Test
    void anyParticipantBlocksAStart() {
        sessions.start(session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P2, 100));
        AltarSession pair = new AltarSession(sessions.nextId(), AltarActionSpec.INITIATION, ALTAR_B, Optional.of(P1), P3, 200,
                Map.of(P1, 1L, P2, 2L));
        assertEquals(AltarSessions.Refusal.PLAYER_BUSY, sessions.start(pair).orElseThrow());
        assertTrue(sessions.of(P1).isEmpty());
    }

    @Test
    void completeSucceedsExactlyOnce() {
        AltarSession s = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100);
        sessions.start(s);
        assertTrue(sessions.complete(s));
        assertEquals(AltarSession.State.COMPLETED, s.state());
        assertFalse(s.running());
        assertFalse(sessions.complete(s));
        assertFalse(sessions.cancel(s), "a late cancellation finds it already ended");
        assertEquals(AltarSession.State.COMPLETED, s.state());
        assertEquals(0, sessions.size());
    }

    @Test
    void cancelFreesTheAltarAndEveryPlayer() {
        AltarSession pair = new AltarSession(sessions.nextId(), AltarActionSpec.INITIATION, ALTAR_A, Optional.of(P1), P2, 200,
                Map.of(P1, 1L, P2, 2L));
        sessions.start(pair);
        assertTrue(sessions.cancel(pair));
        assertEquals(AltarSession.State.CANCELLED, pair.state());
        assertFalse(sessions.cancel(pair));
        assertFalse(sessions.complete(pair));
        assertTrue(sessions.at(ALTAR_A).isEmpty());
        assertTrue(sessions.of(P1).isEmpty());
        assertTrue(sessions.of(P2).isEmpty());
        assertTrue(sessions.start(session(AltarActionSpec.MEDITATION, ALTAR_A, P1, 0)).isEmpty());
    }

    @Test
    void endingAnOldSessionDoesNotFreeANewOneAtTheSameAltar() {
        AltarSession old = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100);
        sessions.start(old);
        sessions.complete(old);
        AltarSession fresh = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100);
        assertTrue(sessions.start(fresh).isEmpty());
        assertFalse(sessions.cancel(old));
        assertEquals(fresh, sessions.at(ALTAR_A).orElseThrow());
        assertEquals(fresh, sessions.of(P1).orElseThrow());
    }

    @Test
    void cancelIfCancelsOnlyMatchingRunningSessions() {
        AltarSession a = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 100);
        AltarSession b = session(AltarActionSpec.MEDITATION, ALTAR_B, P2, 0);
        sessions.start(a);
        sessions.start(b);
        List<AltarSession> cancelled = sessions.cancelIf(s -> s.involves(P2));
        assertEquals(List.of(b), cancelled);
        assertEquals(AltarSession.State.CANCELLED, b.state());
        assertTrue(a.running());
        assertEquals(1, sessions.size());
        assertTrue(sessions.cancelIf(s -> false).isEmpty());
    }

    @Test
    void runningListsSessionsByIdOldestFirst() {
        AltarSession first = session(AltarActionSpec.ATTUNEMENT, ALTAR_B, P1, 100);
        AltarSession second = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P2, 100);
        AltarSession third = session(AltarActionSpec.MEDITATION, new RitualSite("minecraft:overworld", 3), P3, 0);
        sessions.start(third);
        sessions.start(second);
        sessions.start(first);
        assertEquals(List.of(first, second, third), sessions.running());
        sessions.complete(second);
        assertEquals(List.of(first, third), sessions.running());
    }

    @Test
    void timedSessionIsDueAfterItsDuration() {
        AltarSession s = session(AltarActionSpec.ATTUNEMENT, ALTAR_A, P1, 3);
        assertFalse(s.due());
        assertEquals(1, s.advance());
        assertEquals(2, s.advance());
        assertFalse(s.due());
        assertEquals(3, s.advance());
        assertTrue(s.due());
        assertEquals(3, s.elapsed());
    }

    @Test
    void openEndedSessionIsNeverDue() {
        AltarSession s = session(AltarActionSpec.MEDITATION, ALTAR_A, P1, 0);
        for (int i = 0; i < 1000; i++) {
            s.advance();
        }
        assertFalse(s.due());
        assertEquals(1000, s.elapsed());
    }

    @Test
    void sessionExposesItsParticipantsAndPayload() {
        AltarSession pair = new AltarSession(9, AltarActionSpec.INITIATION, ALTAR_A, Optional.of(P1), P2, 200, Map.of(P1, 1L, P2, 2L));
        assertTrue(pair.involves(P1));
        assertTrue(pair.involves(P2));
        assertFalse(pair.involves(P3));
        assertEquals(2, pair.participants().size());
        assertEquals(Map.of(P1, 1L, P2, 2L), pair.menus());
        assertEquals(9, pair.id());
        assertEquals(P2, pair.subject());
        String payload = "offering";
        pair.setPayload(payload);
        String back = pair.payload();
        assertEquals(payload, back);
    }

    @Test
    void sessionIdsIncrease() {
        long a = sessions.nextId();
        long b = sessions.nextId();
        assertTrue(b > a);
    }
}
