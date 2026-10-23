package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.FamiliarWork;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarCommandGameTests {
    private static OnlinePlayers.Registration movement, guarding;
    @AfterBatch(batch = "herbalwitch_familiar_movement") public static void movementCleanup(ServerLevel level) { if (movement != null) { movement.close(); movement = null; } }
    @AfterBatch(batch = "herbalwitch_familiar_guarding") public static void guardCleanup(ServerLevel level) { if (guarding != null) { guarding.close(); guarding = null; } }
    private static Vec3 feet(GameTestHelper h, int x, int z) { return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(x, 1, z))); }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_movement", timeoutTicks = 180)
    public static void realNavigationFollowsThenStaysAndSleepOverridesTheCommand(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "movement", bound -> {
            var owner = bound.owner(); var mob = bound.mob();
            owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get())); movement = OnlinePlayers.register(List.of(owner));
            owner.moveTo(feet(h, 1, 1)); mob.moveTo(feet(h, 7, 7)); double before = mob.distanceToSqr(owner);
            check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.FOLLOW).equals("command_set"), "follow refused");
            h.runAfterDelay(55, () -> {
                check(mob.distanceToSqr(owner) < before - 16 && mob.distanceToSqr(owner) <= 36, "real follow made no bounded progress: " + mob.position() + ", " + bound.record().state());
                var work = FamiliarWork.in(h.getLevel()); check(work.lastPaths() <= 4 && work.lastScans() <= 8, "dimension budget exceeded");
                check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.STAY).equals("command_set"), "stay refused");
                Vec3 anchor = mob.position(); owner.moveTo(feet(h, 7, 7));
                h.runAfterDelay(25, () -> {
                    check(mob.position().distanceToSqr(anchor) < 2, "Stay followed its owner");
                    mob.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 100, 0));
                    check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.FOLLOW).equals("command_set"), "sleep desired command refused");
                    Vec3 asleep = mob.position();
                    h.runAfterDelay(20, () -> {
                        check(mob.position().distanceToSqr(asleep) < .5 && !mob.isNoAi(), "command overcame sleep or set permanent NoAI");
                        check(WitchStatusService.energyUnits(owner) == 7500, "ordinary movement spent energy");
                        FamiliarCommands.stopAssignments(owner.server, owner.getUUID(), "owner_offline");
                        check(bound.record().command() == FamiliarCommand.STAY && FamiliarStore.get(owner.server).mobile().count(owner.getUUID()) == 0, "logout retained mobile assignment");
                        h.succeed();
                    });
                });
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_guarding", timeoutTicks = 140)
    public static void guardAttacksOnlyThreatsAndPassiveClearsCombat(GameTestHelper h) {
        FamiliarFixtures.bound(h, "wolf", "guarding", bound -> {
            var owner = bound.owner(); var wolf = bound.mob(); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            guarding = OnlinePlayers.register(List.of(owner)); var zombie = h.spawn(EntityType.ZOMBIE, 5, 1, 5); zombie.setNoAi(true); zombie.setTarget(owner);
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(net.minecraft.world.item.Items.NETHERITE_HELMET)); zombie.clearFire();
            check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.GUARD_WITCH).equals("command_set"), "guard refused");
            h.runAfterDelay(35, () -> {
                check(zombie.getHealth() < zombie.getMaxHealth(), "guard never attacked actual hostile threat");
                check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.PASSIVE).equals("command_set"), "passive refused");
                float health = zombie.getHealth();
                h.runAfterDelay(25, () -> {
                    check(wolf.getTarget() == null && zombie.getHealth() == health, "passive continued attack");
                    check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.PROTECT_HOME).equals("no_home")
                            && FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.RETURN_HOME).equals("no_home"), "home command invented an assignment");
                    check(WitchStatusService.energyUnits(owner) == 7500, "ordinary guard spent energy"); h.succeed();
                });
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_mobile_cap", timeoutTicks = 100)
    public static void unloadedReservationsShareTheCapAndLogoutNeverRecreatesThem(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "mobile_cap", bound -> {
            var owner = bound.owner(); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            var store = FamiliarStore.get(owner.server); var records = new ArrayList<FamiliarRecord>();
            for (int i = 0; i < 4; i++) {
                var state = new CompoundTag(); state.putString("command", "stay"); state.putString("dimension", h.getLevel().dimension().location().toString()); state.putLong("position", bound.mob().blockPosition().asLong());
                var record = new FamiliarRecord(UUID.randomUUID(), owner.getUUID(), UUID.randomUUID(), "cat", "minecraft:cat", 1,
                        FamiliarRecord.Phase.ACTIVE, UUID.randomUUID(), 1, state, new CompoundTag()); check(store.create(record), "unloaded fixture shard"); records.add(record);
            }
            for (int i = 0; i < 3; i++) check(FamiliarCommands.set(owner, records.get(i).bond(), FamiliarCommand.values()[new int[]{0, 2, 4}[i]]).equals("command_set"), "shared mobile command refused");
            check(FamiliarCommands.set(owner, records.get(3).bond(), FamiliarCommand.FOLLOW).equals("mobile_cap"), "unloaded bond bypassed cap");
            check(FamiliarCommands.set(owner, records.get(0).bond(), FamiliarCommand.STAY).equals("command_set"), "slot release refused");
            check(FamiliarCommands.set(owner, records.get(3).bond(), FamiliarCommand.FOLLOW).equals("command_set"), "released slot unavailable");
            FamiliarCommands.login(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(owner));
            check(store.mobile().count(owner.getUUID()) == 0, "login retained an old reservation");
            for (var record : records) check(store.record(record.bond()).orElseThrow().command() == FamiliarCommand.STAY && h.getLevel().getEntity(record.creature()) == null, "login recreated absent entity");
            h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_recall", timeoutTicks = 100)
    public static void recallMovesOneLoadedIncarnationChargesOnceAndNeverSummonsAnAbsentOne(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "recall", bound -> {
            var owner = bound.owner(); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            h.runAfterDelay(6, () -> {
                check(FamiliarRecall.recall(owner, bound.bond()).equals("recalled"), "loaded recall refused");
                var live = h.getLevel().getEntity(bound.record().creature());
                check(bound.mob().isRemoved() && live != null && live != bound.mob() && live.distanceToSqr(owner) <= 9, "recall did not replace exactly once nearby");
                check(bound.record().generation() == 2 && WitchStatusService.energyUnits(owner) == 7000, "wrong generation or recall cost");
                check(FamiliarRecall.recall(owner, bound.bond()).equals("cooldown") && WitchStatusService.energyUnits(owner) == 7000, "recall cooldown charged twice");
                live.discard(); var store = FamiliarStore.get(owner.server); store.update(bound.record().change(s -> s.putLong("recallAt", 0)), false);
                check(FamiliarRecall.recall(owner, bound.bond()).equals("not_loaded") && WitchStatusService.energyUnits(owner) == 7000, "absent familiar summoned or charged"); h.succeed();
            });
        });
    }
}
