package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrder;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrderRules;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shard round trips, strict refusal of malformed or foreign data, and the store's indexing contract. */
class ShopCodecTest {
    @TempDir
    Path directory;

    static final ShopStackKey TEA = ShopStackKey.of("herbalwitch:preparations/drinks/recovery_tea");

    static ShopRecord record() {
        ShopRecord r = new ShopRecord(UUID.randomUUID(), UUID.randomUUID(), "Owner", "Hazel's Remedies", 10);
        r.generation = 1;
        r.binding = new ShopBinding("minecraft:overworld", 123L, UUID.randomUUID(), 1);
        assertTrue(r.stock.insert(TEA, 20, 16));
        r.offers.add(new ShopOffer(UUID.randomUUID(), 1, TEA, 1, 2, true));
        r.proceeds = 42;
        r.links.add(new ShopLink(ShopLink.Kind.SHELF, "minecraft:overworld", 99L, UUID.randomUUID(), List.of(r.offers.get(0).id())));
        r.orders.add(ShopOrderRules.create(UUID.randomUUID(), UUID.randomUUID(), "Customer", TEA.item(), ShopOrder.Requirement.ANY, 3, 4, 0, 1000));
        r.record(new ShopHistoryEntry(UUID.randomUUID(), 5, r.owner, "Owner", ShopHistoryEntry.Type.DEPOSIT, TEA.item(), 20, 0));
        r.transactions.add(new ShopTransaction(UUID.randomUUID(), ShopTransaction.Kind.PURCHASE, ShopTransaction.Phase.PREPARED, UUID.randomUUID(),
                "Buyer", true, 3, List.of(new ShopTransaction.SlotChange(0, new CompoundTag(), new CompoundTag())),
                List.of(new ShopTransaction.Goods(TEA, 1, 16)), List.of(), 0, 2, null, 0, r.offers.get(0).id(), 7, ""));
        r.bans.add(UUID.randomUUID());
        return r;
    }

    @Test
    void exactRoundTrip() throws IOException {
        ShopRecord r = record();
        byte[] bytes = ShopCodec.encode(r);
        ShopRecord back = ShopCodec.decode(bytes);
        assertArrayEquals(bytes, ShopCodec.encode(back));
        assertEquals(r.stock.count(TEA), back.stock.count(TEA));
        assertEquals(r.transactions, back.transactions);
        assertEquals(r.offers, back.offers);
        assertEquals(r.orders, back.orders);
    }

