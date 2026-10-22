package io.github.dirtyredz.herbalwitch.companions.animals.inspection;

import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.*;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AnimalPayloadTest {
    @Test void cardRoundTripIsBoundedAndContainsOnlyOneRelationship() {
        var card = new AnimalSnapshot(2, 42, new UUID(0, 1), 3, "minecraft:cow", 80, 4, 1200, 0, 0, 100, 0, 0, 13, Reason.READY);
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try { card.encode(buf); assertTrue(buf.readableBytes() < 4096); assertEquals(card, AnimalSnapshot.decode(buf)); }
        finally { buf.release(); }
    }
    @Test void negativeOutOfBoundAndTrailingCardFactsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new AnimalSnapshot(1, 2, new UUID(0, 1), 1, "cow", 101, 0, 0, 0, 0, 0, 0, 0, 0, Reason.READY));
        var card = new AnimalSnapshot(1, 2, new UUID(0, 1), 1, "cow", 0, 0, 0, 0, 0, 0, 0, 0, 0, Reason.READY);
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try { card.encode(buf); buf.writeByte(1); assertThrows(RuntimeException.class, () -> AnimalSnapshot.decode(buf)); }
        finally { buf.release(); }
    }
    @Test void adoptionRequestCannotChangeTargetSessionOrRevision() {
        UUID target = new UUID(0, 1); var request = new AnimalRequest(2, 10, target, 7);
        assertTrue(request.matches(2, 10, target, 7)); assertFalse(request.matches(3, 10, target, 7));
        assertFalse(request.matches(2, 11, target, 7)); assertFalse(request.matches(2, 10, new UUID(0, 2), 7)); assertFalse(request.matches(2, 10, target, 8));
    }
}
