package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarReceiptVerification;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarReceiptVerificationTest {
    @Test void missingOlderPendingAndDivergentPlayerReceiptsCannotUnlockInventory() {
        var expected = new CompoundTag(); expected.putInt("schema", 1); expected.putLong("sequence", 5);
        expected.putUUID("bond", UUID.randomUUID()); expected.putUUID("transaction", UUID.randomUUID());
        assertFalse(FamiliarReceiptVerification.matches(expected, new CompoundTag()));
        var saved = expected.copy(); saved.putLong("sequence", 4);
        assertFalse(FamiliarReceiptVerification.matches(expected, saved));
        saved = expected.copy(); saved.putBoolean("pending", true);
        assertFalse(FamiliarReceiptVerification.matches(expected, saved));
        saved = expected.copy(); saved.putUUID("transaction", UUID.randomUUID());
        assertFalse(FamiliarReceiptVerification.matches(expected, saved));
        assertTrue(FamiliarReceiptVerification.matches(expected, expected.copy()));
        saved = expected.copy(); saved.putLong("sequence", 6);
        assertTrue(FamiliarReceiptVerification.matches(expected, saved));
    }
}
