package io.github.dirtyredz.herbalwitch.apothecary.shop.persistence;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopCodec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopSavedData;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopCheckpointProbe;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopPlayerReceipt;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopReconciliation;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopRecovery;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotProbe;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Persistence and recovery against the real store, writer and player files: reload, unsupported
 * schemas preserved, failed player checkpoints quarantined and resolved only from proof, failed shop
 * commits left read-only, and startup reconciliation from saved player data.
 */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopPersistenceGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);

    static Path temp() {
        try {
            return Files.createTempDirectory("herbalwitch-shop-test");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_persistence", timeoutTicks = 60)
    public static void shardsReloadExactlyAndUnsupportedSchemasStayPreserved(GameTestHelper h) throws IOException {
        floor(h);
        Shop shop = shop(h, witch(h, "persist_owner"), COUNTER);
        selling(h, shop, 7, 3);
        ShopRecord live = record(h, shop);
        try (ShopStore reopened = new ShopStore(store(h).directory(), (name, data) -> { })) {
            while (!reopened.indexed()) reopened.indexBatch();
            ShopRecord back = reopened.record(shop.id()).orElseThrow();
            check(java.util.Arrays.equals(ShopCodec.encode(live), ShopCodec.encode(back)), "reloaded shard differs");
        }
        Path dir = temp();
        CompoundTag future = ShopCodec.toTag(live);
        future.putInt("schema", 2);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        NbtIo.write(future, new DataOutputStream(raw));
        Path file = dir.resolve(ShopCodec.name(live.id) + ".dat");
        new VerifiedSnapshotWriter(ShopSavedData.FORMAT).write(file, raw.toByteArray(), ShopCodec.MAX_BYTES);
        byte[] before = Files.readAllBytes(file);
        try (ShopStore foreign = new ShopStore(dir, (name, data) -> { })) {
            foreign.indexBatch();
            check(foreign.record(live.id).isEmpty() && "unsupported".equals(foreign.problem(live.id).orElse("")), "unsupported schema became a shop");
            check(!foreign.create(live), "an unsupported identity was reused");
        }
        check(java.util.Arrays.equals(before, Files.readAllBytes(file)), "unsupported shard rewritten");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_persistence", timeoutTicks = 60)
    public static void aFailedPlayerCheckpointQuarantinesAndOnlyProofSettlesIt(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "quarantine_owner"), COUNTER);
        UUID offer = selling(h, shop, 4, 2);
        ServerPlayer buyer = player(h, "quarantine_buyer");
        emeralds(buyer, 6);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        ShopResult result;
        try (ShopCheckpointProbe ignored = new ShopCheckpointProbe()) {
            result = ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null);
        }
        check(!result.success() && result.code().equals("recovery") && result.transaction() != null, "unverified checkpoint reported " + result);
        ShopRecord held = record(h, shop);
        check(held.lifecycle == ShopRecord.Lifecycle.QUARANTINED && held.transactions.size() == 1, "not quarantined");
        check(held.proceeds == 0 && held.stock.count(ShopStackKey.of(tea(1))) == 3, "prepared value became spendable");
        check(ShopPlayerReceipt.of(buyer).orElseThrow().pending(), "receipt not pending");
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null).code().equals("recovery"), "a blocked shop sold again");
        ServerPlayer other = player(h, "second_buyer");
        emeralds(other, 6);
        check(!ShopTrades.purchase(other, store(h), shop.id(), offer, revision, null).success(), "a quarantined shop sold to someone else");
        check(ShopTrades.withdraw(shop.owner(), store(h), shop.id(), 0, 1).code().equals("recovery"), "stock moved while a transfer was unresolved");
        var outcome = ShopReconciliation.player(buyer, false);
        check(outcome.orElse(null) == ShopRecovery.Outcome.FINALIZE, "proven checkpoint did not finalize: " + outcome);
        ShopRecord settled = record(h, shop);
        check(settled.transactions.isEmpty() && settled.proceeds == 2 && settled.lifecycle == ShopRecord.Lifecycle.CLOSED, "settlement");
        check(!ShopPlayerReceipt.of(buyer).orElseThrow().pending(), "receipt still pending");
        check(count(buyer, tea(1).getItem()) == 1 && count(buyer, Items.EMERALD) == 4, "buyer value");
        check(ShopReconciliation.player(buyer, false).isEmpty(), "settlement replayed");
        check(record(h, shop).proceeds == 2, "proceeds credited twice");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_persistence", timeoutTicks = 60)
    public static void aFailedShopCommitLeavesTheShardReadOnlyWithProofOnThePlayerSide(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "commit_owner"), COUNTER);
        UUID offer = selling(h, shop, 4, 2);
        ServerPlayer buyer = player(h, "commit_buyer");
        emeralds(buyer, 6);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        AtomicInteger replaces = new AtomicInteger();
        String name = ShopCodec.name(shop.id());
        ShopResult result;
        try (SnapshotProbe ignored = new SnapshotProbe((stage, path) -> {
            if (stage == VerifiedSnapshotWriter.Stage.BEFORE_REPLACE && path.getFileName().toString().startsWith(name) && replaces.incrementAndGet() == 2) {
                throw new IllegalStateException("simulated full disk");
            }
        })) {
            result = ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null);
        }
        check(result.code().equals("recovery") && result.transaction() != null, "a failed commit reported " + result);
        check(!store(h).writable(shop.id()), "shard still writable after an uncertain write");
        check(ShopPlayerReceipt.of(buyer).orElseThrow().pending() && count(buyer, tea(1).getItem()) == 1, "player endpoint");
        check(ShopTrades.purchase(player(h, "late"), store(h), shop.id(), offer, revision, null).code().equals("unavailable"), "a read-only shop sold");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_persistence", timeoutTicks = 60)
    public static void startupReconciliationFinalizesRollsBackOrQuarantinesFromSavedPlayerData(GameTestHelper h) throws IOException {
        floor(h);
        var server = h.getLevel().getServer();
        ServerPlayer applied = player(h, "file_applied"), untouched = player(h, "file_untouched"), older = player(h, "file_older");
        UUID owner = UUID.randomUUID();
        Path dir = temp();
        List<UUID> shops = new java.util.ArrayList<>();
        for (ServerPlayer p : List.of(applied, untouched, older)) {
            p.getInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
            ShopRecord r = new ShopRecord(UUID.randomUUID(), owner, "Owner", "Restart Shop", 0);
            r.generation = 1;
            r.binding = new io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopBinding("minecraft:overworld", 1L, UUID.randomUUID(), 1);
            r.stock.insert(ShopStackKey.of(tea(1)), 3, 16);
            long sequence = p == older ? 5 : 1;
            ShopTransaction t = new ShopTransaction(UUID.randomUUID(), ShopTransaction.Kind.PURCHASE, ShopTransaction.Phase.PREPARED, p.getUUID(), "p", true,
                    sequence, List.of(new ShopTransaction.SlotChange(0, new ItemStack(Items.EMERALD, 2).save(new CompoundTag()), new CompoundTag()),
                    new ShopTransaction.SlotChange(1, new CompoundTag(), tea(1).save(new CompoundTag()))),
                    List.of(new ShopTransaction.Goods(ShopStackKey.of(tea(1)), 1, 16)), List.of(), 0, 2, null, 0, null, 0, "");
            ShopRecord prepared = io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTestAccess.prepare(r, t);
            new ShopSavedData(dir, r.id, new VerifiedSnapshotWriter(ShopSavedData.FORMAT)).put(prepared, true);
            shops.add(r.id);
            if (p == applied) {
                p.getInventory().setItem(0, ItemStack.EMPTY);
                p.getInventory().setItem(1, tea(1));
                io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTestAccess.receipt(p, new ShopPlayerReceipt.Marker(1, t.id(), r.id, true));
            }
            check(io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopPlayerCheckpoint.save(p), "player file");
        }
        try (ShopStore restarted = new ShopStore(dir, (name, data) -> { })) {
            restarted.onLoaded(shard -> ShopReconciliation.startup(server, restarted, shard));
            restarted.indexBatch();
            ShopRecord a = restarted.record(shops.get(0)).orElseThrow(), b = restarted.record(shops.get(1)).orElseThrow(), c = restarted.record(shops.get(2)).orElseThrow();
            check(a.transactions.isEmpty() && a.proceeds == 2 && a.stock.count(ShopStackKey.of(tea(1))) == 2, "receipt-bearing player file did not finalize");
            check(b.transactions.isEmpty() && b.proceeds == 0 && b.stock.count(ShopStackKey.of(tea(1))) == 3, "untouched player file did not roll back");
            check(c.transactions.size() == 1 && c.lifecycle == ShopRecord.Lifecycle.QUARANTINED && c.proceeds == 0 && c.stock.count(ShopStackKey.of(tea(1))) == 2,
                    "an older player file was trusted");
        }
        h.succeed();
    }
}
