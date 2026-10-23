package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarWireTest {
    private static FamiliarRosterPage.Row row(String text) {
        return new FamiliarRosterPage.Row(UUID.randomUUID(), text, "cat", 1, 8, 10, "stay", "ACTIVE", false,
                "minecraft:overworld", 123, text.repeat(2), text, text.repeat(2), 500, true);
    }
    @Test void roundTripsMaximumRowCountWithoutRecoveryOrInventoryFields() {
        var rows = new ArrayList<FamiliarRosterPage.Row>(); for (int i = 0; i < 32; i++) rows.add(row("Whiskers"));
        var page = new FamiliarRosterPage(8, 99, 17, rows, true, "");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try { page.encode(buffer); assertEquals(page, FamiliarRosterPage.decode(buffer)); assertFalse(buffer.isReadable()); }
        finally { buffer.release(); }
        assertEquals(Set.of("bond", "name", "species", "level", "health", "maximum", "command", "phase", "loaded", "dimension", "position", "home", "status", "buffs", "reserve", "automatic"),
                Arrays.stream(FamiliarRosterPage.Row.class.getRecordComponents()).map(java.lang.reflect.RecordComponent::getName).collect(java.util.stream.Collectors.toSet()));
    }
    @Test void boundsActualUtf8BytesAndRejectsExcessRowsBeforeAllocation() {
        var rows = new ArrayList<FamiliarRosterPage.Row>(); for (int i = 0; i < 32; i++) rows.add(row("\u754c".repeat(64)));
        var page = new FamiliarRosterPage(8, 99, 17, rows, true, "");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            assertThrows(IllegalArgumentException.class, () -> page.encode(buffer));
            assertThrows(DecoderException.class, () -> FamiliarRosterPage.decode(buffer));
            buffer.clear(); buffer.writeVarInt(1); buffer.writeLong(2); buffer.writeLong(3); buffer.writeVarInt(33);
            assertThrows(DecoderException.class, () -> FamiliarRosterPage.decode(buffer));
        } finally { buffer.release(); }
        rows.add(row("x")); assertThrows(IllegalArgumentException.class, () -> new FamiliarRosterPage(1, 2, 3, rows, false, ""));
    }
    @Test void requestCarriesIntentAndSessionButNeverAnOwnerIdentity() {
        var request = new FamiliarRequest(1, 2, 3, "detail", UUID.randomUUID(), null, "");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            request.encode(buffer); assertEquals(request, FamiliarRequest.decode(buffer));
            buffer.clear().writeZero(513); assertThrows(DecoderException.class, () -> FamiliarRequest.decode(buffer));
        } finally { buffer.release(); }
        assertFalse(Arrays.stream(FamiliarRequest.class.getRecordComponents()).anyMatch(f -> f.getName().equals("owner")));
    }
    @Test void rejectsNonfiniteHealthAndUnknownLevel() {
        assertThrows(IllegalArgumentException.class, () -> new FamiliarRosterPage.Row(UUID.randomUUID(), "", "cat", 1, Float.NaN, 10, "stay", "ACTIVE", false, "", 0, "", "", "", 0, false));
        assertThrows(IllegalArgumentException.class, () -> new FamiliarRosterPage.Row(UUID.randomUUID(), "", "cat", 6, 10, 10, "stay", "ACTIVE", false, "", 0, "", "", "", 0, false));
    }
}
