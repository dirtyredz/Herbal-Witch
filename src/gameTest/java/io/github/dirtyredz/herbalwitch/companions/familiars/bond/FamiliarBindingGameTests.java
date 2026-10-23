package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.*;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import io.github.dirtyredz.herbalwitch.companions.familiars.ritual.BindingAction;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.fixture.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class FamiliarBindingGameTests {
    private record Setup(ServerPlayer owner, Cat cat) {}
    private static Setup setup(GameTestHelper helper, String name) {
        WitchFixtures.altar(helper); MagicFixtures.circle(helper);
        var player = AnimalFixtures.realPlayer(helper, name);
        player.moveTo(helper.absolutePos(WitchFixtures.FRONT).getCenter());
        check(WitchStatusService.convert(player, UUID.randomUUID()), "witch setup failed");
        var cat = helper.spawn(EntityType.CAT, new BlockPos(5, 1, 2)); cat.setTame(true); cat.setOwnerUUID(player.getUUID()); cat.setAge(0);
        AnimalFixtures.relationship(cat, player.getUUID(), 100, 5);
        player.getInventory().setItem(0, new ItemStack(AnimalItems.bait(Bait.FISH)));
        player.getInventory().setItem(1, new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE)));
        player.getInventory().setItem(2, new ItemStack(Items.AMETHYST_SHARD));
        return new Setup(player, cat);
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_binding", timeoutTicks = 100)
    public static void altarBindingPreservesCareAndRejectsStaleUnmarkedSource(GameTestHelper helper) {
        var setup = setup(helper, "m9_binding_cat"); var player = setup.owner(); var cat = setup.cat();
        helper.runAfterDelay(6, () -> {
            try (var online = OnlinePlayers.register(List.of(player))) {
                CompoundTag stale = new CompoundTag(); cat.saveWithoutId(stale);
                var menu = WitchFixtures.open(helper, player); check(menu != null, "altar menu did not open");
                String result = BindingAction.begin(player, menu, Optional.of(cat.getUUID())); check(result.equals("familiar.begun"), "binding refused: " + result);
                var session = WitchFixtures.session(player); WitchFixtures.advance(session, 199);
                check(!cat.isRemoved() && WitchStatusService.energyUnits(player) == 10000, "binding completed early");
                WitchFixtures.advance(session, 1);
                var store = FamiliarStore.get(player.server); var record = store.creature(cat.getUUID()).orElseThrow();
                check(record.phase() == FamiliarRecord.Phase.ACTIVE, "binding phase " + record.phase());
                check(cat.isRemoved() && helper.getLevel().getEntity(cat.getUUID()) instanceof Mob, "conversion did not replace source");
                var familiar = (Mob) helper.getLevel().getEntity(cat.getUUID());
                check(familiar.getType() == FamiliarEntities.CAT.get() && FamiliarReconciliation.matches(familiar, record), "wrong representation");
                check(AnimalTrustService.relationship(familiar, player.getUUID()).trust() == 100, "care lost");
                check(WitchStatusService.energyUnits(player) == 7500 && player.getInventory().getItem(0).isEmpty()
                        && player.getInventory().getItem(1).isEmpty() && player.getInventory().getItem(2).isEmpty(), "wrong payment");
                check(record.payload().isEmpty() && !record.state().contains("receipt") && record.command().key().equals("stay"), "unfinished binding receipt");
                var old = EntityType.CAT.create(helper.getLevel()); old.load(stale);
                check(!helper.getLevel().addFreshEntity(old), "stale unmarked source admitted");
                check(store.record(record.bond()).orElseThrow().phase() == FamiliarRecord.Phase.ACTIVE && !familiar.isRemoved(), "stale source harmed current bond");
                helper.succeed();
            }
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_binding_cancel", timeoutTicks = 100)
    public static void lostTrustCancelsSessionWithoutPaymentOrConversion(GameTestHelper helper) {
        var setup = setup(helper, "m9_binding_cancel"); var player = setup.owner();
        helper.runAfterDelay(6, () -> {
            try (var online = OnlinePlayers.register(List.of(player))) {
                var menu = WitchFixtures.open(helper, player);
                check(BindingAction.begin(player, menu, Optional.of(setup.cat().getUUID())).equals("familiar.begun"), "binding refused");
                var session = WitchFixtures.session(player); WitchFixtures.advance(session, 40);
                AnimalFixtures.relationship(setup.cat(), player.getUUID(), 60, 5);
                WitchFixtures.advance(session, 160);
                check(!session.running() && !setup.cat().isRemoved() && WitchStatusService.energyUnits(player) == 10000, "canceled binding changed creature or energy");
                check(player.getInventory().getItem(2).getCount() == 1 && FamiliarStore.get(player.server).creature(setup.cat().getUUID()).isEmpty(), "canceled binding created authority");
                helper.succeed();
            }
        });
    }
}
