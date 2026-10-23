package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureSavedData;
import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotProbe;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.UncheckedIOException;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * M12 regression: a capture ledger whose write failed locked transport and also refused every
 * eligible vanilla animal's world join, which permanently removes it (a farm emptied on chunk load).
 * Writing must stay locked while ordinary, never-transported animals keep joining.
 */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class CaptureLedgerLockGameTests {
    private CaptureLedgerLockGameTests() {
    }

    @GameTest(template = "empty", batch = "herbalwitch_capture_ledger_lock")
    public static void writeLockedLedgerStillAdmitsNeverTransportedAnimals(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var data = CaptureSavedData.get(server);
        try {
            try (var probe = new SnapshotProbe((stage, file) -> {
                if (file.getFileName().toString().startsWith(CaptureSavedData.NAME)) throw new UncheckedIOException(new IOException("injected ledger write failure"));
            })) {
                check(!data.checkpoint(), "injected ledger write succeeded");
            }
            check(!data.writable(), "a failed write must lock writing");
            var cow = helper.spawn(EntityType.COW, new BlockPos(4, 1, 4));
            check(helper.getLevel().getEntity(cow.getUUID()) == cow && !cow.isRemoved(), "write-locked ledger refused a never-captured cow");
            helper.succeed();
        } finally {
            // Leave the shared test world with its ledger as written on disk.
            CaptureSavedData.reset();
            CaptureSavedData.get(server);
        }
    }
}