    private static byte[] raw(CompoundTag root) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.write(root, new DataOutputStream(out));
        return out.toByteArray();
    }

    @Test
    void unknownSchemaUnknownFieldsDuplicatesAndBoundsAreRefused() throws IOException {
        CompoundTag root = ShopCodec.toTag(record());
        root.putInt("schema", 2);
        assertInstanceOf(ShopCodec.UnsupportedSchema.class, assertThrows(IOException.class, () -> ShopCodec.decode(raw(root))));
        CompoundTag extra = ShopCodec.toTag(record());
        extra.putString("currency", "gold");
        assertThrows(IOException.class, () -> ShopCodec.decode(raw(extra)));
        ShopRecord dup = record();
        dup.offers.add(dup.offers.get(0));
        assertThrows(IOException.class, () -> ShopCodec.decode(raw(ShopCodec.toTag(dup))));
        ShopRecord rich = record();
        rich.proceeds = ShopCodec.MAX_PROCEEDS + 1;
        assertThrows(IOException.class, () -> ShopCodec.encode(rich));
        ShopRecord many = record();
        for (int i = 0; i < 12; i++) many.offers.add(new ShopOffer(UUID.randomUUID(), 1, TEA, 1, 1, true));
        assertThrows(IOException.class, () -> ShopCodec.encode(many));
        ShopRecord badName = record();
        badName.name = "§cRed";
        assertThrows(IOException.class, () -> ShopCodec.encode(badName));
        ShopRecord unbound = record();
        unbound.binding = null;
        assertThrows(IOException.class, () -> ShopCodec.encode(unbound), "an open or closed shop needs its register binding");
    }

    @Test
    void historyIsABoundedRingAndReceiptsAreBoundedIndependently() {
        ShopRecord r = record();
        for (int i = 0; i < 300; i++) r.record(new ShopHistoryEntry(UUID.randomUUID(), i, r.owner, "Owner", ShopHistoryEntry.Type.SALE, "", 1, 1));
        assertEquals(ShopHistoryEntry.CAPACITY, r.history.size());
        assertEquals(299, r.history.peekLast().time());
        assertEquals(1, r.transactions.size(), "history eviction never removes recovery evidence");
    }

    @Test
    void namesAreBoundedPlainText() {
        assertTrue(ShopRecord.validName("Hazel's Remedies"));
        assertTrue(ShopRecord.validName("é".repeat(32)));
        assertFalse(ShopRecord.validName("x".repeat(33)));
        assertFalse(ShopRecord.validName("中".repeat(32 + 1)));
        assertFalse(ShopRecord.validName(" padded"));
        assertFalse(ShopRecord.validName(""));
        assertFalse(ShopRecord.validName("line\nbreak"));
    }

    @Test
    void storeIndexesBeforeMutationAndPreservesUnreadableShards() throws IOException {
        ShopRecord good = record();
        good.transactions.clear();
        UUID corrupt = UUID.randomUUID();
        Path bad = directory.resolve(ShopCodec.name(corrupt) + ".dat");
        Files.write(bad, new byte[] {1, 2, 3});
        try (ShopStore store = new ShopStore(directory, (name, data) -> { })) {
            assertFalse(store.create(good), "no mutation before indexing completes");
            store.indexBatch();
            assertTrue(store.ready());
            assertTrue(store.create(good));
            assertTrue(store.known(corrupt));
            assertEquals("unreadable", store.problem(corrupt).orElseThrow());
            assertTrue(store.record(corrupt).isEmpty());
            assertFalse(store.writable(corrupt));
            assertEquals(good.id, store.indexes().register("minecraft:overworld", 123L).orElseThrow());
            assertEquals(good.id, store.indexes().fixture("minecraft:overworld", 99L).orElseThrow());
            assertEquals(1, store.indexes().owned(good.owner).size());
            ShopRecord stale = good.copy();
            assertFalse(store.update(stale, true), "a record must advance its revision");
            ShopRecord next = good.next();
            next.proceeds = 50;
            assertTrue(store.update(next, true));
        }
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(bad), "an unreadable shard is never rewritten");
        try (ShopStore reopened = new ShopStore(directory, (name, data) -> { })) {
            reopened.indexBatch();
            assertEquals(50, reopened.record(good.id).orElseThrow().proceeds);
            assertFalse(reopened.create(good), "identities are never reused");
        }
        byte[] verified = new VerifiedSnapshotWriter(ShopSavedData.FORMAT).read(directory.resolve(ShopCodec.name(good.id) + ".dat"), ShopCodec.MAX_BYTES);
        assertEquals(50, ShopCodec.decode(verified).proceeds);
    }

    @Test
    void failedCheckpointLeavesTheShardReadOnlyAndKeepsTheRecord() {
        ShopRecord good = record();
        good.transactions.clear();
        ShopSavedData shard = new ShopSavedData(directory, good.id, new VerifiedSnapshotWriter(ShopSavedData.FORMAT,
                stage -> { if (stage == VerifiedSnapshotWriter.Stage.BEFORE_REPLACE) throw new IOException("disk full"); }));
        assertFalse(shard.put(good, true));
        assertFalse(shard.writable());
        assertEquals("write_failed", shard.problem());
        assertEquals(good, shard.record());
        assertTrue(shard.isDirty());
        ShopRecord next = good.next();
        assertFalse(shard.put(next, false), "no further writes after an uncertain checkpoint");
    }
}
