package io.github.dirtyredz.herbalwitch.companions.animals.persistence;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureService;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureReconciliation;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord.Phase;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureSavedData;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

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
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * A second JVM restores each scenario's ledger bytes into the world, lets the production loader
 * reconcile them (as a restart does) and then drives the production paths: the stale chunk copies of
 * the animal try to rejoin, held tokens are used, and operators recover or reissue. Every scenario
 * must end with exactly one animal, or none and a recoverable payload, never two.
 */
final class CaptureRestartVerify {
    private final GameTestHelper helper;
    private final Path evidence;
    private final Deque<Runnable> steps = new ArrayDeque<>();
    private final List<Map<String, Object>> results = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();

    CaptureRestartVerify(GameTestHelper helper, Path evidence) {
        this.helper = helper;
        this.evidence = evidence;
    }

    void start() {
        floor(helper);
        for (CaptureRestartScenario scenario : CaptureRestartScenario.values()) steps.add(() -> scenario(scenario));
        steps.add(this::corruptLedger);
        next();
    }

    private void next() {
        Runnable step = steps.poll();
        if (step == null) {
            CaptureRestartReport.write(evidence.resolve("verified.json"), CaptureRestartReport.verified(results, failures));
            if (failures.isEmpty()) helper.succeed();
            else helper.fail(String.join("; ", failures));
            return;
        }
        try {
            step.run();
        } catch (RuntimeException failure) {
            failures.add("step failed: " + failure);
            next();
        }
    }

    /** Waits out the ledger's transaction interval, then continues. */
    private void later(Runnable action) {
        AnimalFixtures.afterTransportInterval(helper, () -> {
            try {
                action.run();
            } catch (RuntimeException failure) {
                failures.add("step failed: " + failure);
                next();
            }
        });
    }

