package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.*;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarActionCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarProgressGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_level_recall", timeoutTicks = 100)
    public static void recallPreservesHealthAboveTheNativeMaximum(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "level_recall_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server); store.update(b.record().change(s -> s.putInt("xp", 350)), true);
            FamiliarBondProgress.project(b.mob(), b.record()); b.mob().setHealth(b.mob().getMaxHealth()); float health = b.mob().getHealth();
            b.owner().getInventory().setItem(0, new ItemStack(io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems.BOOK.get()));
            h.runAfterDelay(6, () -> {
                check(io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarRecall.recall(b.owner(), b.bond()).equals("recalled"), "level recall refused");
                var mob = (net.minecraft.world.entity.Mob) h.getLevel().getEntity(b.record().creature());
                check(mob != null && mob.getHealth() == health && mob.getMaxHealth() == health, "recall clamped health before bond projection"); h.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_capped_feed", timeoutTicks = 100)
    public static void maximumTrustCareConsumesOnceAndKeepsTheM8Cooldown(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "progress_feed_" + UUID.randomUUID(), b -> {
            b.owner().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.FISH), 2));
            check(AnimalCareService.feed(b.owner(), b.mob(), InteractionHand.MAIN_HAND, Bait.FISH) == AnimalCareAccess.Reason.READY, "capped owner feed refused");
            check(b.record().state().getInt("xp") == 2 && b.owner().getMainHandItem().getCount() == 1, "feed did not consume/reward exactly once");
            check(AnimalTrustService.relationship(b.mob(), b.owner().getUUID()).trust() == 100, "capped feed invented trust");
            check(AnimalCareService.feed(b.owner(), b.mob(), InteractionHand.MAIN_HAND, Bait.FISH) == AnimalCareAccess.Reason.COOLDOWN, "capped feed bypassed M8 clock");
            check(b.record().state().getInt("xp") == 2 && b.owner().getMainHandItem().getCount() == 1, "rejected feed consumed or rewarded"); h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_upgrades", timeoutTicks = 120)
    public static void paidUpgradesDoNotStackHealOrBypassHarmonyCooldown(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "upgrades_" + UUID.randomUUID(), b -> {
            var altar = h.absolutePos(new BlockPos(4,1,4)); var store = FamiliarStore.get(b.owner().server);
            check(!FamiliarUpgrades.eligible(b.owner(), b.bond(), altar, FamiliarActionCatalog.HEARTWOOD), "level one obtained heartwood");
            store.update(b.record().change(s -> s.putInt("xp", 100)), true); FamiliarBondProgress.project(b.mob(), b.record());
            b.mob().setHealth(7); float before = b.mob().getMaxHealth();
            h.runAfterDelay(6, () -> {
                check(FamiliarUpgrades.apply(b.owner(), b.bond(), altar, FamiliarActionCatalog.HEARTWOOD, AltarOffering.none()), "heartwood refused");
                check(b.mob().getMaxHealth() == before + 4 && b.mob().getHealth() == 7, "heartwood healed or wrong maximum");
                check(!FamiliarUpgrades.apply(b.owner(), b.bond(), altar, FamiliarActionCatalog.HEARTWOOD, AltarOffering.none()), "heartwood repeated");
                h.runAfterDelay(6, () -> {
                    check(FamiliarUpgrades.apply(b.owner(), b.bond(), altar, FamiliarActionCatalog.SPIRIT_WELL, AltarOffering.none()), "spirit well refused");
                    check(b.record().state().getBoolean("spiritWell") && b.record().state().getInt("reserve") == 0, "spirit well refilled reserve");
                    h.runAfterDelay(6, () -> {
                        check(FamiliarUpgrades.apply(b.owner(), b.bond(), altar, FamiliarActionCatalog.HARMONY, AltarOffering.none()), "harmony refused");
                        check(b.record().state().getInt("xp") == 110 && !FamiliarUpgrades.eligible(b.owner(), b.bond(), altar, FamiliarActionCatalog.HARMONY), "harmony XP or gate wrong");
                        check(WitchStatusService.energyUnits(b.owner()) == 1500, "upgrade energy total wrong"); h.succeed();
                    });
                });
            });
        });
    }
}
