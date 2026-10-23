package io.github.dirtyredz.herbalwitch.apothecary.shop.persistence;

import com.google.gson.Gson;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandCodec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandSavedData;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopCodec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopCheckpointProbe;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopPlayerCheckpoint;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopReconciliation;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransfer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotProbe;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.gametest.GameTestHolder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Commerce restart qualification across two dedicated JVMs. The prepare process runs real transfers
 * and stops each one at a durable boundary (a thrown {@link Halt} is an Error, so no production catch
 * block runs and nothing further is written), copying the exact shard, player and demand file bytes on
 * disk at that instant. The verify process restores those bytes, reconciles them with the production
 * startup path and checks the outcome and the conservation of usable value.
 */
@GameTestHolder("herbalwitch_commerce_restart")
public final class ShopRestartGameTests {
    /** A simulated process stop: never caught by production code. */
    static final class Halt extends Error {
        Halt(String where) {
            super("halt at " + where, null, false, false);
        }
    }

    private static final Gson GSON = new Gson();
    private static final List<String> KINDS = List.of("deposit", "purchase", "withdraw", "proceeds");
    private static final List<String> BOUNDARIES = List.of("prepare_unwritten", "prepared", "player_saved", "commit_unwritten", "committed");

    private static Path evidence() {
        return Path.of(System.getProperty("herbalwitch.commerce.restart.evidence"));
    }

