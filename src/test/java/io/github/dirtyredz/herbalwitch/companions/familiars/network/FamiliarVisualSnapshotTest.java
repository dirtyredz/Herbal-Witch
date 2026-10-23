package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarVisualSnapshotTest {
    @Test void trackerProjectionIsBoundedAndCannotCarryPrivateInventory() {
        var value = new FamiliarVisualSnapshot(23, UUID.randomUUID(), 7, "cat_bell", "protective_sachet", "protect_home", true);
        var bytes = new FriendlyByteBuf(Unpooled.buffer());
        try { value.encode(bytes); assertTrue(bytes.readableBytes() <= FamiliarVisualSnapshot.MAX_BYTES); assertEquals(value, FamiliarVisualSnapshot.decode(bytes)); }
        finally { bytes.release(); }
        assertThrows(IllegalArgumentException.class, () -> new FamiliarVisualSnapshot(23, UUID.randomUUID(), 1, "inventory_nbt", "", "stay", false));
        var oversized = new FriendlyByteBuf(Unpooled.buffer());
        try { oversized.writeZero(FamiliarVisualSnapshot.MAX_BYTES + 1); assertThrows(IllegalArgumentException.class, () -> FamiliarVisualSnapshot.decode(oversized)); }
        finally { oversized.release(); }
    }
}
