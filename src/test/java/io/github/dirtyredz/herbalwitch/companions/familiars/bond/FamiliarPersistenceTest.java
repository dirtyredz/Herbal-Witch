package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarPersistenceTest {
    @TempDir Path directory;
    static FamiliarRecord record() {
        var payload = new CompoundTag(); payload.putString("preserve", "care and native equipment");
        return FamiliarRecord.binding(UUID.randomUUID(), UUID.randomUUID(), "cat", 1, payload);
    }
    @Test void exactRoundTripWithDefensiveCopiesAndPermanentCommandNames() throws Exception {
        var r = record(); assertEquals(r, FamiliarCodec.decode(FamiliarCodec.encode(r, FamiliarCodec.MAX_BYTES)));
        var copy = r.state(); copy.putString("command", "follow"); assertEquals("stay", r.command().key());
        assertThrows(IllegalArgumentException.class, () -> r.change(s -> s.putString("command", "1")));
        byte[] bytes = FamiliarCodec.encode(r, FamiliarCodec.MAX_BYTES);
        assertThrows(IOException.class, () -> FamiliarCodec.decode(Arrays.copyOf(bytes, bytes.length + 1)));
    }
    @Test void cannotMutateBeforeIndexingAndRestartQuarantinesPreparedPayload() {
        var r = record();
        try (var store = new FamiliarStore(directory, (name, data) -> {})) {
            assertFalse(store.create(r)); store.indexBatch(); assertTrue(store.ready()); assertTrue(store.create(r));
        }
        try (var store = new FamiliarStore(directory, (name, data) -> {})) {
            assertFalse(store.ready()); store.indexBatch();
            assertEquals(FamiliarRecord.Phase.QUARANTINED, store.record(r.bond()).orElseThrow().phase());
            assertEquals(r.payload(), store.creature(r.creature()).orElseThrow().payload());
        }
    }
    @Test void unlimitedRosterIndexesInBatchesAndUsesKeysetPages() throws Exception {
        UUID owner = UUID.randomUUID();
        for (int i = 0; i < 65; i++) {
            var seed = record();
            var r = new FamiliarRecord(seed.bond(), owner, seed.creature(), "cat", "minecraft:cat", 1,
                    FamiliarRecord.Phase.ACTIVE, seed.transaction(), 2, seed.state(), new CompoundTag());
            var shard = new FamiliarSavedData(directory, r.bond(), new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT));
            assertTrue(shard.put(r, true, FamiliarCodec.MAX_BYTES));
        }
        try (var store = new FamiliarStore(directory, (name, data) -> {})) {
            assertEquals(32, store.indexBatch()); assertFalse(store.ready());
            assertEquals(32, store.indexBatch()); assertFalse(store.ready());
            assertEquals(1, store.indexBatch()); assertTrue(store.ready());
            var first = store.roster().page(owner, null, 32);
            var second = store.roster().page(owner, first.get(31), 32);
            var third = store.roster().page(owner, second.get(31), 32);
            Set<UUID> all = new HashSet<>(first); all.addAll(second); all.addAll(third);
            assertEquals(65, all.size());
        }
    }
    @Test void failedVerificationKeepsIdentityDirtyAndLocksEveryLaterWriter() {
        var r = record();
        var writer = new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT, stage -> {
            if (stage == VerifiedSnapshotWriter.Stage.BEFORE_VERIFY) throw new IOException("injected post-replace failure");
        });
        var shard = new FamiliarSavedData(directory, r.bond(), writer);
        assertFalse(shard.put(r, true, FamiliarCodec.MAX_BYTES));
        assertTrue(shard.isDirty()); assertFalse(shard.writable()); assertEquals(r, shard.record());
        assertFalse(shard.put(r.quarantine(), true, FamiliarCodec.MAX_BYTES));
        shard.save(directory.resolve("unrelated.dat").toFile()); assertFalse(Files.exists(directory.resolve("unrelated.dat")));
    }
    @Test void corruptFileIsPreservedAndIncompleteIdentityCannotBeAssumedAbsent() throws Exception {
        var path = directory.resolve(FamiliarSavedData.name(UUID.randomUUID()) + ".dat");
        byte[] broken = {1, 2, 3}; Files.write(path, broken);
        try (var store = new FamiliarStore(directory, (name, data) -> {})) {
            store.indexBatch(); assertTrue(store.indexed()); assertFalse(store.ready()); assertFalse(store.create(record()));
        }
        assertArrayEquals(broken, Files.readAllBytes(path));
    }
    @Test void staleRevisionsOwnerChangesAndGenerationRegressionCannotReplaceAuthority() {
        var r = record(); var shard = new FamiliarSavedData(directory, r.bond(), new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT));
        assertTrue(shard.put(r, true, FamiliarCodec.MAX_BYTES)); assertFalse(shard.put(r, true, FamiliarCodec.MAX_BYTES));
        var stolen = new FamiliarRecord(r.bond(), UUID.randomUUID(), r.creature(), r.species(), r.originalType(), 1, r.phase(), r.transaction(), 2, r.state(), r.payload());
        assertFalse(shard.put(stolen, true, FamiliarCodec.MAX_BYTES));
        assertThrows(IllegalArgumentException.class, () -> r.transition(r.phase(), 0, r.transaction(), r.payload()));
    }
    @Test void excessiveDepthAndPayloadRefuseBeforeChangingShard() {
        var r = record(); var deep = r.change(s -> { CompoundTag cursor = s; for (int i = 0; i < 20; i++) { var next = new CompoundTag(); cursor.put("nested", next); cursor = next; } });
        var shard = new FamiliarSavedData(directory, r.bond(), new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT));
        assertFalse(shard.put(deep, true, FamiliarCodec.MAX_BYTES)); assertNull(shard.record());
    }
}
