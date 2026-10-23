package io.github.dirtyredz.herbalwitch.companions.animals.persistence;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureService;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureSavedData;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.platform.persistence.SnapshotProbe;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Runs every scenario's real capture/release through {@link CaptureService} and stops it at its
 * ledger boundary: the probe copies the ledger file exactly as it is on disk at that instant, then
 * throws an {@link Error}, so no production {@code catch} runs and nothing further is written, as if
 * the process died there. The evidence also keeps the animal as its chunk would hold it (the source
 * before capture, the inserted incarnation during release) and the player's held item.
 */
final class CaptureRestartPrepare {
    /** Unwinds the stopped operation without entering any production RuntimeException handler. */
    static final class Stop extends Error {
        Stop() {
            super("capture restart boundary reached", null, false, false);
        }
    }

    private final GameTestHelper helper;
    private final Path evidence;
    private final Deque<CaptureRestartScenario> pending = new ArrayDeque<>(java.util.List.of(CaptureRestartScenario.values()));
    private int stopped;

    CaptureRestartPrepare(GameTestHelper helper, Path evidence) {
        this.helper = helper;
        this.evidence = evidence;
    }

    void start() {
        floor(helper);
        next();
    }

    private void next() {
        CaptureRestartScenario scenario = pending.poll();
        if (scenario == null) {
            CaptureRestartReport.write(evidence.resolve("prepared.json"), CaptureRestartReport.prepared(CaptureRestartScenario.values().length, stopped));
            helper.succeed();
            return;
        }
        ServerPlayer player = AnimalFixtures.realPlayer(helper, scenario.player());
        Mob cow = AnimalFixtures.animal(helper, EntityType.COW);
        AnimalFixtures.relationship(cow, player.getUUID(), 80, 4);
        cow.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 2400));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CHARM.get()));
        CompoundTag meta = new CompoundTag();
        meta.putUUID("animal", cow.getUUID());
        meta.put("source", save(cow));
        AnimalFixtures.afterTransportInterval(helper, () -> {
            if (scenario.op == CaptureRestartScenario.Op.CAPTURE) {
                run(scenario, player, cow.getUUID(), meta, () -> CaptureService.capture(player, cow, InteractionHand.MAIN_HAND));
                AnimalFixtures.afterTransportInterval(helper, this::next);
                return;
            }
            check(CaptureService.capture(player, cow, InteractionHand.MAIN_HAND) == null, scenario + ": capture before release failed");
            meta.put("token", player.getMainHandItem().save(new CompoundTag()));
            AnimalFixtures.afterTransportInterval(helper, () -> {
                BlockPos clicked = helper.absolutePos(new BlockPos(5, 0, 5));
                run(scenario, player, cow.getUUID(), meta, () -> CaptureService.release(player, InteractionHand.MAIN_HAND, clicked, Direction.UP));
                AnimalFixtures.afterTransportInterval(helper, this::next);
            });
        });
    }

    private void run(CaptureRestartScenario scenario, ServerPlayer player, UUID animal, CompoundTag meta, java.util.function.Supplier<String> operation) {
        Path dir = evidence.resolve(scenario.dir());
        int[] writes = {0};
        boolean[] reached = {false};
        try (var probe = new SnapshotProbe((stage, file) -> {
            if (!file.getFileName().toString().equals(CaptureSavedData.NAME + ".dat")) return;
            if (stage == VerifiedSnapshotWriter.Stage.BEFORE_WRITE) writes[0]++;
            if (scenario.write == writes[0] && scenario.stage == stage) {
                reached[0] = true;
                record(dir, file, player, animal, meta);
                throw new Stop();
            }
        })) {
            String result = operation.get();
            check(scenario.write == 0, scenario + ": operation finished without reaching its boundary (" + result + ")");
            check(result == null, scenario + ": operation failed: " + result);
        } catch (Stop stop) {
            stopped++;
        }
        if (scenario.write == 0) {
            // A completed release is compacted in memory; the ordinary save persists that tombstone.
            if (scenario.op == CaptureRestartScenario.Op.RELEASE) check(CaptureSavedData.get(player.server).checkpoint(), scenario + ": compacting save failed");
            record(dir, player.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").resolve(CaptureSavedData.NAME + ".dat"), player, animal, meta);
        } else {
            check(reached[0], scenario + ": boundary " + scenario.write + "/" + scenario.stage + " never reached");
        }
        // Whatever this process still holds in memory is not evidence; clear the plot for the next scenario.
        Entity live = helper.getLevel().getEntity(animal);
        if (live != null) live.discard();
    }

    private void record(Path dir, Path ledger, ServerPlayer player, UUID animal, CompoundTag meta) {
        try {
            Files.createDirectories(dir);
            if (Files.isRegularFile(ledger)) Files.copy(ledger, dir.resolve("ledger.dat"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            CompoundTag copy = meta.copy();
            copy.putString("player", playerName(dir));
            copy.put("hand", player.getMainHandItem().save(new CompoundTag()));
            Entity live = helper.getLevel().getEntity(animal);
            if (live instanceof Mob mob && !mob.isRemoved()) copy.put("inserted", save(mob));
            NbtIo.writeCompressed(copy, dir.resolve("case.nbt").toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String playerName(Path dir) {
        return CaptureRestartScenario.valueOf(dir.getFileName().toString().toUpperCase(java.util.Locale.ROOT)).player();
    }

    private static CompoundTag save(Mob mob) {
        CompoundTag tag = new CompoundTag();
        mob.save(tag);
        return tag;
    }
}
