package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.FamiliarPickupJournal;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarPickupJournalTest {
    @TempDir Path directory;
    private static CompoundTag source(UUID entity) { var source = new CompoundTag(); source.putUUID("UUID", entity); source.putString("id", "minecraft:item"); return source; }
    @Test void freshJournalCannotAdmitARetiredSourceOrOverwriteItsPayload() throws Exception {
        var id = UUID.randomUUID(); var bond = UUID.randomUUID(); var operation = UUID.randomUUID();
        var journal = new FamiliarPickupJournal(directory); assertFalse(journal.retired(id));
        assertTrue(journal.prepare(id, bond, operation, source(id)));
        var reopened = new FamiliarPickupJournal(directory); assertTrue(reopened.retired(id));
        assertEquals(source(id), reopened.inspect(id).getCompound("source"));
        assertFalse(reopened.prepare(id, UUID.randomUUID(), UUID.randomUUID(), source(id)));
        assertEquals(bond, reopened.inspect(id).getUUID("bond"));
    }
    @Test void failedVerificationNeverAuthorizesTheOldItemAndCorruptFilesStayRetired() throws Exception {
        var id = UUID.randomUUID();
        var writer = new VerifiedSnapshotWriter(FamiliarPickupJournal.FORMAT, stage -> {
            if (stage == VerifiedSnapshotWriter.Stage.BEFORE_VERIFY) throw new java.io.IOException("injected pickup verification failure");
        });
        var journal = new FamiliarPickupJournal(directory, writer);
        assertFalse(journal.prepare(id, UUID.randomUUID(), UUID.randomUUID(), source(id)));
        assertTrue(journal.retired(id)); assertTrue(new FamiliarPickupJournal(directory).retired(id));
        Files.write(directory.resolve(id + ".dat"), new byte[]{1,2,3});
        var corrupt = new FamiliarPickupJournal(directory); assertTrue(corrupt.retired(id));
        assertThrows(java.io.IOException.class, () -> corrupt.inspect(id));
    }
    @Test void failureBeforeWriteRetainsTheLiveClaimWithoutInventingADurableGrant() {
        var id = UUID.randomUUID();
        var journal = new FamiliarPickupJournal(directory, new VerifiedSnapshotWriter(FamiliarPickupJournal.FORMAT, stage -> {
            if (stage == VerifiedSnapshotWriter.Stage.BEFORE_WRITE) throw new java.io.IOException("injected pickup write failure");
        }));
        assertFalse(journal.prepare(id, UUID.randomUUID(), UUID.randomUUID(), source(id)));
        assertTrue(journal.retired(id)); assertFalse(new FamiliarPickupJournal(directory).retired(id));
        // The caller's pending bond/player receipt remains authoritative; reopening never replays a cargo grant.
    }
}