    private void scenario(CaptureRestartScenario scenario) {
        Path dir = evidence.resolve(scenario.dir());
        CompoundTag meta = read(dir.resolve("case.nbt"));
        CaptureSavedData data = restart(Files.isRegularFile(dir.resolve("ledger.dat")) ? bytes(dir.resolve("ledger.dat")) : null);
        UUID animal = meta.getUUID("animal");
        ServerPlayer player = AnimalFixtures.realPlayer(helper, scenario.player());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", scenario.dir());
        result.put("expect", scenario.expect.name());
        var record = data.record(animal).orElse(null);
        result.put("phase", record == null ? "absent" : record.phase().name());
        results.add(result);
        String name = scenario.dir();
        switch (scenario.expect) {
            case ABSENT -> {
                expect(record == null, name, "an undurable capture left a record");
                expect(rejoin(meta.getCompound("source")), name, "the untouched source animal could not return");
                expect(count(animal) == 1, name, "source animal not exactly once");
                clear(animal);
                next();
            }
            case QUARANTINED -> {
                expect(record != null && record.phase() == Phase.QUARANTINED && record.storedPayload(), name, "interrupted transfer not quarantined with its payload");
                expect(!rejoin(meta.getCompound("source")), name, "a stale source incarnation rejoined");
                if (meta.contains("inserted")) expect(!rejoin(meta.getCompound("inserted")), name, "an interrupted release incarnation rejoined");
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.of(meta.getCompound("hand")));
                if (player.getMainHandItem().is(AnimalItems.FILLED_CHARM.get())) {
                    expect("token".equals(release(player)), name, "a quarantined token released");
                }
                expect(count(animal) == 0, name, "a quarantined animal is in the world");
                long generation = record == null ? 0 : record.generation();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                expect(CaptureService.issue(player, animal, true) == null, name, "operator recovery refused");
                later(() -> {
                    hold(player);
                    expect(release(player) == null, name, "recovered token did not release");
                    expect(count(animal) == 1, name, "recovery did not yield exactly one animal");
                    expect(generationOf(animal) == generation + 1, name, "recovery did not advance the generation");
                    result.put("recovered", true);
                    clear(animal);
                    later(this::next);
                });
            }
            case STORED_REISSUE -> {
                expect(record != null && record.phase() == Phase.STORED, name, "stored capture not stored");
                expect(!rejoin(meta.getCompound("source")), name, "the captured source rejoined");
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                expect(CaptureService.issue(player, animal, false) == null, name, "operator reissue refused");
                later(() -> {
                    hold(player);
                    expect(release(player) == null, name, "reissued token did not release");
                    expect(count(animal) == 1, name, "reissue did not yield exactly one animal");
                    clear(animal);
                    later(this::next);
                });
            }
            case STORED_TOKEN -> {
                expect(record != null && record.phase() == Phase.STORED, name, "stored capture not stored");
                expect(!rejoin(meta.getCompound("source")), name, "the captured source rejoined");
                ItemStack token = ItemStack.of(meta.contains("token") ? meta.getCompound("token") : meta.getCompound("hand"));
                player.setItemInHand(InteractionHand.MAIN_HAND, token.copy());
                expect(release(player) == null, name, "the held token did not release after restart");
                expect(count(animal) == 1, name, "release did not yield exactly one animal");
                later(() -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, token.copy());
                    expect("token".equals(release(player)), name, "a copied token released twice");
                    expect(count(animal) == 1, name, "a copied token duplicated the animal");
                    clear(animal);
                    later(this::next);
                });
            }
            case RELEASED -> {
                expect(record != null && record.phase() == Phase.RELEASED && !record.storedPayload(), name, "completed release not a compact tombstone");
                expect(!rejoin(meta.getCompound("source")), name, "the pre-capture source rejoined");
                expect(meta.contains("inserted") && rejoin(meta.getCompound("inserted")), name, "the released animal could not return from its chunk");
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.of(meta.getCompound("token")));
                expect("token".equals(release(player)), name, "the spent token released again");
                expect("token".equals(CaptureService.issue(player, animal, true)), name, "a completed release was recoverable");
                expect(count(animal) == 1, name, "released animal not exactly once");
                clear(animal);
                next();
            }
        }
    }

    /** An unreadable ledger stays byte for byte, refuses transport, and leaves ordinary animals alone. */
    private void corruptLedger() {
        byte[] corrupt = new byte[96];
        new java.util.Random(12).nextBytes(corrupt);
        CaptureSavedData data = restart(corrupt);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "corrupt_ledger");
        results.add(result);
        expect(!data.writable() && !data.readable(), "corrupt_ledger", "a corrupt ledger was accepted");
        ServerPlayer player = AnimalFixtures.realPlayer(helper, "capture_restart_corrupt");
        Mob cow = AnimalFixtures.animal(helper, EntityType.COW);
        expect(helper.getLevel().getEntity(cow.getUUID()) == cow, "corrupt_ledger", "a never-captured cow was refused");
        AnimalFixtures.relationship(cow, player.getUUID(), 80, 4);
        cow.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 200));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CHARM.get()));
        expect("persistence".equals(CaptureService.capture(player, cow, InteractionHand.MAIN_HAND)), "corrupt_ledger", "capture ran on a corrupt ledger");
        CompoundTag released = read(evidence.resolve(CaptureRestartScenario.RELEASE_COMPLETE.dir()).resolve("case.nbt")).getCompound("inserted");
        expect(!rejoin(released), "corrupt_ledger", "a transported incarnation joined without its ledger");
        data.setDirty();
        data.save(ledgerPath().toFile());
        expect(Arrays.equals(corrupt, bytes(ledgerPath())), "corrupt_ledger", "the corrupt ledger was rewritten");
        cow.discard();
        result.put("preserved", true);
        next();
    }

    private CaptureSavedData restart(byte[] ledger) {
        try {
            Files.createDirectories(ledgerPath().getParent());
            if (ledger == null) Files.deleteIfExists(ledgerPath());
            else Files.write(ledgerPath(), ledger);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        CaptureSavedData.reset();
        return CaptureSavedData.get(helper.getLevel().getServer());
    }

    private Path ledgerPath() {
        return helper.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(CaptureSavedData.NAME + ".dat");
    }

    /** As if the chunk holding this saved animal loaded: true when the world accepted it. */
    private boolean rejoin(CompoundTag saved) {
        Entity entity = EntityType.loadEntityRecursive(saved, helper.getLevel(), e -> e);
        if (entity == null) return false;
        entity.moveTo(helper.absolutePos(new BlockPos(2, 1, 6)).getCenter());
        boolean added = helper.getLevel().addFreshEntity(entity);
        if (!added) entity.discard();
        return added && helper.getLevel().getEntity(entity.getUUID()) == entity;
    }

    private String release(ServerPlayer player) {
        return CaptureService.release(player, InteractionHand.MAIN_HAND, helper.absolutePos(new BlockPos(5, 0, 5)), Direction.UP);
    }

    /** Moves the first filled charm the operator delivered into the main hand. */
    private static void hold(ServerPlayer player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(AnimalItems.FILLED_CHARM.get())) {
                player.setItemInHand(InteractionHand.MAIN_HAND, inventory.removeItemNoUpdate(slot));
                return;
            }
        }
    }

    private int count(UUID animal) {
        return helper.getLevel().getEntity(animal) == null ? 0 : 1;
    }

    private long generationOf(UUID animal) {
        return helper.getLevel().getEntity(animal) instanceof Mob mob ? CaptureReconciliation.generation(mob) : -1;
    }

    private void clear(UUID animal) {
        Entity live = helper.getLevel().getEntity(animal);
        if (live != null) live.discard();
    }

    private void expect(boolean condition, String scenario, String message) {
        if (!condition) failures.add(scenario + ": " + message);
    }

    private static CompoundTag read(Path file) {
        try {
            return NbtIo.readCompressed(file.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] bytes(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