    @GameTestGenerator
    public static Collection<TestFunction> restart() {
        String phase = System.getProperty("herbalwitch.commerce.restart.phase", "");
        if (phase.isEmpty()) return List.of();
        if (!Set.of("prepare", "verify").contains(phase)) throw new IllegalArgumentException("Unknown commerce restart phase");
        return List.of(new TestFunction("commerce_restart", "commerce_restart." + phase, "herbalwitch_commerce_restart:empty", 1200, 0, true,
                h -> {
                    try {
                        if (phase.equals("prepare")) prepare(h);
                        else verify(h);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
    }

    // ---- prepare ----

    record Totals(int teas, long emeralds) {
    }

    private static Path playerFile(MinecraftServer server, UUID player) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player + ".dat");
    }

    /** Copies the files a scenario's reconciliation needs, as they are on disk right now. */
    private static void capture(MinecraftServer server, String scenario, UUID shop, UUID player, boolean demand) {
        try {
            Path dir = evidence().resolve(scenario);
            Files.createDirectories(dir);
            Path shard = ShopStore.get(server).directory().resolve(ShopCodec.name(shop) + ".dat");
            Files.copy(shard, dir.resolve("shard.dat"), StandardCopyOption.REPLACE_EXISTING);
            if (player != null && Files.exists(playerFile(server, player))) Files.copy(playerFile(server, player), dir.resolve("player.dat"), StandardCopyOption.REPLACE_EXISTING);
            if (demand) {
                Path file = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(NpcDemandSavedData.NAME + ".dat");
                if (Files.exists(file)) Files.copy(file, dir.resolve("demand.dat"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Totals totals(ServerPlayer player, ShopRecord shop) {
        int teas = count(player, tea(1).getItem()) + shop.stock.count(ShopStackKey.of(tea(1)));
        long emeralds = count(player, Items.EMERALD) + shop.proceeds;
        for (ShopTransaction t : shop.transactions) {
            teas += t.stockOut().stream().mapToInt(ShopTransaction.Goods::count).sum();
            emeralds += t.proceedsOut();
        }
        return new Totals(teas, emeralds);
    }

    private static void prepare(GameTestHelper h) throws IOException {
        Files.createDirectories(evidence());
        MinecraftServer server = h.getLevel().getServer();
        floor(h);
        Map<String, Object> scenarios = new LinkedHashMap<>();
        int x = 0;
        for (String kind : KINDS) {
            for (String boundary : BOUNDARIES) {
                String name = kind + "." + boundary;
                ServerPlayer owner = witch(h, "r_owner");
                Shop shop = shop(h, owner, new BlockPos(1 + (x % 7), 1, 1 + (x / 7) % 7));
                x++;
                UUID offer = selling(h, shop, 6, 2);
                ServerPlayer actor = kind.equals("purchase") ? player(h, "r_buyer") : owner;
                if (kind.equals("purchase")) emeralds(actor, 10);
                if (kind.equals("deposit")) actor.getInventory().setItem(3, tea(4));
                if (kind.equals("proceeds")) {
                    ServerPlayer buyer = player(h, "r_payer");
                    emeralds(buyer, 4);
                    check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, record(h, shop).offer(offer).orElseThrow().revision(), null).success(), "proceeds setup");
                }
                check(ShopPlayerCheckpoint.save(actor), "baseline player file");
                Totals before = totals(actor, record(h, shop));
                run(server, name, boundary, shop, actor, () -> {
                    long revision = record(h, shop).offer(offer).orElseThrow().revision();
                    switch (kind) {
                        case "deposit" -> ShopTrades.deposit(actor, store(h), shop.id(), 3, 4);
                        case "purchase" -> ShopTrades.purchase(actor, store(h), shop.id(), offer, revision, null);
                        case "withdraw" -> ShopTrades.withdraw(actor, store(h), shop.id(), 0, 2);
                        default -> ShopTrades.proceeds(actor, store(h), shop.id(), 2);
                    }
                });
                scenarios.put(name, Map.of("shop", shop.id().toString(), "player", actor.getUUID().toString(), "teas", before.teas(), "emeralds", before.emeralds(),
                        "boundary", boundary, "kind", kind));
            }
        }
        for (String boundary : List.of("prepared", "debited")) {
            String name = "npc." + boundary;
            Shop shop = shop(h, witch(h, "r_npc"), new BlockPos(1 + (x % 7), 1, 1 + (x / 7) % 7));
            x++;
            selling(h, shop, 3, 1);
            ShopRecord r = record(h, shop);
            UUID villager = UUID.randomUUID();
            var plan = new ShopTransfer.Plan(ShopTransaction.Kind.NPC_SALE, r.offers.get(0).id(), List.of(),
                    List.of(new ShopTransaction.Goods(ShopStackKey.of(tea(1)), 1, 16)), List.of(), 0, 1, null, 0);
            try {
                ShopTransfer.villager(store(h), shop.id(), villager, 0, plan, t -> {
                    if (boundary.equals("debited")) check(NpcDemandService.debit(server, t, r.owner, r.id, villager), "debit");
                    capture(server, name, shop.id(), null, true);
                    throw new Halt(name);
                });
            } catch (Halt expected) {
                // the process "stopped" here
            }
            scenarios.put(name, Map.of("shop", shop.id().toString(), "teas", 3, "emeralds", 0, "boundary", boundary, "kind", "npc"));
        }
        olderPlayerFile(h, server, scenarios, x);
        Files.writeString(evidence().resolve("prepared.json"), GSON.toJson(Map.of("process", ProcessHandle.current().pid(), "scenarios", scenarios)));
        h.succeed();
    }

    /** Runs one transfer and stops it at {@code boundary}, capturing the files on disk at that instant. */
    private static void run(MinecraftServer server, String name, String boundary, Shop shop, ServerPlayer actor, Runnable transfer) {
        String file = ShopCodec.name(shop.id());
        AtomicInteger writes = new AtomicInteger();
        Consumer<String> player = stage -> {
            if (boundary.equals("prepared") && stage.equals("before_save") || boundary.equals("player_saved") && stage.equals("after_save")) {
                capture(server, name, shop.id(), actor.getUUID(), false);
                throw new Halt(name);
            }
        };
        try (SnapshotProbe shard = new SnapshotProbe((stage, path) -> {
            if (stage != VerifiedSnapshotWriter.Stage.BEFORE_REPLACE || !path.getFileName().toString().startsWith(file)) return;
            int n = writes.incrementAndGet();
            if (boundary.equals("prepare_unwritten") && n == 1 || boundary.equals("commit_unwritten") && n == 2) {
                capture(server, name, shop.id(), actor.getUUID(), false);
                throw new Halt(name);
            }
        }); ShopCheckpointProbe checkpoint = new ShopCheckpointProbe(player)) {
            transfer.run();
            if (boundary.equals("committed")) capture(server, name, shop.id(), actor.getUUID(), false);
            else check(false, name + " never reached its boundary");
        } catch (Halt expected) {
            // the process "stopped" here
        }
    }

    /** A purchase whose player file is older than the transaction before it: never trusted. */
    private static void olderPlayerFile(GameTestHelper h, MinecraftServer server, Map<String, Object> scenarios, int x) throws IOException {
        Shop shop = shop(h, witch(h, "r_old"), new BlockPos(1 + (x % 7), 1, 1 + (x / 7) % 7));
        UUID offer = selling(h, shop, 4, 1);
        ServerPlayer buyer = player(h, "r_old_buyer");
        emeralds(buyer, 4);
        check(ShopPlayerCheckpoint.save(buyer), "baseline");
        Path old = evidence().resolve("player.older.dat");
        Files.copy(playerFile(server, buyer.getUUID()), old, StandardCopyOption.REPLACE_EXISTING);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null).success(), "first purchase");
        run(server, "purchase.older_player_file", "player_saved", shop, buyer,
                () -> ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null));
        Files.copy(old, evidence().resolve("purchase.older_player_file").resolve("player.dat"), StandardCopyOption.REPLACE_EXISTING);
        scenarios.put("purchase.older_player_file", Map.of("shop", shop.id().toString(), "player", buyer.getUUID().toString(), "teas", 0, "emeralds", 0,
                "boundary", "older", "kind", "purchase"));
        // The same transaction with its player file missing entirely.
        Path missing = evidence().resolve("purchase.missing_player_file");
        Files.createDirectories(missing);
        Files.copy(evidence().resolve("purchase.older_player_file").resolve("shard.dat"), missing.resolve("shard.dat"), StandardCopyOption.REPLACE_EXISTING);
        scenarios.put("purchase.missing_player_file", Map.of("shop", shop.id().toString(), "player", UUID.randomUUID().toString(), "teas", 0, "emeralds", 0,
                "boundary", "missing", "kind", "purchase"));
    }

    // ---- verify ----

    @SuppressWarnings("unchecked")
    private static void verify(GameTestHelper h) throws IOException {
        MinecraftServer server = h.getLevel().getServer();
        Map<String, Object> prepared = GSON.fromJson(Files.readString(evidence().resolve("prepared.json")), Map.class);
        Map<String, Map<String, Object>> scenarios = (Map<String, Map<String, Object>>) prepared.get("scenarios");
        List<String> checks = new ArrayList<>();
        for (var entry : scenarios.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> s = entry.getValue();
            UUID shop = UUID.fromString((String) s.get("shop"));
            Path dir = evidence().resolve(name);
            Path store = Files.createTempDirectory("herbalwitch-commerce-restart");
            Files.copy(dir.resolve("shard.dat"), store.resolve(ShopCodec.name(shop) + ".dat"));
            UUID player = s.containsKey("player") ? UUID.fromString((String) s.get("player")) : null;
            if (player != null) {
                Files.deleteIfExists(playerFile(server, player));
                if (Files.exists(dir.resolve("player.dat"))) Files.copy(dir.resolve("player.dat"), playerFile(server, player));
            }
            var demand = Files.exists(dir.resolve("demand.dat"))
                    ? NpcDemandCodec.decode(new VerifiedSnapshotWriter(NpcDemandSavedData.FORMAT).read(dir.resolve("demand.dat"), NpcDemandCodec.MAX_BYTES)) : null;
            ShopReconciliation.npcEvidence((srv, t) -> demand != null && demand.debited(t));
            try (ShopStore restarted = new ShopStore(store, (n, d) -> { })) {
                restarted.onLoaded(shard -> ShopReconciliation.startup(server, restarted, shard));
                restarted.indexBatch();
                ShopRecord r = restarted.record(shop).orElseThrow(() -> new AssertionError(name + " shard unreadable"));
                outcome(server, name, s, r, player, demand);
                checks.add(name);
            } finally {
                ShopReconciliation.npcEvidence(NpcDemandService::debited);
            }
        }
        corruptShardIsPreserved(scenarios, checks);
        Files.writeString(evidence().resolve("verified.json"), GSON.toJson(Map.of("process", ProcessHandle.current().pid(), "checks", checks)));
        h.succeed();
    }

    private static void outcome(MinecraftServer server, String name, Map<String, Object> s, ShopRecord r, UUID player, Object demand) {
        String boundary = (String) s.get("boundary");
        String kind = (String) s.get("kind");
        var endpoint = player == null ? null : ShopPlayerCheckpoint.saved(server, player);
        int playerTeas = 0;
        long playerEmeralds = 0;
        if (endpoint != null && endpoint.readable()) {
            for (var tag : endpoint.slots().values()) {
                ItemStack stack = ItemStack.of(tag);
                if (stack.is(tea(1).getItem())) playerTeas += stack.getCount();
                if (stack.is(Items.EMERALD)) playerEmeralds += stack.getCount();
            }
        }
        int teas = playerTeas + r.stock.count(ShopStackKey.of(tea(1)));
        long emeralds = playerEmeralds + r.proceeds;
        for (ShopTransaction t : r.transactions) {
            teas += t.stockOut().stream().mapToInt(ShopTransaction.Goods::count).sum();
            emeralds += t.proceedsOut();
        }
        switch (boundary) {
            case "older", "missing" -> {
                check(r.transactions.size() == 1 && r.lifecycle == ShopRecord.Lifecycle.QUARANTINED, name + ": unproven evidence was trusted");
                return;
            }
            case "prepared", "debited" -> {
                if (kind.equals("npc")) {
                    boolean debited = boundary.equals("debited");
                    check(r.transactions.isEmpty(), name + ": npc sale unresolved");
                    check(r.proceeds == (debited ? 1 : 0) && r.stock.count(ShopStackKey.of(tea(1))) == (debited ? 2 : 3), name + ": npc outcome " + r.proceeds);
                    return;
                }
                check(r.transactions.isEmpty() && r.settled(r.receipts.peekLast().transaction()) && r.receipts.peekLast().discarded(), name + ": not rolled back");
            }
            case "player_saved", "commit_unwritten", "committed" -> check(r.transactions.isEmpty() && !r.receipts.peekLast().discarded(), name + ": not finalized");
            default -> check(r.transactions.isEmpty(), name + ": an unwritten prepare left a transaction");
        }
        check(teas == ((Number) s.get("teas")).intValue() && emeralds == ((Number) s.get("emeralds")).longValue(),
                name + ": value not conserved " + teas + "/" + emeralds + " vs " + s.get("teas") + "/" + s.get("emeralds"));
        check(!r.blocked(), name + ": shop still blocked");
    }

    private static void corruptShardIsPreserved(Map<String, Map<String, Object>> scenarios, List<String> checks) throws IOException {
        var any = scenarios.entrySet().iterator().next();
        UUID shop = UUID.fromString((String) any.getValue().get("shop"));
        Path store = Files.createTempDirectory("herbalwitch-commerce-corrupt");
        byte[] bytes = Files.readAllBytes(evidence().resolve(any.getKey()).resolve("shard.dat"));
        bytes[bytes.length - 1] ^= 0x5A;
        Path file = store.resolve(ShopCodec.name(shop) + ".dat");
        Files.write(file, bytes);
        try (ShopStore restarted = new ShopStore(store, (n, d) -> { })) {
            restarted.indexBatch();
            check(restarted.record(shop).isEmpty() && restarted.problem(shop).isPresent() && !restarted.writable(shop), "corrupt shard became a shop");
        }
        check(java.util.Arrays.equals(bytes, Files.readAllBytes(file)), "corrupt shard rewritten");
        checks.add("corrupt_shard_preserved");
    }
}
