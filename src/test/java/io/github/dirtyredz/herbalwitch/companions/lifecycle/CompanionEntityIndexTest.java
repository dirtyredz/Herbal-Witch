package io.github.dirtyredz.herbalwitch.companions.lifecycle;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CompanionEntityIndexTest {
    private static final class Creature {
        final UUID id;
        boolean removed;
        Creature(UUID id) { this.id = id; }
        @Override public boolean equals(Object other) { return other instanceof Creature c && c.id.equals(id); }
        @Override public int hashCode() { return id.hashCode(); }
    }
    private CompanionEntityIndex<Creature> index() { return new CompanionEntityIndex<>(c -> c.id, c -> !c.removed); }

    @Test void sameObjectIsIdempotentButEqualObjectsDoNotShareAdmission() {
        var index = index(); var first = new Creature(UUID.randomUUID()); var copy = new Creature(first.id);
        assertTrue(index.admit(first)); assertTrue(index.admit(first)); assertFalse(index.admit(copy));
        index.leave(copy);
        assertSame(first, index.current(first.id).orElseThrow());
    }

    @Test void removedIncarnationCanBeReplacedAndItsLateLeaveCannotForgetTheReplacement() {
        var index = index(); var first = new Creature(UUID.randomUUID()); var next = new Creature(first.id);
        assertTrue(index.admit(first)); first.removed = true;
        assertTrue(index.admit(next)); index.leave(first);
        assertSame(next, index.current(first.id).orElseThrow());
        next.removed = true;
        assertTrue(index.current(first.id).isEmpty()); assertFalse(index.admit(next));
    }

    @Test void removalReturnsOnlyTheIndexedObjectAndDoesNotInventAnAbsentOne() {
        var index = index(); var first = new Creature(UUID.randomUUID());
        assertTrue(index.current(first.id).isEmpty()); assertTrue(index.remove(first.id).isEmpty());
        assertTrue(index.admit(first)); assertSame(first, index.remove(first.id).orElseThrow());
        assertTrue(index.current(first.id).isEmpty());
    }

    @Test void independentWorldIndexesNeverShareIdentity() {
        var a = index(); var b = index(); var first = new Creature(UUID.randomUUID()); var second = new Creature(first.id);
        assertTrue(a.admit(first)); assertTrue(b.admit(second));
        a.leave(first);
        assertSame(second, b.current(first.id).orElseThrow());
    }
}
