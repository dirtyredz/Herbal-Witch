package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarReceiptVerification;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import net.minecraft.nbt.NbtIo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarReceiptVerificationTest {
    @TempDir Path world;

    @Test void integratedHostCannotVerifyNewerPlayerFileOverStaleLevelPlayer() throws Exception {
        var owner = UUID.randomUUID();
        var expected = new CompoundTag(); expected.putInt("schema", 1); expected.putLong("sequence", 5);
        expected.putUUID("bond", UUID.randomUUID()); expected.putUUID("transaction", UUID.randomUUID());
        var state = new CompoundTag(); state.putString("command", "stay"); state.put("settledReceipt", expected);
        var record = new FamiliarRecord(expected.getUUID("bond"), owner, UUID.randomUUID(), "cat", "minecraft:cat",
                1, FamiliarRecord.Phase.ACTIVE, expected.getUUID("transaction"), 1, state, new CompoundTag());
        Files.createDirectories(world.resolve("playerdata"));
        NbtIo.writeCompressed(player(owner, expected), world.resolve("playerdata").resolve(owner + ".dat").toFile());
        var stale = expected.copy(); stale.putLong("sequence", 4);
        var data = new CompoundTag(); data.put("Player", player(owner, stale));
        var level = new CompoundTag(); level.put("Data", data);
        NbtIo.writeCompressed(level, world.resolve("level.dat").toFile());
        assertFalse(FamiliarReceiptVerification.verify(record, world.resolve("data")),
                "level.dat Player takes precedence: a newer playerdata receipt cannot unlock the host bond");
        data.put("Player", player(owner, expected));
        NbtIo.writeCompressed(level, world.resolve("level.dat").toFile());
        assertTrue(FamiliarReceiptVerification.verify(record, world.resolve("data")));
    }

    private static CompoundTag player(UUID owner, CompoundTag receipt) {
        var player = new CompoundTag(); player.putUUID("UUID", owner);
        var forge = new CompoundTag(); forge.put(FamiliarPlayerReceipt.KEY, receipt.copy());
        player.put("ForgeData", forge); return player;
    }

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
