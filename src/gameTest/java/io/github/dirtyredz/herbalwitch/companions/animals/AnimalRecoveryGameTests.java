package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord.Phase;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.payload.VanillaCaptureAdapters;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalRecoveryGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_recovery", timeoutTicks = 100)
    public static void reissueRotatesTokenAndRecoveryRetiresOldLiveIncarnationWithoutSpawning(GameTestHelper h) {
        AnimalFixtures.afterTransportInterval(h, () -> {
            floor(h); var player = AnimalFixtures.player(h, "m8_recovery"); var cow = AnimalFixtures.animal(h, EntityType.COW);
            var data = CaptureSavedData.get(player.server);
            var record = new CaptureRecord(cow.getUUID(), 1, UUID.randomUUID(), Phase.CAPTURE_PREPARED, player.getUUID(), UUID.randomUUID(),
                    "minecraft:cow", 1, "Cow", "source", "", VanillaCaptureAdapters.snapshot(cow)).phase(Phase.STORED, "");
            check(data.commit(record), "fixture checkpoint failed");
            for (int i = 0; i < player.getInventory().items.size(); i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
            check("inventory".equals(CaptureService.issue(player, cow.getUUID(), false)) && data.token(record.token()).isPresent(), "full inventory rotated token");
            player.getInventory().setItem(0, ItemStack.EMPTY);
            String denial = CaptureService.issue(player, cow.getUUID(), false);
            check(denial == null, "reissue refused: " + denial);
            check(data.token(record.token()).isEmpty(), "reissue left old token active");
            var reissued = data.record(cow.getUUID()).orElseThrow();
            check(reissued.generation() == 1 && CaptureToken.read(player.getInventory().getItem(0)).equals(reissued.token()), "reissue changed generation or lost item");
            check(data.commit(reissued.phase(Phase.RELEASE_PREPARED, "destination").phase(Phase.QUARANTINED, "destination")), "quarantine fixture failed");
            player.getInventory().setItem(1, ItemStack.EMPTY);
            AnimalFixtures.afterTransportInterval(h, () -> {
                String recoveryDenial = CaptureService.issue(player, cow.getUUID(), true);
                check(recoveryDenial == null, "recovery refused: " + recoveryDenial);
                var recovered = data.record(cow.getUUID()).orElseThrow();
                check(recovered.generation() == 2 && recovered.phase() == Phase.STORED && recovered.captor().equals(player.getUUID()), "recovery transferred or failed to advance");
                check(cow.isRemoved() && h.getLevel().getEntity(cow.getUUID()) == null && data.token(reissued.token()).isEmpty(), "recovery left old incarnation or spawned directly");
                h.succeed();
            });
        });
    }
}
