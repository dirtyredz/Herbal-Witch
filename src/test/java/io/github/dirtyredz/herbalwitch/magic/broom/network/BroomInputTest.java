package io.github.dirtyredz.herbalwitch.magic.broom.network;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BroomInputTest {
    @Test void authenticatesRiderSessionAndMonotonicSequence() {
        UUID rider = UUID.randomUUID(), nonce = UUID.randomUUID();
        var session = new BroomControlSession(rider, nonce, 0);
        assertFalse(session.accept(UUID.randomUUID(), nonce, 0, 1, 0));
        assertFalse(session.accept(rider, UUID.randomUUID(), 0, 1, 0));
        assertFalse(session.accept(rider, nonce, 0, 128, 0));
        assertTrue(session.accept(rider, nonce, 0, 1, 0));
        assertFalse(session.accept(rider, nonce, 1, 2, 0));
        assertFalse(session.accept(rider, nonce, 0, 2, 1));
        assertTrue(session.accept(rider, nonce, 1, 2, 1));
        assertEquals(2, session.flags(10));
        assertEquals(0, session.flags(11));
        assertFalse(session.expired(40));
        assertTrue(session.expired(41));
    }

    @Test void floodIsBoundedAndNewMountRejectsOldPackets() {
        UUID rider = UUID.randomUUID(), nonce = UUID.randomUUID();
        var session = new BroomControlSession(rider, nonce, 0);
        for (int i = 0; i < 10000; i++) { session.accept(rider, nonce, i, -1, 0); }
        assertFalse(session.accept(rider, nonce, 10001, 1, 1));
        assertTrue(session.accept(rider, nonce, 10002, 1, 20));
        var remount = new BroomControlSession(rider, UUID.randomUUID(), 21);
        assertFalse(remount.accept(rider, nonce, 10003, 1, 21));
    }

}
