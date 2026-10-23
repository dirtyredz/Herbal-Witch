package io.github.dirtyredz.herbalwitch.qualification.performance;

import com.google.gson.GsonBuilder;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatEntity;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Runs the frozen measurement protocol on the server thread, one tick at a time: each phase builds its
 * workload, warms up for {@link #WARM} ticks, then measures {@link #WINDOWS} consecutive windows of
 * {@link #WINDOW} ticks. The control phase measures the same JVM and world before any workload exists.
 */
final class PerformanceRun {
    static final int WARM = 600;
    static final int WINDOWS = 3;
    static final int WINDOW = 800;
    static final int RECORDS = 4096;
    static final int SALES = 32;
    private static final BlockPos IDLE = new BlockPos(4096, -60, 4096), BUSY = new BlockPos(6144, -60, 6144),
            SATURATION = new BlockPos(8192, -60, 8192), CHURN = new BlockPos(10240, -60, 10240),
            VANILLA = new BlockPos(12288, -60, 12288), PART = new BlockPos(14336, -60, 14336);

    private record Phase(String name, Runnable setup, int windows) {
    }

    private final GameTestHelper helper;
    private final Path evidence;
    private final Workloads workloads;
    private final TickSampler sampler;
    private final Deque<Phase> phases = new ArrayDeque<>();
    private final Map<String, Object> report = new LinkedHashMap<>();
    private final Map<String, Object> results = new LinkedHashMap<>();
    private Phase phase;
    private int warm;
    private List<Map<String, Object>> windows;
    private OnlinePlayers.Registration online;

    PerformanceRun(GameTestHelper helper, Path evidence) {
        this.helper = helper;
        this.evidence = evidence;
        this.workloads = new Workloads(helper.getLevel());
        this.sampler = new TickSampler(helper.getLevel().getServer());
        report.put("process", ProcessHandle.current().pid());
        report.put("protocol", Map.of("warmTicks", WARM, "windows", WINDOWS, "windowTicks", WINDOW));
        report.put("jvm", Map.of("maxHeapMb", Runtime.getRuntime().maxMemory() >> 20, "processors", Runtime.getRuntime().availableProcessors(),
                "java", System.getProperty("java.version")));
        report.put("phases", results);
    }

    void start() {
        phases.add(new Phase("control", () -> { }, WINDOWS));
        phases.add(new Phase("idle_cottage", () -> report.put("idleBlockEntitiesPlaced", workloads.idleCottage(IDLE)), WINDOWS));
        phases.add(new Phase("settle", workloads::release, 0));
        // The matched control: the busy settlement's counts with vanilla stand-ins.
        phases.add(new Phase("busy_vanilla_control", () -> report.put("vanillaBlockEntitiesPlaced", workloads.vanillaSettlement(VANILLA)), WINDOWS));
        phases.add(new Phase("settle_after_vanilla", workloads::release, 0));
        // Attribution: each part of the busy settlement alone (informational, not gated).
        for (var part : Workloads.Part.values()) {
            String tag = "p" + part.ordinal();
            phases.add(new Phase("busy_part_" + part.name().toLowerCase(java.util.Locale.ROOT), () -> {
                workloads.busySettlement(PART.offset(part.ordinal() * 2048, 0, 0), tag, java.util.EnumSet.of(part));
                if (part == Workloads.Part.FAMILIARS) online = OnlinePlayers.register(List.copyOf(workloads.owners.subList(workloads.owners.size() - Workloads.OWNERS, workloads.owners.size())));
            }, WINDOWS));
            phases.add(new Phase("settle_after_" + tag, () -> {
                if (online != null) { online.close(); online = null; }
                workloads.release();
            }, 0));
        }
        phases.add(new Phase("busy_settlement", () -> {
            int before = workloads.owners.size();
            report.put("busyBlockEntitiesPlaced", workloads.busySettlement(BUSY, "b", java.util.EnumSet.allOf(Workloads.Part.class)));
            online = OnlinePlayers.register(List.copyOf(workloads.owners.subList(before, before + Workloads.OWNERS)));
        }, WINDOWS));
        phases.add(new Phase("saturation", () -> {
            int before = workloads.owners.size();
            report.put("saturationBlockEntitiesPlaced", workloads.busySettlement(SATURATION, "c", java.util.EnumSet.allOf(Workloads.Part.class)));
            var both = new java.util.ArrayList<>(workloads.owners.subList(before - Workloads.OWNERS, before + Workloads.OWNERS));
            online.close();
            online = OnlinePlayers.register(List.copyOf(both));
        }, WINDOWS));
        phases.add(new Phase("settle_after_saturation", () -> {
            online.close();
            workloads.release();
        }, 0));
        phases.add(new Phase("churn", this::churn, 0));
        phases.add(new Phase("large_persistence", this::persistence, WINDOWS));
        phases.add(new Phase("commerce_checkpoints", this::commerce, 0));
        // Attribution runs (-PperformanceOnly=a,b) keep the control, the named phases and every settle step.
        var only = java.util.Set.of(System.getProperty("herbalwitch.performance.only", "").split(","));
        if (!only.equals(java.util.Set.of(""))) phases.removeIf(p -> !p.name.equals("control") && !p.name.startsWith("settle") && !only.contains(p.name));
        next();
    }

    private void next() {
        phase = phases.poll();
        if (phase == null) {
            write();
            helper.succeed();
            return;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        results.put(phase.name, result);
        phase.setup.run();
        result.put("censusStart", Census.take(helper.getLevel()));
        windows = new ArrayList<>();
        result.put("windows", windows);
        warm = phase.windows == 0 ? 200 : WARM;
        helper.runAfterDelay(1, this::tick);
    }

    private void tick() {
        if (warm > 0) {
            if (--warm == 0) sampler.start();
        } else {
            sampler.sample();
            if (sampler.size() >= WINDOW) {
                Map<String, Object> stats = sampler.stats();
                stats.put("census", Census.take(helper.getLevel()));
                windows.add(stats);
                sampler.start();
            }
        }
        if (warm == 0 && windows.size() >= phase.windows) {
            ((Map<String, Object>) results.get(phase.name)).put("censusEnd", Census.take(helper.getLevel()));
            next();
            return;
        }
        helper.runAfterDelay(1, this::tick);
    }

    /** Lifecycle churn: place a seat, sit, stand, break it; open and close a cabinet; then count what is left. */
    private void churn() {
        var level = helper.getLevel();
        workloads.plot(CHURN);
        var seat = FurnitureCatalog.withBehavior(FurnitureBehavior.SEATING).stream().filter(s -> s.mount().cells().size() == 1).findFirst().orElseThrow();
        var cabinet = FurnitureCatalog.withBehavior(FurnitureBehavior.STORAGE).stream().filter(s -> s.mount().cells().size() == 1).findFirst().orElseThrow();
        var sitter = workloads.player("perf_churn_sitter", CHURN.offset(2, 0, 1));
        sitter.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos at = CHURN.offset(2, 0, 2), box = CHURN.offset(4, 0, 2);
        int sat = 0, opened = 0;
        for (int cycle = 0; cycle < 100; cycle++) {
            level.setBlock(at, DecorBlocks.block(seat).defaultBlockState(), Block.UPDATE_ALL);
            var hit = new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false);
            if (level.getBlockState(at).use(level, sitter, InteractionHand.MAIN_HAND, hit).consumesAction() && sitter.isPassenger()) sat++;
            sitter.stopRiding();
            level.destroyBlock(at, false);
            level.setBlock(box, DecorBlocks.block(cabinet).defaultBlockState(), Block.UPDATE_ALL);
            level.getBlockState(box).use(level, sitter, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(box), Direction.UP, box, false));
            if (sitter.containerMenu != sitter.inventoryMenu) opened++;
            sitter.closeContainer();
            level.destroyBlock(box, false);
        }
        var area = new AABB(CHURN.offset(-8, -2, -8), CHURN.offset(56, 8, 56));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cycles", 100);
        out.put("seated", sat);
        out.put("menusOpened", opened);
        out.put("seatEntitiesLeft", level.getEntitiesOfClass(SeatEntity.class, area).size());
        out.put("itemsLeft", level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).size());
        report.put("churn", out);
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).forEach(net.minecraft.world.entity.Entity::discard);
        workloads.release();
    }

    /** 4,096 bonds for one owner, each a verified durable create; then roster pages and steady ticks. */
    private void persistence() {
        var store = FamiliarStore.get(helper.getLevel().getServer());
        check(store.ready(), "familiar store not ready");
        var owner = workloads.player("perf_roster_owner", BlockPos.ZERO);
        long[] creates = new long[RECORDS];
        for (int i = 0; i < RECORDS; i++) {
            var record = workloads.record(owner, new BlockPos(20_000_000, 80, 20_000_000));
            long t0 = System.nanoTime();
            check(store.create(record), "persistence fixture record " + i);
            creates[i] = System.nanoTime() - t0;
        }
        long[] pages = new long[64];
        var cursor = (java.util.UUID) null;
        for (int i = 0; i < pages.length; i++) {
            long t0 = System.nanoTime();
            var page = store.roster().page(owner.getUUID(), cursor, 32);
            pages[i] = System.nanoTime() - t0;
            cursor = page.isEmpty() ? null : page.get(page.size() - 1);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("records", RECORDS);
        out.put("createMs", latency(creates));
        out.put("pageMs", latency(pages));
        report.put("largePersistence", out);
    }

    /** Commerce: each sale forces and verifies the buyer's player file and the shop shard before committing. */
    private void commerce() {
        var h = helper;
        ShopFixtures.floor(h);
        var shop = ShopFixtures.shop(h, ShopFixtures.witch(h, "perf_shop_owner"), new BlockPos(4, 1, 6));
        var offer = ShopFixtures.selling(h, shop, SALES, 1);
        long[] sales = new long[SALES];
        for (int i = 0; i < SALES; i++) {
            var buyer = ShopFixtures.player(h, "perf_buyer_" + i);
            ShopFixtures.emeralds(buyer, 1);
            long revision = ShopFixtures.record(h, shop).offer(offer).orElseThrow().revision();
            long t0 = System.nanoTime();
            var result = ShopTrades.purchase(buyer, ShopFixtures.store(h), shop.id(), offer, revision, null);
            sales[i] = System.nanoTime() - t0;
            check(result.success(), "performance sale " + i + " refused: " + result.code());
        }
        report.put("commerceSaleMs", latency(sales));
    }

    static Map<String, Object> latency(long[] nanos) {
        long[] sorted = Arrays.stream(nanos).sorted().toArray();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", sorted.length);
        out.put("p50", Math.round(sorted[sorted.length / 2] / 10_000.0) / 100.0);
        out.put("p95", Math.round(sorted[(int) Math.ceil(.95 * sorted.length) - 1] / 10_000.0) / 100.0);
        out.put("max", Math.round(sorted[sorted.length - 1] / 10_000.0) / 100.0);
        return out;
    }

    private void write() {
        try {
            Files.createDirectories(evidence);
            Files.writeString(evidence.resolve("performance.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
