package io.github.dirtyredz.herbalwitch.companions.familiars.recovery;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.FamiliarEquipmentInventory;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarRecoveryGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_admin_recovery", timeoutTicks = 100)
    public static void administrativeRecoveryRetiresWithoutSpawning(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "admin_recovery_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server); long generation = b.record().generation();
            var payload = io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters.snapshot(b.mob()).orElseThrow();
            store.update(b.record().transition(FamiliarRecord.Phase.QUARANTINED, generation, UUID.randomUUID(), payload), true);
            h.runAfterDelay(6, () -> {
                check(FamiliarRecovery.recover(b.owner().server, b.bond()), "retained inspectable payload was refused");
                check(b.mob().isRemoved() && b.record().phase() == FamiliarRecord.Phase.INCAPACITATED
                        && b.record().generation() > generation && !b.record().payload().isEmpty(), "admin recovery did not retire generation");
                check(h.getLevel().getEntity(b.record().creature()) == null && WitchStatusService.energyUnits(b.owner()) == 7500,
                        "admin recovery spawned or spent"); h.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_revival_refusal", timeoutTicks = 100)
    public static void refusedAdmissionRetainsPayloadAndDoesNotRetry(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "revival_refusal_" + UUID.randomUUID(), b -> {
            b.mob().hurt(b.mob().damageSources().generic(), 1000);
            check(b.record().phase() == FamiliarRecord.Phase.INCAPACITATED, "refusal fixture not incapacitated");
            h.runAfterDelay(6, () -> {
                java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> refuse = event -> {
                    if (event.getEntity().getUUID().equals(b.record().creature())) event.setCanceled(true);
                };
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, false,
                        net.minecraftforge.event.entity.EntityJoinLevelEvent.class, refuse);
                try {
                    check(!FamiliarRevival.revive(b.owner(), b.bond(), h.absolutePos(new BlockPos(4,1,4)), AltarOffering.none()), "canceled admission succeeded");
                } finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(refuse); }
                check(b.record().phase() == FamiliarRecord.Phase.QUARANTINED && !b.record().payload().isEmpty(), "refusal lost retained state");
                check(h.getLevel().getEntity(b.record().creature()) == null, "refusal left a live candidate");
                check(WitchStatusService.energyUnits(b.owner()) == 7500, "refused insertion charged energy");
                check(!FamiliarRecovery.recover(b.owner().server, b.bond()), "administrative recovery released unresolved receipt"); h.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_retreat", timeoutTicks = 220)
    public static void injuredPassiveFamiliarRetreatsToTheLoadedOwner(GameTestHelper h) {
        FamiliarFixtures.bound(h, "wolf", "retreat_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server);
            store.update(b.record().change(s -> s.putString("command", "passive")), true);
            // Keep both ends of the six-block retreat inside the declared nine-block floor.
            b.mob().moveTo(h.absolutePos(new BlockPos(2,1,5)).getCenter());
            b.owner().moveTo(h.absolutePos(new BlockPos(8,1,5)).getCenter());
            b.mob().setHealth(2); double before = b.mob().distanceToSqr(b.owner());
            h.succeedWhen(() -> check(b.mob().isAlive() && b.mob().distanceToSqr(b.owner()) < before / 2
                    && b.mob().getTarget() == null, "injured familiar did not retreat toward owner"));
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_revival", timeoutTicks = 100)
    public static void lethalDamageRetainsGearAndRevivesOnceForEnergy(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "revival_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server);
            store.update(FamiliarEquipmentInventory.equipment(b.record(), ItemStack.EMPTY, new ItemStack(FamiliarItems.gear("cat_bell"))).change(s -> {
                s.putInt("reserve", 700); var buffs = new net.minecraft.nbt.CompoundTag(); buffs.putInt("guardian", 3000); s.put("buffs", buffs);
            }), true);
            long generation = b.record().generation(); var box = b.mob().getBoundingBox().inflate(3);
            b.mob().hurt(b.mob().damageSources().generic(), 1000);
            check(b.mob().isRemoved() && b.record().phase() == FamiliarRecord.Phase.INCAPACITATED, "lethal damage did not incapacitate");
            check(!b.record().payload().isEmpty() && !FamiliarEquipmentInventory.item(b.record(), 1).isEmpty(), "recovery lost payload or gear");
            check(h.getLevel().getEntitiesOfClass(ItemEntity.class, box).isEmpty(), "incapacitation dropped inventory");
            h.runAfterDelay(6, () -> {
                var altar = h.absolutePos(new BlockPos(4, 1, 4));
                check(FamiliarRevival.revive(b.owner(), b.bond(), altar, AltarOffering.none()), "revival refused");
                var alive = (Mob) h.getLevel().getEntity(b.record().creature());
                check(alive != null && alive != b.mob() && alive.getHealth() == alive.getMaxHealth() / 2, "revival health or identity wrong");
                check(b.record().generation() > generation && b.record().phase() == FamiliarRecord.Phase.ACTIVE && b.record().payload().isEmpty(), "revival not compacted");
                check(b.record().state().getCompound("buffs").isEmpty() && b.record().state().getInt("reserve") == 700, "revival reset reserve or retained buffs");
                check(WitchStatusService.energyUnits(b.owner()) == 2500, "revival did not cost fifty energy");
                check(!FamiliarRevival.revive(b.owner(), b.bond(), altar, AltarOffering.none()), "revival replayed"); h.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_missing", timeoutTicks = 100)
    public static void administrativeRemovalDoesNotAuthorizeRevival(GameTestHelper h) {
        FamiliarFixtures.bound(h, "wolf", "missing_" + UUID.randomUUID(), b -> {
            b.mob().kill();
            check(b.record().phase() == FamiliarRecord.Phase.ACTIVE && b.record().payload().isEmpty(), "administrative removal manufactured recovery");
            check(!FamiliarRevival.revive(b.owner(), b.bond(), h.absolutePos(new BlockPos(4,1,4)), AltarOffering.none()), "missing entity revived");
            check(!FamiliarRecovery.recover(b.owner().server, b.bond()), "administrator invented a payload"); h.succeed();
        });
    }
}
